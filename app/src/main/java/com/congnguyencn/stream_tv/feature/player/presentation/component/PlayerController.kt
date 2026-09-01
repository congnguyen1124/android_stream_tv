package com.congnguyencn.stream_tv.feature.player.presentation.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
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
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.player.presentation.PlayerUiState

internal enum class PlayerControllerFocusTarget {
  Title,
  LikeButton,
  SaveButton,
  CommentButton,
  SettingButton,
  Progress,
}

private object PlayerControllerDefaults {
  const val GradientMidStop = 0.42f
  val HorizontalPadding = 58.dp
  val BottomPadding = 36.dp
  val ContentSpacing = 16.dp
  val TitleWidth = 400.dp
  val ActionButtonSize = 40.dp
  val ActionSpacing = 16.dp
  val ActionIconSize = 22.dp
  val SeekControlHeight = 42.dp
  val SeekThumbSize = 14.dp
}

/** Full-screen controller chrome with the same focus targets as the reference TV player. */
@Composable
@Suppress("LongParameterList")
internal fun PlayerController(
  uiState: PlayerUiState,
  focusTarget: PlayerControllerFocusTarget,
  focusRequesters: Map<PlayerControllerFocusTarget, FocusRequester>,
  onFocusTargetChanged: (PlayerControllerFocusTarget) -> Unit,
  onInteraction: () -> Unit,
  onTogglePlayPause: () -> Unit,
  onSeekForward: () -> Unit,
  onSeekBack: () -> Unit,
  onTitleClick: () -> Unit,
  onLikeClick: () -> Unit,
  onSaveClick: () -> Unit,
  onCommentClick: () -> Unit,
  onSettingsClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val resolvedFocusTarget = focusTarget.takeIf { target -> uiState.isTargetAvailable(target) }
    ?: uiState.defaultControllerFocusTarget()

  LaunchedEffect(resolvedFocusTarget, uiState.isSeekable, uiState.settings.isAvailable) {
    androidx.compose.runtime.withFrameMillis { }
    focusRequesters[resolvedFocusTarget]?.requestFocus()
  }

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
      ControllerTitleAndActions(
        uiState = uiState,
        focusRequesters = focusRequesters,
        onFocusTargetChanged = onFocusTargetChanged,
        onInteraction = onInteraction,
        onTitleClick = onTitleClick,
        onLikeClick = onLikeClick,
        onSaveClick = onSaveClick,
        onCommentClick = onCommentClick,
        onSettingsClick = onSettingsClick,
      )

      if (uiState.isSeekable) {
        PlayerSeekControl(
          uiState = uiState,
          onTogglePlayPause = {
            onInteraction()
            onTogglePlayPause()
          },
          onSeekForward = {
            onInteraction()
            onSeekForward()
          },
          onSeekBack = {
            onInteraction()
            onSeekBack()
          },
          onFocused = { onFocusTargetChanged(PlayerControllerFocusTarget.Progress) },
          modifier = Modifier
            .focusRequester(focusRequesters.getValue(PlayerControllerFocusTarget.Progress))
            .focusProperties {
              up = focusRequesters.getValue(uiState.lastActionFocusTarget())
            },
        )
      } else {
        PlayerTimeLabel(position = uiState.position, duration = uiState.duration)
      }
    }
  }
}

@Composable
@Suppress("LongParameterList")
private fun ControllerTitleAndActions(
  uiState: PlayerUiState,
  focusRequesters: Map<PlayerControllerFocusTarget, FocusRequester>,
  onFocusTargetChanged: (PlayerControllerFocusTarget) -> Unit,
  onInteraction: () -> Unit,
  onTitleClick: () -> Unit,
  onLikeClick: () -> Unit,
  onSaveClick: () -> Unit,
  onCommentClick: () -> Unit,
  onSettingsClick: () -> Unit,
) {
  val progressRequester = focusRequesters.getValue(PlayerControllerFocusTarget.Progress)
  val titleRequester = focusRequesters.getValue(PlayerControllerFocusTarget.Title)
  val likeRequester = focusRequesters.getValue(PlayerControllerFocusTarget.LikeButton)
  val saveRequester = focusRequesters.getValue(PlayerControllerFocusTarget.SaveButton)
  val commentRequester = focusRequesters.getValue(PlayerControllerFocusTarget.CommentButton)
  val settingRequester = focusRequesters.getValue(PlayerControllerFocusTarget.SettingButton)

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Bottom,
  ) {
    PlayerTitleButton(
      uiState = uiState,
      onClick = {
        onInteraction()
        onTitleClick()
      },
      modifier = Modifier
        .width(PlayerControllerDefaults.TitleWidth)
        .focusRequester(titleRequester)
        .focusProperties {
          right = likeRequester
          if (uiState.isSeekable) down = progressRequester
        }
        .onFocusChanged {
          if (it.hasFocus) onFocusTargetChanged(PlayerControllerFocusTarget.Title)
        }
        .testTag("player-controller-title"),
    )

    Row(horizontalArrangement = Arrangement.spacedBy(PlayerControllerDefaults.ActionSpacing)) {
      PlayerControllerActionButton(
        iconResId = if (uiState.isLiked) R.drawable.ic_player_like_filled else R.drawable.ic_player_like,
        contentDescription = stringResource(R.string.player_like),
        focusTarget = PlayerControllerFocusTarget.LikeButton,
        focusRequester = likeRequester,
        left = titleRequester,
        right = saveRequester,
        down = progressRequester.takeIf { uiState.isSeekable },
        onFocusTargetChanged = onFocusTargetChanged,
        onClick = {
          onInteraction()
          onLikeClick()
        },
      )
      PlayerControllerActionButton(
        iconResId = if (uiState.isSaved) R.drawable.ic_player_saved else R.drawable.ic_player_save,
        contentDescription = stringResource(R.string.player_save),
        focusTarget = PlayerControllerFocusTarget.SaveButton,
        focusRequester = saveRequester,
        left = likeRequester,
        right = commentRequester,
        down = progressRequester.takeIf { uiState.isSeekable },
        onFocusTargetChanged = onFocusTargetChanged,
        onClick = {
          onInteraction()
          onSaveClick()
        },
      )
      PlayerControllerActionButton(
        iconResId = R.drawable.ic_player_comment,
        contentDescription = stringResource(R.string.player_comments),
        focusTarget = PlayerControllerFocusTarget.CommentButton,
        focusRequester = commentRequester,
        left = saveRequester,
        right = settingRequester.takeIf { uiState.settings.isAvailable },
        down = progressRequester.takeIf { uiState.isSeekable },
        onFocusTargetChanged = onFocusTargetChanged,
        onClick = {
          onInteraction()
          onCommentClick()
        },
      )
      if (uiState.settings.isAvailable) {
        PlayerControllerActionButton(
          iconResId = R.drawable.ic_setting,
          contentDescription = stringResource(R.string.player_settings),
          focusTarget = PlayerControllerFocusTarget.SettingButton,
          focusRequester = settingRequester,
          left = commentRequester,
          right = null,
          down = progressRequester.takeIf { uiState.isSeekable },
          onFocusTargetChanged = onFocusTargetChanged,
          onClick = {
            onInteraction()
            onSettingsClick()
          },
        )
      }
    }
  }
}

