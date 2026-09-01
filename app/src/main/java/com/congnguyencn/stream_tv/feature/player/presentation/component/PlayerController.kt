package com.congnguyencn.stream_tv.feature.player.presentation.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.player.presentation.PlayerUiState

private object PlayerControllerDefaults {
  const val GradientMidStop = 0.42f
  val HorizontalPadding = 52.dp
  val BottomPadding = 36.dp
  val ContentSpacing = 22.dp
  val SettingButtonSize = 44.dp
  val SettingIconSize = 22.dp
  val SeekControlHeight = 42.dp
  val SeekThumbSize = 14.dp
}

/** Full-screen player chrome. It deliberately contains no content lists or lazy container. */
@Composable
internal fun PlayerController(
  uiState: PlayerUiState,
  progressFocusRequester: FocusRequester,
  settingFocusRequester: FocusRequester,
  onTogglePlayPause: () -> Unit,
  onSeekForward: () -> Unit,
  onSeekBack: () -> Unit,
  onSettingsClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colorStops = arrayOf(
            0f to StreamTvColors.TransparentBlack20,
            PlayerControllerDefaults.GradientMidStop to StreamTvColors.Transparent,
            1f to StreamTvColors.TransparentBlack80,
          ),
        ),
      )
      .testTag("player-controller"),
  ) {
    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .padding(
          start = PlayerControllerDefaults.HorizontalPadding,
          end = PlayerControllerDefaults.HorizontalPadding,
          bottom = PlayerControllerDefaults.BottomPadding,
        ),
      verticalArrangement = Arrangement.spacedBy(PlayerControllerDefaults.ContentSpacing),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        PlayerTitleRow(
          title = uiState.title,
          isLive = !uiState.isSeekable,
          modifier = Modifier.weight(1f),
        )
        if (uiState.settings.isAvailable) {
          Spacer(modifier = Modifier.width(24.dp))
          PlayerRoundIconButton(
            iconResId = R.drawable.ic_setting,
            contentDescription = stringResource(R.string.player_settings),
            onClick = onSettingsClick,
            modifier = Modifier
              .size(PlayerControllerDefaults.SettingButtonSize)
              .focusRequester(settingFocusRequester)
              .focusProperties {
                if (uiState.isSeekable) down = progressFocusRequester
              },
          )
        }
      }

      if (uiState.isSeekable) {
        PlayerSeekControl(
          uiState = uiState,
          onTogglePlayPause = onTogglePlayPause,
          onSeekForward = onSeekForward,
          onSeekBack = onSeekBack,
          onMoveToSettings = {
            if (uiState.settings.isAvailable) settingFocusRequester.requestFocus()
          },
          modifier = Modifier.focusRequester(progressFocusRequester),
        )
      } else {
        PlayerTimeLabel(position = uiState.position, duration = uiState.duration)
      }
    }
  }
}

@Composable
private fun PlayerSeekControl(
  uiState: PlayerUiState,
  onTogglePlayPause: () -> Unit,
  onSeekForward: () -> Unit,
  onSeekBack: () -> Unit,
  onMoveToSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isFocused by interactionSource.collectIsFocusedAsState()

  Surface(
    onClick = onTogglePlayPause,
    modifier = modifier
      .fillMaxWidth()
      .height(PlayerControllerDefaults.SeekControlHeight)
      .onPreviewKeyEvent { event ->
        if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

        when (event.key) {
          Key.DirectionLeft, Key.MediaRewind -> onSeekBack()
          Key.DirectionRight, Key.MediaFastForward -> onSeekForward()
          Key.DirectionUp -> onMoveToSettings()
          Key.MediaPlayPause -> onTogglePlayPause()
          else -> return@onPreviewKeyEvent false
        }
        true
      }
      .testTag("player-seek-control"),
    colors = ClickableSurfaceDefaults.colors(
      containerColor = StreamTvColors.Transparent,
      focusedContainerColor = StreamTvColors.Transparent,
      pressedContainerColor = StreamTvColors.Transparent,
    ),
    scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
    interactionSource = interactionSource,
  ) {
    Row(
      modifier = Modifier.fillMaxSize(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = uiState.position.coerceAtMost(uiState.duration).toClockString(),
        color = StreamTvColors.Neutral10,
        style = StreamTvTheme.typography.labelMedium,
      )
      Spacer(modifier = Modifier.width(14.dp))
      PlayerSeekTrack(
        progressFraction = uiState.progressFraction,
        bufferedFraction = uiState.bufferedFraction,
        isFocused = isFocused,
        modifier = Modifier.weight(1f),
      )
      Spacer(modifier = Modifier.width(14.dp))
      Text(
        text = uiState.duration.toClockString(),
        color = StreamTvColors.Neutral10,
        style = StreamTvTheme.typography.labelMedium,
      )
    }
  }
}

@Composable
private fun PlayerSeekTrack(
  progressFraction: Float,
  bufferedFraction: Float,
  isFocused: Boolean,
  modifier: Modifier = Modifier,
) {
  BoxWithConstraints(
    modifier = modifier.height(PlayerControllerDefaults.SeekControlHeight),
    contentAlignment = Alignment.CenterStart,
  ) {
    PlayerProgressBar(
      progressFraction = progressFraction,
      bufferedFraction = bufferedFraction,
      modifier = Modifier.fillMaxWidth(),
    )
    if (isFocused) {
      val travel = (maxWidth - PlayerControllerDefaults.SeekThumbSize).coerceAtLeast(0.dp)
      Box(
        modifier = Modifier
          .offset(x = travel * progressFraction)
          .size(PlayerControllerDefaults.SeekThumbSize)
          .background(StreamTvColors.NeutralWhite, CircleShape),
      )
    }
  }
}

@Composable
internal fun PlayerRoundIconButton(
  @DrawableRes iconResId: Int,
  contentDescription: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    onClick = onClick,
    modifier = modifier,
    shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
    scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
    colors = ClickableSurfaceDefaults.colors(
      containerColor = StreamTvColors.TransparentWhite10,
      contentColor = StreamTvColors.NeutralWhite,
      focusedContainerColor = StreamTvColors.NeutralWhite,
      focusedContentColor = StreamTvColors.NeutralBlack,
      pressedContainerColor = StreamTvColors.Primary60,
      pressedContentColor = StreamTvColors.NeutralWhite,
    ),
  ) {
    Icon(
      imageVector = ImageVector.vectorResource(iconResId),
      contentDescription = contentDescription,
      modifier = Modifier
        .align(Alignment.Center)
        .size(PlayerControllerDefaults.SettingIconSize),
    )
  }
}
