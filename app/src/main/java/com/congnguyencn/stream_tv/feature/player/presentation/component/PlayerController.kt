package com.congnguyencn.stream_tv.feature.player.presentation.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.player.presentation.PlayerUiState
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerDetailsUiState
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerMetadataUiState
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSeekPreviewUiState
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

  /** How long after the last seek the frame strip stays up before the title row comes back. */
  const val ScrubIdleMillis = 1_600L
  const val GradientStartStop = 0.36f
  const val ChromeFadeMillis = 180

  @Stable
  val HorizontalPadding: Dp = 54.dp

  @Stable
  val BottomPadding: Dp = 52.dp

  @Stable
  val ContentSpacing: Dp = 10.dp

  @Stable
  val TitleHorizontalOffset: Dp = (-10).dp

  @Stable
  val TitleWidth: Dp = 380.dp

  @Stable
  val TitleHeight: Dp = 72.dp
}

/** Full-screen controller chrome with the same focus targets as the reference TV player. */
@Composable
@Suppress("LongParameterList", "LongMethod")
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
  var isSeekBarFocused by remember { mutableStateOf(false) }
  var seekKey by remember { mutableIntStateOf(0) }
  var isScrubbing by remember { mutableStateOf(false) }
  val resolvedFocusTarget = focusTarget.takeIf { target -> uiState.isTargetAvailable(target) }
    ?: uiState.defaultControllerFocusTarget()

  LaunchedEffect(Unit) {
    delay(PlayerControllerDefaults.ActivationGuardMillis)
    isActivationEnabled = true
  }

  LaunchedEffect(resolvedFocusTarget, uiState.isSeekable, uiState.settings.isAvailable) {
    // Let the key event that revealed the controller finish before moving focus. Without this
    // second frame, a center KeyUp can land on Title and open Metadata immediately.
    withFrameMillis { }
    withFrameMillis { }
    focusRequesters[resolvedFocusTarget]?.requestFocus()
  }

  // The frame strip stands in for the title row while the viewer scrubs and stands down once they
  // stop. Showing it the whole time the seek bar merely holds focus would hide the title for no
  // reason, since the seek bar is where focus lands by default.
  LaunchedEffect(seekKey) {
    if (seekKey == 0) return@LaunchedEffect
    isScrubbing = true
    delay(PlayerControllerDefaults.ScrubIdleMillis)
    isScrubbing = false
  }

  val isFramePreviewVisible = isScrubbing && isSeekBarFocused && uiState.details.seekPreview.isAvailable
  val chromeAlpha by animateFloatAsState(
    targetValue = if (isFramePreviewVisible) 0f else 1f,
    animationSpec = tween(durationMillis = PlayerControllerDefaults.ChromeFadeMillis),
    label = "PlayerControllerChromeAlpha",
  )

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
      Box(modifier = Modifier.fillMaxWidth()) {
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
          modifier = Modifier.alpha(chromeAlpha),
        )

        PlayerSeekPreviewLane(
          seekPreview = uiState.details.seekPreview,
          progressFraction = uiState.progressFraction,
          isVisible = isFramePreviewVisible,
          modifier = Modifier.align(Alignment.BottomStart),
        )
      }

      if (uiState.isSeekable) {
        PlayerSeekBar(
          uiState = uiState,
          onTogglePlayPause = {
            if (isActivationEnabled) {
              onInteraction()
              onTogglePlayPause()
            }
          },
          onSeekForward = {
            onInteraction()
            seekKey++
            onSeekForward()
          },
          onSeekBack = {
            onInteraction()
            seekKey++
            onSeekBack()
          },
          onFocusChanged = { hasFocus ->
            isSeekBarFocused = hasFocus
            if (hasFocus) {
              onFocusTargetChanged(PlayerControllerFocusTarget.Progress)
            } else {
              isScrubbing = false
            }
          },
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
  modifier: Modifier = Modifier,
) {
  val progressRequester = focusRequesters.getValue(PlayerControllerFocusTarget.Progress)
  val titleRequester = focusRequesters.getValue(PlayerControllerFocusTarget.Title)
  val likeRequester = focusRequesters.getValue(PlayerControllerFocusTarget.LikeButton)

  Row(
    modifier = modifier.fillMaxWidth(),
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
        .onFocusChanged { focusState ->
          if (focusState.hasFocus) onFocusTargetChanged(PlayerControllerFocusTarget.Title)
        }
        .testTag("player-controller-title"),
    )

    PlayerControllerActions(
      uiState = uiState,
      focusRequesters = focusRequesters,
      onFocusTargetChanged = onFocusTargetChanged,
      onLikeClick = {
        onInteraction()
        onLikeClick()
      },
      onSaveClick = {
        onInteraction()
        onSaveClick()
      },
      onCommentClick = {
        onInteraction()
        onCommentClick()
      },
      onSettingsClick = {
        onInteraction()
        onSettingsClick()
      },
    )
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

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF102838)
@Composable
private fun PlayerControllerPreview() {
  val requesters = remember {
    PlayerControllerFocusTarget.entries.associateWith { FocusRequester() }
  }
  StreamTvTheme {
    PlayerController(
      uiState = playerControllerPreviewUiState(),
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

/** Shared by the previews of every piece the controller is assembled from. */
internal fun playerControllerPreviewUiState(): PlayerUiState = PlayerUiState.Initial.copy(
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
    seekPreview = PlayerSeekPreviewUiState(frameUrls = List(size = 8) { "" }),
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
