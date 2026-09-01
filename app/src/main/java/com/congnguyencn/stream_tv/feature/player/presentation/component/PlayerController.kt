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
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
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
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerDetailsUiState
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerMetadataUiState
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingCategory
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingOptionUiItem
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingUiItem
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingsUiState
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

internal enum class PlayerControllerFocusTarget {
  Title,
  LikeButton,
  SaveButton,
  CommentButton,
  SettingButton,
  Progress,
}

private object PlayerControllerDefaults {
  const val ActivationGuardMillis = 120L
  const val GradientStartStop = 0.36f
  val HorizontalPadding = 54.dp
  val BottomPadding = 52.dp
  val ContentSpacing = 10.dp
  val TitleHorizontalOffset = (-10).dp
  val TitleWidth = 380.dp
  val TitleHeight = 72.dp
  val ActionButtonSize = 40.dp
  val ActionAreaWidth = 40.dp
  val ActionAreaHeight = 64.dp
  val ActionSpacing = 24.dp
  val ActionIconSize = 24.dp
  val ActionLabelHeight = 20.dp
  val SeekControlHeight = 40.dp
  val SeekTrackHeight = 20.dp
  val SeekThumbSize = 14.dp
  val SeekThumbIdleSize = 10.dp
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
  var isActivationEnabled by remember { mutableStateOf(false) }
  val resolvedFocusTarget = focusTarget.takeIf { target -> uiState.isTargetAvailable(target) }
    ?: uiState.defaultControllerFocusTarget()

  LaunchedEffect(Unit) {
    delay(PlayerControllerDefaults.ActivationGuardMillis)
    isActivationEnabled = true
  }

