package com.congnguyencn.stream_tv.feature.player.presentation.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.player.presentation.PlayerUiState

internal object PlayerControlRowDefaults {
  /** Tall enough to hold a 44dp button plus the caption that appears under the focused one. */
  @Stable
  val RowHeight: Dp = 84.dp

  @Stable
  val ClusterSpacing: Dp = 8.dp

  @Stable
  val TransportSpacing: Dp = 14.dp

  @Stable
  val PillShape: Shape = CircleShape

  @Stable
  val PillHeight: Dp = 44.dp

  @Stable
  val PillHorizontalPadding: Dp = 20.dp

  /** How far below the button's own bottom edge the caption is pushed. */
  @Stable
  val LabelOffset: Dp = 22.dp

  const val LabelFadeMillis = 120
}

/**
 * The control row, below the seek bar.
 *
 * Three clusters in a `Box` rather than one `Row` with weights: the transport group has to sit dead
 * centre on the panel regardless of how wide the leading and trailing clusters are, and a weighted
 * row centres the *gap* between them instead.
 *
 * The row is one focus group whose focused child is saved on the way up and restored on the way
 * down, so Down from the seek bar lands on the control the viewer last used rather than resetting to
 * the first one.
 *
 * Restoration is driven by the controller, which remembers the last focused control. Neither
 * `Modifier.focusRestorer` nor `FocusRequester.saveFocusedChild` served here: both key off the
 * group's focus-search enter and exit, and this row is left and re-entered by explicit
 * `FocusRequester` jumps, which bypass those hooks — the restore always reported nothing saved.
 *
 * @param focusRowRequester Attached to the row itself, so the controller can address it as a unit.
 * @param onMoveUp Null on a live stream, where there is no seek bar above to move to.
 */
@Composable
@Suppress("LongParameterList")
internal fun PlayerControlRow(
  uiState: PlayerUiState,
  focusRequesters: Map<PlayerControllerFocusTarget, FocusRequester>,
  focusRowRequester: FocusRequester,
  onMoveUp: (() -> Unit)?,
  onFocusTargetChanged: (PlayerControllerFocusTarget) -> Unit,
  onTogglePlayPause: () -> Unit,
  onSeekForward: () -> Unit,
  onSeekBack: () -> Unit,
  onDescriptionClick: () -> Unit,
  onLikeClick: () -> Unit,
  onSaveClick: () -> Unit,
  onCommentClick: () -> Unit,
  onSettingsClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(PlayerControlRowDefaults.RowHeight)
      .focusRequester(focusRowRequester)
      .focusGroup()
      // Handled for the whole row rather than per button: every control's Up means the same thing.
      .onPreviewKeyEvent { event ->
        if (event.type == KeyEventType.KeyDown && event.key == Key.DirectionUp && onMoveUp != null) {
          onMoveUp()
          true
        } else {
          false
        }
      }
      .testTag("player-control-row"),
  ) {
    PlayerDescriptionPill(
      focusRequesters = focusRequesters,
      onFocusTargetChanged = onFocusTargetChanged,
      onClick = onDescriptionClick,
      modifier = Modifier.align(Alignment.CenterStart),
    )

    PlayerTransportCluster(
      uiState = uiState,
      focusRequesters = focusRequesters,
      onFocusTargetChanged = onFocusTargetChanged,
      onTogglePlayPause = onTogglePlayPause,
      onSeekForward = onSeekForward,
      onSeekBack = onSeekBack,
      modifier = Modifier.align(Alignment.Center),
    )

    PlayerActionCluster(
      uiState = uiState,
      focusRequesters = focusRequesters,
      onFocusTargetChanged = onFocusTargetChanged,
      onLikeClick = onLikeClick,
      onSaveClick = onSaveClick,
      onCommentClick = onCommentClick,
      onSettingsClick = onSettingsClick,
      modifier = Modifier.align(Alignment.CenterEnd),
    )
  }
}