@Composable
private fun PlayerTitleButton(uiState: PlayerUiState, onClick: () -> Unit, modifier: Modifier = Modifier) {
  Surface(
    onClick = onClick,
    modifier = modifier,
    shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(12.dp)),
    scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
    colors = ClickableSurfaceDefaults.colors(
      containerColor = StreamTvColors.Transparent,
      contentColor = StreamTvColors.NeutralWhite,
      focusedContainerColor = StreamTvColors.TransparentWhite10,
      focusedContentColor = StreamTvColors.NeutralWhite,
    ),
  ) {
    PlayerTitleRow(
      title = uiState.title,
      isLive = !uiState.isSeekable,
      modifier = Modifier.padding(16.dp),
    )
  }
}

@Composable
private fun PlayerControllerActionButton(
  @DrawableRes iconResId: Int,
  contentDescription: String,
  focusTarget: PlayerControllerFocusTarget,
  focusRequester: FocusRequester,
  left: FocusRequester?,
  right: FocusRequester?,
  down: FocusRequester?,
  onFocusTargetChanged: (PlayerControllerFocusTarget) -> Unit,
  onClick: () -> Unit,
) {
  PlayerRoundIconButton(
    iconResId = iconResId,
    contentDescription = contentDescription,
    onClick = onClick,
    modifier = Modifier
      .size(PlayerControllerDefaults.ActionButtonSize)
      .focusRequester(focusRequester)
      .focusProperties {
        left?.let { requester -> this.left = requester }
        right?.let { requester -> this.right = requester }
        down?.let { requester -> this.down = requester }
      }
      .onFocusChanged {
        if (it.hasFocus) onFocusTargetChanged(focusTarget)
      }
      .testTag("player-controller-${focusTarget.name}"),
  )
}

@Composable
private fun PlayerSeekControl(
  uiState: PlayerUiState,
  onTogglePlayPause: () -> Unit,
  onSeekForward: () -> Unit,
  onSeekBack: () -> Unit,
  onFocused: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isFocused by interactionSource.collectIsFocusedAsState()

  Surface(
    onClick = onTogglePlayPause,
    modifier = modifier
      .fillMaxWidth()
      .height(PlayerControllerDefaults.SeekControlHeight)
      .onFocusChanged { if (it.hasFocus) onFocused() }
      .onPreviewKeyEvent { event ->
        if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

        when (event.key) {
          Key.DirectionLeft, Key.MediaRewind -> onSeekBack()
          Key.DirectionRight, Key.MediaFastForward -> onSeekForward()
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
  content: (@Composable BoxScope.() -> Unit)? = null,
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
    if (content != null) {
      content()
    } else {
      Icon(
        imageVector = ImageVector.vectorResource(iconResId),
        contentDescription = contentDescription,
        modifier = Modifier
          .align(Alignment.Center)
          .size(PlayerControllerDefaults.ActionIconSize),
        tint = LocalContentColor.current,
      )
    }
  }
}

private fun PlayerUiState.isTargetAvailable(target: PlayerControllerFocusTarget): Boolean = when (target) {
  PlayerControllerFocusTarget.SettingButton -> settings.isAvailable

  PlayerControllerFocusTarget.Progress -> isSeekable

  PlayerControllerFocusTarget.Title,
  PlayerControllerFocusTarget.LikeButton,
  PlayerControllerFocusTarget.SaveButton,
  PlayerControllerFocusTarget.CommentButton,
  -> true
}

private fun PlayerUiState.defaultControllerFocusTarget(): PlayerControllerFocusTarget =
  if (isSeekable) PlayerControllerFocusTarget.Progress else PlayerControllerFocusTarget.Title

private fun PlayerUiState.lastActionFocusTarget(): PlayerControllerFocusTarget =
  if (settings.isAvailable) PlayerControllerFocusTarget.SettingButton else PlayerControllerFocusTarget.CommentButton