  LaunchedEffect(resolvedFocusTarget, uiState.isSeekable, uiState.settings.isAvailable) {
    // Let the key event that revealed the controller finish before moving focus. Without this
    // second frame, a center KeyUp can land on Title and open Metadata immediately.
    androidx.compose.runtime.withFrameMillis { }
    androidx.compose.runtime.withFrameMillis { }
    focusRequesters[resolvedFocusTarget]?.requestFocus()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colorStops = arrayOf(
            0f to StreamTvColors.Transparent,
            PlayerControllerDefaults.GradientStartStop to StreamTvColors.Transparent,
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
        onTitleClick = { if (isActivationEnabled) onTitleClick() },
        onLikeClick = { if (isActivationEnabled) onLikeClick() },
        onSaveClick = { if (isActivationEnabled) onSaveClick() },
        onCommentClick = { if (isActivationEnabled) onCommentClick() },
        onSettingsClick = { if (isActivationEnabled) onSettingsClick() },
      )

      if (uiState.isSeekable) {
        PlayerSeekControl(
          uiState = uiState,
          onTogglePlayPause = {
            if (isActivationEnabled) {
              onInteraction()
              onTogglePlayPause()
            }
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
        .offset(x = PlayerControllerDefaults.TitleHorizontalOffset)
        .width(PlayerControllerDefaults.TitleWidth)
        .height(PlayerControllerDefaults.TitleHeight)
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
  val subtitle = listOf(
    uiState.details.metadata.collectionTitle,
    uiState.details.metadata.releaseYear,
  ).filter(String::isNotBlank).joinToString(separator = " • ")

  Surface(
    onClick = onClick,
    modifier = modifier,
    shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(12.dp)),
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
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalArrangement = Arrangement.Center,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (!uiState.isSeekable) {
          PlayerLiveBadge()
          Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
          text = uiState.title,
          color = LocalContentColor.current,
          style = StreamTvTheme.typography.headlineLarge.copy(
            fontSize = 22.sp,
            lineHeight = 26.sp,
          ),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      if (subtitle.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = subtitle,
          color = LocalContentColor.current.copy(alpha = 0.72f),
          style = StreamTvTheme.typography.labelMedium.copy(
            fontSize = 12.sp,
            lineHeight = 16.sp,
          ),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
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
  var isFocused by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .width(PlayerControllerDefaults.ActionAreaWidth)
      .height(PlayerControllerDefaults.ActionAreaHeight),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(
      text = contentDescription,
      modifier = Modifier
        .requiredWidth(88.dp)
        .height(PlayerControllerDefaults.ActionLabelHeight)
        .alpha(if (isFocused) 1f else 0f),
      color = StreamTvColors.NeutralWhite,
      style = StreamTvTheme.typography.labelMedium.copy(fontSize = 12.sp),
      textAlign = TextAlign.Center,
      maxLines = 1,
    )
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
          isFocused = it.hasFocus
          if (it.hasFocus) onFocusTargetChanged(focusTarget)
        }
        .testTag("player-controller-${focusTarget.name}"),
    )
  }
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
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
    ) {
      PlayerSeekTrack(
        progressFraction = uiState.progressFraction,
        bufferedFraction = uiState.bufferedFraction,
        isFocused = isFocused,
        modifier = Modifier.fillMaxWidth(),
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = uiState.position.coerceAtMost(uiState.duration).toClockString(),
          color = StreamTvColors.Neutral10,
          style = StreamTvTheme.typography.labelMedium.copy(fontSize = 12.sp),
        )
        Text(
          text = uiState.duration.toClockString(),
          color = StreamTvColors.Neutral10,
          style = StreamTvTheme.typography.labelMedium.copy(fontSize = 12.sp),
        )
      }
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
    modifier = modifier.height(PlayerControllerDefaults.SeekTrackHeight),
    contentAlignment = Alignment.CenterStart,
  ) {
    PlayerProgressBar(
      progressFraction = progressFraction,
      bufferedFraction = bufferedFraction,
      modifier = Modifier.fillMaxWidth(),
    )
    val thumbSize = if (isFocused) {
      PlayerControllerDefaults.SeekThumbSize
    } else {
      PlayerControllerDefaults.SeekThumbIdleSize
    }
    val travel = (maxWidth - thumbSize).coerceAtLeast(0.dp)
    Box(
      modifier = Modifier
        .offset(x = travel * progressFraction)
        .size(thumbSize)
        .background(StreamTvColors.NeutralWhite, CircleShape),
    )
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

@Preview(device = Devices.TV_720p, showBackground = true, backgroundColor = 0xFF102838)
@Composable
private fun PlayerControllerPreview() {
  val requesters = remember {
    PlayerControllerFocusTarget.entries.associateWith { FocusRequester() }
  }
  StreamTvTheme {
    PlayerController(
      uiState = previewControllerUiState(),
      focusTarget = PlayerControllerFocusTarget.SettingButton,
      focusRequesters = requesters,
      onFocusTargetChanged = {},
      onInteraction = {},
      onTogglePlayPause = {},
      onSeekForward = {},
      onSeekBack = {},
      onTitleClick = {},
      onLikeClick = {},
      onSaveClick = {},
      onCommentClick = {},
      onSettingsClick = {},
    )
  }
}

private fun previewControllerUiState(): PlayerUiState = PlayerUiState.Initial.copy(
  title = "A New Era of Sports",
  isPlaying = true,
  position = 91.seconds,
  duration = 1_970.seconds,
  bufferedPosition = 154.seconds,
  details = PlayerDetailsUiState.Empty.copy(
    metadata = PlayerMetadataUiState.Empty.copy(
      collectionTitle = "Sports documentary",
      releaseYear = "1 hour ago",
    ),
  ),
  settings = PlayerSettingsUiState(
    items = listOf(
      PlayerSettingUiItem(
        category = PlayerSettingCategory.Quality,
        selectedLabel = "Full HD",
        options = listOf(
          PlayerSettingOptionUiItem(id = "1080", label = "1080p", isSelected = true),
        ),
      ),
    ),
  ),
)