/** The metadata entry point, shaped as a pill so it reads as a label rather than an icon. */
@Composable
private fun PlayerDescriptionPill(
  focusRequesters: Map<PlayerControllerFocusTarget, FocusRequester>,
  onFocusTargetChanged: (PlayerControllerFocusTarget) -> Unit,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    onClick = onClick,
    modifier = modifier
      .height(PlayerControlRowDefaults.PillHeight)
      .focusRequester(focusRequesters.getValue(PlayerControllerFocusTarget.Description))
      .playerControlFocusProperties(
        focusRequesters = focusRequesters,
        left = null,
        right = PlayerControllerFocusTarget.Rewind,
      )
      .onFocusChanged { focusState ->
        if (focusState.hasFocus) onFocusTargetChanged(PlayerControllerFocusTarget.Description)
      }
      .testTag("player-control-Description"),
    shape = ClickableSurfaceDefaults.shape(shape = PlayerControlRowDefaults.PillShape),
    scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
    colors = ClickableSurfaceDefaults.colors(
      containerColor = StreamTvColors.TransparentWhite10,
      contentColor = StreamTvColors.NeutralWhite,
      focusedContainerColor = StreamTvColors.NeutralWhite,
      focusedContentColor = StreamTvColors.NeutralBlack,
      pressedContainerColor = StreamTvColors.Primary30,
      pressedContentColor = StreamTvColors.NeutralBlack,
    ),
  ) {
    Box(
      modifier = Modifier
        .fillMaxHeight()
        .padding(horizontal = PlayerControlRowDefaults.PillHorizontalPadding),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = stringResource(R.string.player_description),
        color = LocalContentColor.current,
        style = StreamTvTheme.typography.labelMedium.copy(fontSize = 14.sp),
        maxLines = 1,
      )
    }
  }
}

/**
 * Rewind, play/pause, forward.
 *
 * The reference UI puts previous/next episode here; with no playlist to step through, the same three
 * slots carry the seek increments instead, so the shape is familiar and every button does something.
 */
@Composable
@Suppress("LongParameterList")
private fun PlayerTransportCluster(
  uiState: PlayerUiState,
  focusRequesters: Map<PlayerControllerFocusTarget, FocusRequester>,
  onFocusTargetChanged: (PlayerControllerFocusTarget) -> Unit,
  onTogglePlayPause: () -> Unit,
  onSeekForward: () -> Unit,
  onSeekBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.focusGroup(),
    horizontalArrangement = Arrangement.spacedBy(PlayerControlRowDefaults.TransportSpacing),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    if (uiState.isSeekable) {
      PlayerControlButton(
        iconResId = R.drawable.ic_replay_10,
        contentDescription = stringResource(R.string.player_rewind),
        label = stringResource(R.string.player_rewind),
        target = PlayerControllerFocusTarget.Rewind,
        focusRequesters = focusRequesters,
        left = PlayerControllerFocusTarget.Description,
        right = PlayerControllerFocusTarget.PlayPause,
        onFocusTargetChanged = onFocusTargetChanged,
        onClick = onSeekBack,
      )
    }

    PlayerControlButton(
      iconResId = if (uiState.isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
      contentDescription = stringResource(
        if (uiState.isPlaying) R.string.player_pause else R.string.player_play,
      ),
      // No caption: a play/pause glyph needs none, and the reference UI omits it here too.
      label = null,
      target = PlayerControllerFocusTarget.PlayPause,
      focusRequesters = focusRequesters,
      left = if (uiState.isSeekable) PlayerControllerFocusTarget.Rewind else PlayerControllerFocusTarget.Description,
      right = if (uiState.isSeekable) PlayerControllerFocusTarget.Forward else PlayerControllerFocusTarget.Like,
      onFocusTargetChanged = onFocusTargetChanged,
      onClick = onTogglePlayPause,
      size = PlayerIconButtonDefaults.PrimarySize,
      iconSize = PlayerIconButtonDefaults.PrimaryIconSize,
    )

    if (uiState.isSeekable) {
      PlayerControlButton(
        iconResId = R.drawable.ic_forward_10,
        contentDescription = stringResource(R.string.player_forward),
        label = stringResource(R.string.player_forward),
        target = PlayerControllerFocusTarget.Forward,
        focusRequesters = focusRequesters,
        left = PlayerControllerFocusTarget.PlayPause,
        right = PlayerControllerFocusTarget.Like,
        onFocusTargetChanged = onFocusTargetChanged,
        onClick = onSeekForward,
      )
    }
  }
}

/**
 * One entry of the trailing cluster.
 *
 * A resolved value rather than a `when` over the focus target: the target enum spans the whole row,
 * so keying icons and callbacks off it forced branches for controls that can never appear here.
 */
