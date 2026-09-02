package com.congnguyencn.stream_tv.feature.player.presentation.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.player.presentation.PlayerUiState

internal object PlayerControllerActionDefaults {
  @Stable
  val ButtonSize: Dp = 36.dp

  @Stable
  val IconSize: Dp = 18.dp

  /** Tight enough that the cluster reads as one control tucked into the corner, not four buttons. */
  @Stable
  val Spacing: Dp = 10.dp

  @Stable
  val LabelOffset: Dp = 30.dp

  @Stable
  val LabelShape: Shape = RoundedCornerShape(6.dp)

  const val LabelFadeMillis = 140
}

/**
 * The like / save / comment / settings cluster, right-aligned above the seek bar.
 *
 * Deliberately small and low-contrast: these are secondary to playback, and on a 10-foot screen a
 * row of large buttons competes with the video for attention. The focused button is the only one
 * that goes bright.
 */
@Composable
@Suppress("LongParameterList")
internal fun PlayerControllerActions(
  uiState: PlayerUiState,
  focusRequesters: Map<PlayerControllerFocusTarget, FocusRequester>,
  onFocusTargetChanged: (PlayerControllerFocusTarget) -> Unit,
  onLikeClick: () -> Unit,
  onSaveClick: () -> Unit,
  onCommentClick: () -> Unit,
  onSettingsClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val progressRequester = focusRequesters.getValue(PlayerControllerFocusTarget.Progress)
  val titleRequester = focusRequesters.getValue(PlayerControllerFocusTarget.Title)
  val likeRequester = focusRequesters.getValue(PlayerControllerFocusTarget.LikeButton)
  val saveRequester = focusRequesters.getValue(PlayerControllerFocusTarget.SaveButton)
  val commentRequester = focusRequesters.getValue(PlayerControllerFocusTarget.CommentButton)
  val settingRequester = focusRequesters.getValue(PlayerControllerFocusTarget.SettingButton)
  val seekRequester = progressRequester.takeIf { uiState.isSeekable }

  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(PlayerControllerActionDefaults.Spacing),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    PlayerControllerActionButton(
      iconResId = if (uiState.isLiked) R.drawable.ic_heart else R.drawable.ic_heart_outline,
      contentDescription = stringResource(R.string.player_like),
      focusTarget = PlayerControllerFocusTarget.LikeButton,
      focusRequester = likeRequester,
      left = titleRequester,
      right = saveRequester,
      down = seekRequester,
      onFocusTargetChanged = onFocusTargetChanged,
      onClick = onLikeClick,
    )
    PlayerControllerActionButton(
      iconResId = if (uiState.isSaved) R.drawable.ic_bookmark else R.drawable.ic_bookmark_outline,
      contentDescription = stringResource(R.string.player_save),
      focusTarget = PlayerControllerFocusTarget.SaveButton,
      focusRequester = saveRequester,
      left = likeRequester,
      right = commentRequester,
      down = seekRequester,
      onFocusTargetChanged = onFocusTargetChanged,
      onClick = onSaveClick,
    )
    PlayerControllerActionButton(
      iconResId = R.drawable.ic_player_comment,
      contentDescription = stringResource(R.string.player_comments),
      focusTarget = PlayerControllerFocusTarget.CommentButton,
      focusRequester = commentRequester,
      left = saveRequester,
      right = settingRequester.takeIf { uiState.settings.isAvailable },
      down = seekRequester,
      onFocusTargetChanged = onFocusTargetChanged,
      onClick = onCommentClick,
    )
    if (uiState.settings.isAvailable) {
      PlayerControllerActionButton(
        iconResId = R.drawable.ic_settings,
        contentDescription = stringResource(R.string.player_settings),
        focusTarget = PlayerControllerFocusTarget.SettingButton,
        focusRequester = settingRequester,
        left = commentRequester,
        right = null,
        down = seekRequester,
        onFocusTargetChanged = onFocusTargetChanged,
        onClick = onSettingsClick,
      )
    }
  }
}

/**
 * One circular action.
 *
 * The label floats above the circle with `wrapContentSize(unbounded = true)` instead of sitting in a
 * reserved row: reserving space for a label only one button ever shows made the whole cluster twice
 * as tall as the buttons in it.
 */
@Composable
@Suppress("LongParameterList")
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
  var isFocused by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier.size(PlayerControllerActionDefaults.ButtonSize),
    contentAlignment = Alignment.Center,
  ) {
    PlayerActionLabel(
      text = contentDescription,
      isVisible = isFocused,
      modifier = Modifier.align(Alignment.TopCenter),
    )
    PlayerRoundIconButton(
      iconResId = iconResId,
      contentDescription = contentDescription,
      onClick = onClick,
      iconSize = PlayerControllerActionDefaults.IconSize,
      modifier = Modifier
        .fillMaxSize()
        .focusRequester(focusRequester)
        .focusProperties {
          this.left = left ?: FocusRequester.Cancel
          this.right = right ?: FocusRequester.Cancel
          this.up = FocusRequester.Cancel
          this.down = down ?: FocusRequester.Cancel
        }
        .onFocusChanged { focusState ->
          isFocused = focusState.hasFocus
          if (focusState.hasFocus) onFocusTargetChanged(focusTarget)
        }
        .testTag("player-controller-${focusTarget.name}"),
    )
  }
}

@Composable
private fun PlayerActionLabel(text: String, isVisible: Boolean, modifier: Modifier = Modifier) {
  AnimatedVisibility(
    visible = isVisible,
    modifier = modifier,
    enter = fadeIn(animationSpec = tween(PlayerControllerActionDefaults.LabelFadeMillis)),
    exit = fadeOut(animationSpec = tween(PlayerControllerActionDefaults.LabelFadeMillis)),
  ) {
    Text(
      text = text,
      modifier = Modifier
        .offset(y = -PlayerControllerActionDefaults.LabelOffset)
        .wrapContentSize(unbounded = true)
        .background(StreamTvColors.TransparentBlack80, PlayerControllerActionDefaults.LabelShape)
        .padding(horizontal = 8.dp, vertical = 3.dp),
      color = StreamTvColors.NeutralWhite,
      style = StreamTvTheme.typography.labelMedium.copy(fontSize = 11.sp),
      textAlign = TextAlign.Center,
      maxLines = 1,
    )
  }
}

@Composable
internal fun PlayerRoundIconButton(
  @DrawableRes iconResId: Int,
  contentDescription: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  iconSize: Dp = PlayerControllerActionDefaults.IconSize,
  content: (@Composable BoxScope.() -> Unit)? = null,
) {
  Surface(
    onClick = onClick,
    modifier = modifier,
    shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
    scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
    colors = ClickableSurfaceDefaults.colors(
      containerColor = StreamTvColors.TransparentWhite10,
      contentColor = StreamTvColors.Neutral10,
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
          .size(iconSize),
        tint = LocalContentColor.current,
      )
    }
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF102838)
@Composable
private fun PlayerControllerActionsPreview() {
  StreamTvTheme {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(54.dp),
      contentAlignment = Alignment.BottomEnd,
    ) {
      PlayerControllerActions(
        uiState = playerControllerPreviewUiState().copy(isLiked = true),
        focusRequesters = remember {
          PlayerControllerFocusTarget.entries.associateWith { FocusRequester() }
        },
        onFocusTargetChanged = {},
        onLikeClick = {},
        onSaveClick = {},
        onCommentClick = {},
        onSettingsClick = {},
      )
    }
  }
}
