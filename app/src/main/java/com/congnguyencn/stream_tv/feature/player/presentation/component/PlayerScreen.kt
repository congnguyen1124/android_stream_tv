package com.congnguyencn.stream_tv.feature.player.presentation.component

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.congnguyencn.stream_tv.feature.player.presentation.component.setting.PlayerSettingsPanel
import com.congnguyencn.stream_tv.feature.player.presentation.component.setting.rememberPlayerSettingsNavigationState
import com.congnguyencn.streamplayer.StreamTvPlayerManager
import com.congnguyencn.streamplayer.ui.StreamTvPlayerSurface
import kotlinx.coroutines.delay

private object PlayerScreenDefaults {
  const val ControllerAutoHideMillis = 5_000L
  val SettingsPanelWidth = 360.dp
  val SettingsPanelPadding = 24.dp
}

/**
 * Landscape playback with the same controller and setting-section behaviour as the original app.
 *
 * The controller is a full-screen Box overlay only. Related content and episode rows deliberately
 * stay outside this player, so there is no LazyColumn and no second content-navigation mode.
 */
@OptIn(UnstableApi::class)
@Composable
@Suppress("LongMethod", "CyclomaticComplexMethod", "CognitiveComplexMethod")
internal fun PlayerScreen(
  uiState: PlayerUiState,
  playerManager: StreamTvPlayerManager,
  onTogglePlayPause: () -> Unit,
  onSeekForward: () -> Unit,
  onSeekBack: () -> Unit,
  onQualitySelected: (String) -> Unit,
  onSubtitleSelected: (String) -> Unit,
  onAudioSelected: (String) -> Unit,
  onRetry: () -> Unit,
  onExitPlayer: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val playerFocusRequester = remember { FocusRequester() }
  val progressFocusRequester = remember { FocusRequester() }
  val settingFocusRequester = remember { FocusRequester() }
  val playerInteractionSource = remember { MutableInteractionSource() }
  val settingsNavigationState = rememberPlayerSettingsNavigationState()
  var isControllerVisible by remember { mutableStateOf(true) }

  val focusController = {
    if (uiState.isSeekable) {
      progressFocusRequester.requestFocus()
    } else if (uiState.settings.isAvailable) {
      settingFocusRequester.requestFocus()
    } else {
      playerFocusRequester.requestFocus()
    }
  }

  LaunchedEffect(
    isControllerVisible,
    settingsNavigationState.isVisible,
    uiState.isSeekable,
    uiState.settings.isAvailable,
  ) {
    if (settingsNavigationState.isVisible) return@LaunchedEffect

    withFrameMillis { }
    if (isControllerVisible) focusController() else playerFocusRequester.requestFocus()
  }

  LaunchedEffect(
    isControllerVisible,
    settingsNavigationState.isVisible,
    uiState.isPlaying,
    uiState.error,
  ) {
    if (
      isControllerVisible &&
      !settingsNavigationState.isVisible &&
      uiState.isPlaying &&
      uiState.error == null
    ) {
      delay(PlayerScreenDefaults.ControllerAutoHideMillis)
      isControllerVisible = false
    }
  }

  LaunchedEffect(uiState.error) {
    if (uiState.error != null) settingsNavigationState.dismiss()
  }

  BackHandler(enabled = !settingsNavigationState.isVisible) {
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
      isFocusEnabled = !isControllerVisible && !settingsNavigationState.isVisible,
      focusRequester = playerFocusRequester,
      interactionSource = playerInteractionSource,
      onKeyDown = { key ->
        when (key) {
          Key.MediaPlayPause -> {
            onTogglePlayPause()
            isControllerVisible = true
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
            isControllerVisible = true
            true
          }

          else -> false
        }
      },
    )

    if (uiState.error == null) {
      AnimatedVisibility(
        visible = isControllerVisible && !settingsNavigationState.isVisible,
        modifier = Modifier.fillMaxSize(),
        enter = fadeIn(),
        exit = fadeOut(),
      ) {
        PlayerController(
          uiState = uiState,
          progressFocusRequester = progressFocusRequester,
          settingFocusRequester = settingFocusRequester,
          onTogglePlayPause = onTogglePlayPause,
          onSeekForward = onSeekForward,
          onSeekBack = onSeekBack,
          onSettingsClick = { settingsNavigationState.open(uiState.settings) },
        )
      }

      if (uiState.isBuffering) {
        PlayerBufferingIndicator(modifier = Modifier.align(Alignment.Center))
      } else if (!uiState.isPlaying && !isControllerVisible) {
        PlayerPlaybackBadge(
          isPlaying = false,
          modifier = Modifier.align(Alignment.Center),
        )
      }

      PlayerSettingsPanel(
        settings = uiState.settings,
        navigationState = settingsNavigationState,
        onQualitySelected = onQualitySelected,
        onSubtitleSelected = onSubtitleSelected,
        onAudioSelected = onAudioSelected,
        onDismissed = {
          isControllerVisible = true
          settingFocusRequester.requestFocus()
        },
        modifier = Modifier
          .align(Alignment.CenterEnd)
          .padding(PlayerScreenDefaults.SettingsPanelPadding)
          .width(PlayerScreenDefaults.SettingsPanelWidth)
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
      .focusable(interactionSource = interactionSource),
  )
}