private data class PlayerActionUiItem(
  val target: PlayerControllerFocusTarget,
  @DrawableRes val iconResId: Int,
  @StringRes val labelResId: Int,
  val onClick: () -> Unit,
)

/**
 * Like / save / comment on one shared pill, then settings on its own circle.
 *
 * A `LazyRow` because the cluster is data-driven and will grow, and because a lazy row is itself a
 * focus group whose saved child survives the row leaving composition — which is what
 * [PlayerControlRow]'s restorer needs when a section closes and the controller is rebuilt.
 */
@Composable
@Suppress("LongParameterList")
private fun PlayerActionCluster(
  uiState: PlayerUiState,
  focusRequesters: Map<PlayerControllerFocusTarget, FocusRequester>,
  onFocusTargetChanged: (PlayerControllerFocusTarget) -> Unit,
  onLikeClick: () -> Unit,
  onSaveClick: () -> Unit,
  onCommentClick: () -> Unit,
  onSettingsClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val actions = listOf(
    PlayerActionUiItem(
      target = PlayerControllerFocusTarget.Like,
      iconResId = if (uiState.isLiked) R.drawable.ic_heart else R.drawable.ic_heart_outline,
      labelResId = R.string.player_like,
      onClick = onLikeClick,
    ),
    PlayerActionUiItem(
      target = PlayerControllerFocusTarget.Save,
      iconResId = if (uiState.isSaved) R.drawable.ic_bookmark else R.drawable.ic_bookmark_outline,
      labelResId = R.string.player_save,
      onClick = onSaveClick,
    ),
    PlayerActionUiItem(
      target = PlayerControllerFocusTarget.Comment,
      iconResId = R.drawable.ic_player_comment,
      labelResId = R.string.player_comments,
      onClick = onCommentClick,
    ),
  )

  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(PlayerControlRowDefaults.ClusterSpacing),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(contentAlignment = Alignment.Center) {
      // Sized from the lazy row beside it, then inset vertically so the pill stays button-height
      // while the row itself is tall enough for a caption.
      Box(
        modifier = Modifier
          .matchParentSize()
          .padding(
            vertical = (PlayerControlRowDefaults.RowHeight - PlayerControlRowDefaults.PillHeight) / 2,
          )
          .background(StreamTvColors.TransparentWhite10, PlayerControlRowDefaults.PillShape),
      )

      LazyRow(
        modifier = Modifier
          .height(PlayerControlRowDefaults.RowHeight)
          .focusGroup()
          .testTag("player-action-cluster"),
        contentPadding = PaddingValues(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        itemsIndexed(items = actions, key = { _, action -> action.target.name }) { index, action ->
          PlayerControlButton(
            iconResId = action.iconResId,
            contentDescription = stringResource(action.labelResId),
            label = stringResource(action.labelResId),
            target = action.target,
            focusRequesters = focusRequesters,
            left = actions.getOrNull(index - 1)?.target ?: uiState.actionClusterEntryTarget(),
            right = actions.getOrNull(index + 1)?.target
              ?: PlayerControllerFocusTarget.Settings.takeIf { uiState.settings.isAvailable },
            onFocusTargetChanged = onFocusTargetChanged,
            onClick = action.onClick,
            // Transparent so the cluster's shared pill shows through until it takes focus.
            containerColor = StreamTvColors.Transparent,
          )
        }
      }
    }

    if (uiState.settings.isAvailable) {
      PlayerControlButton(
        iconResId = R.drawable.ic_settings,
        contentDescription = stringResource(R.string.player_settings),
        label = stringResource(R.string.player_settings),
        target = PlayerControllerFocusTarget.Settings,
        focusRequesters = focusRequesters,
        left = PlayerControllerFocusTarget.Comment,
        right = null,
        onFocusTargetChanged = onFocusTargetChanged,
        onClick = onSettingsClick,
      )
    }
  }
}

/** What Left from the first action reaches: the last transport control that is actually on screen. */
private fun PlayerUiState.actionClusterEntryTarget(): PlayerControllerFocusTarget = if (isSeekable) {
  PlayerControllerFocusTarget.Forward
} else {
  PlayerControllerFocusTarget.PlayPause
}

/**
 * One control, with the caption that appears under it while focused.
 *
 * The caption is drawn `unbounded` beneath the button rather than in a reserved row: reserving a row
 * for a label that only one button ever shows made the whole control row twice as tall as the
 * buttons in it, which pushed the seek bar up off the video's lower third.
 *
 * @param label Null for the transport primary — a play/pause glyph needs no caption, and the
 *   reference UI omits it there too.
 */
