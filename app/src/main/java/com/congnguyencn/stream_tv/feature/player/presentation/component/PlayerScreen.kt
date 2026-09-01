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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
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

/** Landscape playback with controller and retained section focus behavior from the reference app. */
@OptIn(UnstableApi::class)
@Composable
@Suppress("LongMethod", "LongParameterList", "CyclomaticComplexMethod", "CognitiveComplexMethod")
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
  val playerInteractionSource = remember { MutableInteractionSource() }
  val sectionNavigationState = rememberPlayerSectionNavigationState()
  val controllerFocusRequesters = remember {
    PlayerControllerFocusTarget.entries.associateWith { FocusRequester() }
  }
  var controllerFocusTarget by remember {
    mutableStateOf(PlayerControllerFocusTarget.Progress)
  }
  var isControllerVisible by remember { mutableStateOf(false) }
  var controllerInteractionKey by remember { mutableIntStateOf(0) }

  val showController = {
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

  LaunchedEffect(
    isControllerVisible,
    sectionNavigationState.hasSectionInPlay,
    sectionNavigationState.shouldParkFocus,
    uiState.error,
  ) {
    if (uiState.error != null) return@LaunchedEffect
    withFrameMillis { }
    when {
      sectionNavigationState.shouldParkFocus -> pendingFocusRequester.requestFocus()
      sectionNavigationState.hasSectionInPlay -> Unit
      isControllerVisible -> Unit
      else -> playerFocusRequester.requestFocus()
    }
  }

  LaunchedEffect(
    isControllerVisible,
    sectionNavigationState.hasSectionInPlay,
    uiState.isPlaying,
    uiState.error,
    controllerInteractionKey,
  ) {
    if (
      isControllerVisible &&
      !sectionNavigationState.hasSectionInPlay &&
      uiState.isPlaying &&
      uiState.error == null
    ) {
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
    if (isControllerVisible && uiState.error == null) {
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
      isFocusEnabled = !isControllerVisible && !sectionNavigationState.hasSectionInPlay,
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
        visible = isControllerVisible && !sectionNavigationState.hasSectionInPlay,
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
          onTitleClick = {
            openSection(PlayerSection.Metadata, PlayerControllerFocusTarget.Title)
          },
          onLikeClick = onToggleLike,
          onSaveClick = onToggleSaved,
          onCommentClick = {
            openSection(PlayerSection.Comments, PlayerControllerFocusTarget.CommentButton)
          },
          onSettingsClick = {
            openSection(PlayerSection.Settings, PlayerControllerFocusTarget.SettingButton)
          },
        )
      }

      if (uiState.isBuffering) {
        PlayerBufferingIndicator(modifier = Modifier.align(Alignment.Center))
      } else if (!uiState.isPlaying && !isControllerVisible && !sectionNavigationState.hasSectionInPlay) {
        PlayerPlaybackBadge(
          isPlaying = false,
          modifier = Modifier.align(Alignment.Center),
        )
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
