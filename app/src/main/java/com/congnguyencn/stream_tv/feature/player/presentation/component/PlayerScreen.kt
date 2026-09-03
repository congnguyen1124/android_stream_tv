package com.congnguyencn.stream_tv.feature.player.presentation.component

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.feature.player.presentation.PlayerUiState
import com.congnguyencn.stream_tv.feature.player.presentation.component.section.PlayerPendingFocusTarget
import com.congnguyencn.stream_tv.feature.player.presentation.component.section.PlayerSection
import com.congnguyencn.stream_tv.feature.player.presentation.component.section.PlayerSideSection
import com.congnguyencn.stream_tv.feature.player.presentation.component.section.awaitPlayerSectionFrame
import com.congnguyencn.stream_tv.feature.player.presentation.component.section.rememberPlayerSectionNavigationState
import com.congnguyencn.streamplayer.StreamTvPlayerManager
import com.congnguyencn.streamplayer.ui.StreamTvPlayerSurface
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

private object PlayerScreenDefaults {
  const val ControllerAutoHideMillis = 5_000L
  val SideSectionWidth = 315.dp
  val SideSectionHorizontalPadding = 30.dp
  val SideSectionVerticalPadding = 30.dp
}

/**
 * Landscape playback.
 *
 * Focus ownership runs through one value — [PlayerFocusableGroup] — instead of a set of booleans
 * each `LaunchedEffect` interpreted for itself. Every gate below reads that one value, so "who owns
 * the D-pad right now" has exactly one answer and the answer cannot contradict itself.
 */