@Composable
@Suppress("LongParameterList")
private fun PlayerControlButton(
  @DrawableRes iconResId: Int,
  contentDescription: String,
  label: String?,
  target: PlayerControllerFocusTarget,
  focusRequesters: Map<PlayerControllerFocusTarget, FocusRequester>,
  left: PlayerControllerFocusTarget?,
  right: PlayerControllerFocusTarget?,
  onFocusTargetChanged: (PlayerControllerFocusTarget) -> Unit,
  onClick: () -> Unit,
  size: Dp = PlayerIconButtonDefaults.Size,
  iconSize: Dp = PlayerIconButtonDefaults.IconSize,
  containerColor: Color = StreamTvColors.TransparentWhite10,
) {
  var isFocused by remember { mutableStateOf(false) }

  Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
    PlayerIconButton(
      iconResId = iconResId,
      contentDescription = contentDescription,
      onClick = onClick,
      isFocused = isFocused,
      modifier = Modifier
        .size(size)
        .focusRequester(focusRequesters.getValue(target))
        .playerControlFocusProperties(
          focusRequesters = focusRequesters,
          left = left,
          right = right,
        )
        // `isFocused`, not `hasFocus`: hasFocus also reports true while a descendant holds focus,
        // and it stayed true after focus moved to the seek bar — leaving the button painted white
        // while another control actually had the D-pad.
        .onFocusChanged { focusState ->
          isFocused = focusState.isFocused
          if (focusState.isFocused) onFocusTargetChanged(target)
        }
        .testTag("player-control-${target.name}"),
      iconSize = iconSize,
      containerColor = containerColor,
    )

    if (label != null) {
      PlayerControlLabel(
        text = label,
        isVisible = isFocused,
        modifier = Modifier.align(Alignment.BottomCenter),
      )
    }
  }
}

@Composable
private fun PlayerControlLabel(text: String, isVisible: Boolean, modifier: Modifier = Modifier) {
  AnimatedVisibility(
    visible = isVisible,
    modifier = modifier,
    enter = fadeIn(animationSpec = tween(PlayerControlRowDefaults.LabelFadeMillis)),
    exit = fadeOut(animationSpec = tween(PlayerControlRowDefaults.LabelFadeMillis)),
  ) {
    Text(
      text = text,
      modifier = Modifier
        .offset(y = PlayerControlRowDefaults.LabelOffset)
        .wrapContentSize(unbounded = true),
      color = StreamTvColors.NeutralWhite,
      style = StreamTvTheme.typography.labelMedium.copy(fontSize = 12.sp),
      textAlign = TextAlign.Center,
      maxLines = 1,
    )
  }
}

/**
 * Wires one control's horizontal neighbours.
 *
 * Vertical is deliberately absent here: Up belongs to the row, which has to save the focused child
 * before the jump, and Down is cancelled because the row is the last thing on screen — letting it
 * fall through hands focus to the video surface, which then hides the controller still in use.
 */
private fun Modifier.playerControlFocusProperties(
  focusRequesters: Map<PlayerControllerFocusTarget, FocusRequester>,
  left: PlayerControllerFocusTarget?,
  right: PlayerControllerFocusTarget?,
): Modifier = focusProperties {
  down = FocusRequester.Cancel
  this.left = left?.let(focusRequesters::getValue) ?: FocusRequester.Cancel
  this.right = right?.let(focusRequesters::getValue) ?: FocusRequester.Cancel
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF0E1A22)
@Composable
private fun PlayerControlRowPreview() {
  StreamTvTheme {
    PlayerControlRow(
      uiState = playerControllerPreviewUiState().copy(isLiked = true),
      focusRequesters = remember {
        PlayerControllerFocusTarget.entries.associateWith { FocusRequester() }
      },
      focusRowRequester = remember { FocusRequester() },
      onMoveUp = {},
      onFocusTargetChanged = {},
      onTogglePlayPause = {},
      onSeekForward = {},
      onSeekBack = {},
      onDescriptionClick = {},
      onLikeClick = {},
      onSaveClick = {},
      onCommentClick = {},
      onSettingsClick = {},
      modifier = Modifier.padding(horizontal = 54.dp),
    )
  }
}
