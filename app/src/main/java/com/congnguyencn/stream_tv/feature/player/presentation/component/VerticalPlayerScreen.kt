package com.congnguyencn.stream_tv.feature.player.presentation.component

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.player.presentation.PlayerUiState
import com.congnguyencn.stream_tv.feature.player.presentation.component.setting.PlayerSettingsPanel
import com.congnguyencn.stream_tv.feature.player.presentation.component.setting.rememberPlayerSettingsNavigationState
import com.congnguyencn.streamplayer.StreamTvPlayerManager
import com.congnguyencn.streamplayer.ui.StreamTvPlayerSurface

private object VerticalPlayerScreenDefaults {
  const val PortraitAspectRatio = 9f / 16f
  const val BackgroundPlayerStop = 0.46f
  const val BackgroundPanelStop = 0.68f
  val PlayerHorizontalOffset = 24.dp
  val SideExpansion = 24.dp
  val MinimumSideWidth = 300.dp
  val StageShape = RoundedCornerShape(12.dp)
  val SidePadding = 24.dp
}

/** Portrait playback with a permanently framed video and a companion section on its right. */
@OptIn(UnstableApi::class)
@Composable
internal fun VerticalPlayerScreen(
  uiState: PlayerUiState,
  playerManager: StreamTvPlayerManager,
  onTogglePlayPause: () -> Unit,
  onQualitySelected: (String) -> Unit,
  onSubtitleSelected: (String) -> Unit,
  onAudioSelected: (String) -> Unit,
  onRetry: () -> Unit,
  onExitPlayer: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val playerFocusRequester = androidx.compose.runtime.remember { FocusRequester() }
  val settingFocusRequester = androidx.compose.runtime.remember { FocusRequester() }
  val playerInteractionSource = androidx.compose.runtime.remember { MutableInteractionSource() }
  val settingsNavigationState = rememberPlayerSettingsNavigationState()

  LaunchedEffect(settingsNavigationState.isVisible) {
    if (!settingsNavigationState.isVisible) {
      withFrameMillis { }
      playerFocusRequester.requestFocus()
    }
  }

  LaunchedEffect(uiState.error) {
    if (uiState.error != null) settingsNavigationState.dismiss()
  }

  BackHandler(enabled = !settingsNavigationState.isVisible, onBack = onExitPlayer)

  BoxWithConstraints(
    modifier = modifier
      .fillMaxSize()
      .background(StreamTvColors.NeutralBlack)
      .testTag("vertical-player-screen"),
  ) {
    val portraitPlayerWidth = maxHeight * VerticalPlayerScreenDefaults.PortraitAspectRatio
    val sideWidth = (
      (maxWidth - portraitPlayerWidth) / 2 + VerticalPlayerScreenDefaults.SideExpansion
      ).coerceAtLeast(VerticalPlayerScreenDefaults.MinimumSideWidth)

    VerticalPlayerAmbientBackground(modifier = Modifier.fillMaxSize())

    Box(
      modifier = Modifier
        .align(Alignment.Center)
        .offset(x = -VerticalPlayerScreenDefaults.PlayerHorizontalOffset)
        .fillMaxHeight()
        .aspectRatio(VerticalPlayerScreenDefaults.PortraitAspectRatio)
        .clip(VerticalPlayerScreenDefaults.StageShape)
        .testTag("vertical-player-stage"),
    ) {
      StreamTvPlayerSurface(
        playerManager = playerManager,
        modifier = Modifier.fillMaxSize(),
        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
      )

      if (uiState.error == null) {
        VerticalPlayerStageChrome(uiState = uiState)
      }

      Box(
        modifier = Modifier
          .fillMaxSize()
          .focusRequester(playerFocusRequester)
          .focusProperties { canFocus = !settingsNavigationState.isVisible }
          .onPreviewKeyEvent { event ->
            if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

            when (event.key) {
              Key.DirectionCenter,
              Key.Enter,
              Key.NumPadEnter,
              Key.Spacebar,
              Key.MediaPlayPause,
              -> {
                onTogglePlayPause()
                true
              }

              Key.DirectionRight -> {
                if (uiState.settings.isAvailable) settingFocusRequester.requestFocus()
                true
              }

              else -> false
            }
          }
          .focusable(interactionSource = playerInteractionSource),
      )
    }

    if (uiState.error == null) {
      AnimatedVisibility(
        visible = !settingsNavigationState.isVisible,
        modifier = Modifier
          .align(Alignment.CenterEnd)
          .width(sideWidth)
          .fillMaxHeight()
          .padding(VerticalPlayerScreenDefaults.SidePadding),
        enter = fadeIn(),
        exit = fadeOut(),
      ) {
        VerticalPlayerInteractionSection(
          uiState = uiState,
          settingFocusRequester = settingFocusRequester,
          onMoveToPlayer = { playerFocusRequester.requestFocus() },
          onSettingsClick = { settingsNavigationState.open(uiState.settings) },
          modifier = Modifier.fillMaxSize(),
        )
      }

      PlayerSettingsPanel(
        settings = uiState.settings,
        navigationState = settingsNavigationState,
        onQualitySelected = onQualitySelected,
        onSubtitleSelected = onSubtitleSelected,
        onAudioSelected = onAudioSelected,
        onDismissed = { settingFocusRequester.requestFocus() },
        modifier = Modifier
          .align(Alignment.CenterEnd)
          .width(sideWidth)
          .fillMaxHeight()
          .padding(VerticalPlayerScreenDefaults.SidePadding),
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
private fun VerticalPlayerAmbientBackground(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier.background(
      Brush.horizontalGradient(
        colorStops = arrayOf(
          0f to StreamTvColors.TransparentBlack80,
          VerticalPlayerScreenDefaults.BackgroundPlayerStop to StreamTvColors.NeutralBlack,
          VerticalPlayerScreenDefaults.BackgroundPanelStop to StreamTvColors.Neutral100,
          1f to StreamTvColors.Primary100.copy(alpha = 0.44f),
        ),
      ),
    ),
  )
}

@Composable
private fun VerticalPlayerStageChrome(uiState: PlayerUiState) {
  Box(modifier = Modifier.fillMaxSize()) {
    if (uiState.isBuffering) {
      PlayerBufferingIndicator(modifier = Modifier.align(Alignment.Center))
    } else if (!uiState.isPlaying) {
      PlayerPlaybackBadge(
        isPlaying = false,
        modifier = Modifier.align(Alignment.Center),
      )
    }

    if (uiState.isSeekable) {
      Box(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
          .height(92.dp)
          .background(
            Brush.verticalGradient(
              colors = listOf(StreamTvColors.Transparent, StreamTvColors.TransparentBlack80),
            ),
          )
          .padding(horizontal = 20.dp, vertical = 18.dp),
        contentAlignment = Alignment.BottomCenter,
      ) {
        PlayerProgressBar(
          progressFraction = uiState.progressFraction,
          bufferedFraction = uiState.bufferedFraction,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    }
  }
}

@Composable
private fun VerticalPlayerInteractionSection(
  uiState: PlayerUiState,
  settingFocusRequester: FocusRequester,
  onMoveToPlayer: () -> Unit,
  onSettingsClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.focusGroup(),
    verticalArrangement = Arrangement.Bottom,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          color = StreamTvColors.TransparentWhite10,
          shape = VerticalPlayerScreenDefaults.StageShape,
        )
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Text(
        text = uiState.title,
        color = StreamTvColors.NeutralWhite,
        style = StreamTvTheme.typography.headlineLarge,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
      if (uiState.isSeekable) {
        PlayerTimeLabel(position = uiState.position, duration = uiState.duration)
      } else {
        PlayerLiveBadge()
      }
    }

    Spacer(modifier = Modifier.height(28.dp))

    if (uiState.settings.isAvailable) {
      Row(modifier = Modifier.fillMaxWidth()) {
        PlayerRoundIconButton(
          iconResId = R.drawable.ic_setting,
          contentDescription = stringResource(R.string.player_settings),
          onClick = onSettingsClick,
          modifier = Modifier
            .size(44.dp)
            .focusRequester(settingFocusRequester)
            .onPreviewKeyEvent { event ->
              if (
                event.type == KeyEventType.KeyDown &&
                (event.key == Key.DirectionLeft || event.key == Key.Back || event.key == Key.Escape)
              ) {
                onMoveToPlayer()
                true
              } else {
                false
              }
            }
            .testTag("vertical-player-settings-button"),
        )
      }
    }

    Spacer(modifier = Modifier.height(32.dp))
  }
}