@OptIn(UnstableApi::class)
@Composable
@Suppress("LongMethod", "LongParameterList", "CyclomaticComplexMethod")
internal fun PlayerScreen(
  uiState: PlayerUiState,
  playerManager: StreamTvPlayerManager,
  onTogglePlayPause: () -> Unit,
  onSeekForward: () -> Unit,
  onSeekBack: () -> Unit,
  onToggleLike: () -> Unit,
  onToggleSaved: () -> Unit,
  onCommentLikeToggle: (Long) -> Unit,
  onQualitySelected: (String) -> Unit,
  onSubtitleSelected: (String) -> Unit,
  onAudioSelected: (String) -> Unit,
  onRetry: () -> Unit,
  onExitPlayer: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val playerFocusRequester = remember { FocusRequester() }
  val pendingFocusRequester = remember { FocusRequester() }
  val errorRetryFocusRequester = remember { FocusRequester() }
  val playerInteractionSource = remember { MutableInteractionSource() }
  val sectionNavigationState = rememberPlayerSectionNavigationState()
  val controllerFocusRequesters = remember {
    PlayerControllerFocusTarget.entries.associateWith { FocusRequester() }
  }
  var controllerFocusTarget by remember { mutableStateOf(PlayerControllerFocusTarget.PlayPause) }
  var isControllerVisible by remember { mutableStateOf(false) }
  var controllerInteractionKey by remember { mutableIntStateOf(0) }

  val focusableGroup by remember(sectionNavigationState) {
    derivedStateOf {
      resolvePlayerFocusableGroup(
        hasError = uiState.error != null,
        isControllerVisible = isControllerVisible,
        navigationState = sectionNavigationState,
      )
    }
  }

  val showController = {
    // No parking here: the surface stops being focusable the moment the group flips to Controller,
    // so it releases focus on its own. Parking as well raced the controller's entry focus request
    // and the anchor won, which left the controller on screen with nothing focused.
    controllerInteractionKey++
    isControllerVisible = true
  }
  val openSection: (PlayerSection, PlayerControllerFocusTarget) -> Unit = { section, restoreTarget ->
    if (sectionNavigationState.isAtBaseLevel) {
      controllerFocusTarget = restoreTarget
      pendingFocusRequester.requestFocus()
      isControllerVisible = false
      sectionNavigationState.openRoot(section)
    }
  }

  // The one place focus is handed out. Controller and Section own focusable subtrees that claim it
  // as they enter; everything else is an anchor this effect points at directly. Letting the error
  // panel request focus for itself raced this decision and left nothing focusable when it lost.
  LaunchedEffect(focusableGroup, uiState.error?.isRetryable) {
    awaitPlayerSectionFrame()
    when (focusableGroup) {
      PlayerFocusableGroup.Surface -> playerFocusRequester.requestFocus()
      PlayerFocusableGroup.Parked -> pendingFocusRequester.requestFocus()

      PlayerFocusableGroup.Error ->
        if (uiState.error?.isRetryable == true) errorRetryFocusRequester.requestFocus()

      PlayerFocusableGroup.Controller,
      PlayerFocusableGroup.Section,
      -> Unit
    }
  }

  LaunchedEffect(focusableGroup, uiState.isPlaying, controllerInteractionKey) {
    if (focusableGroup == PlayerFocusableGroup.Controller && uiState.isPlaying) {
      delay(PlayerScreenDefaults.ControllerAutoHideMillis.milliseconds)
      isControllerVisible = false
    }
  }

  LaunchedEffect(uiState.error) {
    if (uiState.error != null) {
      sectionNavigationState.reset()
      isControllerVisible = false
    }
  }

  BackHandler(enabled = !sectionNavigationState.hasSectionInPlay) {
    if (focusableGroup == PlayerFocusableGroup.Controller) {
      isControllerVisible = false
    } else {
      onExitPlayer()
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(StreamTvColors.NeutralBlack)
      .testTag("player-screen"),
  ) {
    StreamTvPlayerSurface(
      playerManager = playerManager,
      modifier = Modifier.fillMaxSize(),
      resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT,
    )

    PlayerInputTarget(
      isFocusEnabled = focusableGroup == PlayerFocusableGroup.Surface,
      focusRequester = playerFocusRequester,
      interactionSource = playerInteractionSource,
      onKeyDown = { key ->
        when (key) {
          Key.MediaPlayPause -> {
            onTogglePlayPause()
            showController()
            true
          }

          Key.DirectionCenter,
          Key.Enter,
          Key.NumPadEnter,
          Key.Spacebar,
          Key.DirectionLeft,
          Key.DirectionRight,
          Key.DirectionUp,
          Key.DirectionDown,
          -> {
            showController()
            true
          }

          else -> false
        }
      },
    )

    PlayerPendingFocusTarget(
      focusRequester = pendingFocusRequester,
      modifier = Modifier.align(Alignment.CenterStart),
    )

    if (uiState.error == null) {
      AnimatedVisibility(
        visible = focusableGroup == PlayerFocusableGroup.Controller,
        modifier = Modifier.fillMaxSize(),
        enter = expandVertically { fullHeight -> fullHeight },
        exit = shrinkVertically { fullHeight -> fullHeight },
      ) {
        PlayerController(
          uiState = uiState,
          focusTarget = controllerFocusTarget,
          focusRequesters = controllerFocusRequesters,
          onFocusTargetChanged = { target ->
            controllerFocusTarget = target
            controllerInteractionKey++
          },
          onInteraction = { controllerInteractionKey++ },
          onTogglePlayPause = onTogglePlayPause,
          onSeekForward = onSeekForward,
          onSeekBack = onSeekBack,
          onDescriptionClick = {
            openSection(PlayerSection.Metadata, PlayerControllerFocusTarget.Description)
          },
          onLikeClick = onToggleLike,
          onSaveClick = onToggleSaved,
          onCommentClick = {
            openSection(PlayerSection.Comments, PlayerControllerFocusTarget.Comment)
          },
          onSettingsClick = {
            openSection(PlayerSection.Settings, PlayerControllerFocusTarget.Settings)
          },
        )
      }

      if (uiState.isBuffering) {
        PlayerBufferingIndicator(modifier = Modifier.align(Alignment.Center))
      }

      PlayerSideSection(
        uiState = uiState,
        navigationState = sectionNavigationState,
        pendingFocusRequester = pendingFocusRequester,
        dismissOnLeft = false,
        onQualitySelected = onQualitySelected,
        onSubtitleSelected = onSubtitleSelected,
        onAudioSelected = onAudioSelected,
        onCommentLikeToggle = onCommentLikeToggle,
        onRootDismissed = { showController() },
        compactSettings = true,
        modifier = Modifier
          .align(Alignment.CenterEnd)
          .padding(
            top = PlayerScreenDefaults.SideSectionVerticalPadding,
            end = PlayerScreenDefaults.SideSectionHorizontalPadding,
            bottom = PlayerScreenDefaults.SideSectionVerticalPadding,
          )
          .width(PlayerScreenDefaults.SideSectionWidth)
          .fillMaxHeight(),
      )
    } else {
      PlayerErrorPanel(
        error = uiState.error,
        onRetry = onRetry.takeIf { uiState.error.isRetryable },
        retryFocusRequester = errorRetryFocusRequester,
        modifier = Modifier.fillMaxSize(),
      )
    }
  }
}

@Composable
private fun PlayerInputTarget(
  isFocusEnabled: Boolean,
  focusRequester: FocusRequester,
  interactionSource: MutableInteractionSource,
  onKeyDown: (Key) -> Boolean,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .focusRequester(focusRequester)
      .focusProperties { canFocus = isFocusEnabled }
      .onPreviewKeyEvent { event ->
        event.type == KeyEventType.KeyDown && onKeyDown(event.key)
      }
      .testTag("player-input-target")
      .focusable(interactionSource = interactionSource),
  )
}
