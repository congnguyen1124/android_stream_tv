package com.congnguyencn.stream_tv.feature.player.presentation.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

private object PlayerControllerDefaults {
  /**
   * How long after the controller appears a click is ignored.
   *
   * The key press that reveals the controller is still travelling: without this guard its KeyUp
   * lands on whichever control just took focus and immediately triggers it.
   */
  const val ActivationGuardMillis = 120L

  /** How long after the last seek the frame strip stays up before the title comes back. */
  const val ScrubIdleMillis = 1_600L
  const val ChromeFadeMillis = 180
  const val TopScrimStop = 0.42f
  const val BottomScrimStop = 0.52f

  @Stable
  val HorizontalPadding: Dp = 54.dp

  @Stable
  val TopPadding: Dp = 44.dp

  @Stable
  val BottomPadding: Dp = 34.dp

  @Stable
  val TitleWidth: Dp = 760.dp

  @Stable
  val SeekBarToControlsSpacing: Dp = 4.dp
}

/**
 * Full-screen controller chrome.
 *
 * Laid out top-and-bottom rather than as one bottom stack: the title block sits in the upper-left
 * where nothing else competes with it, and everything interactive collects along the bottom edge —
 * seek bar first, then the control row. That ordering is what makes the vertical D-pad axis mean
 * something, because Up from any control is always "go scrub" and there is nothing below the row.
 *
 * @param focusTarget The control to focus when this subtree appears. Survives the controller being
 *   destroyed while a section is open, so closing a section returns focus to the button that opened
 *   it.
 */
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
  onDescriptionClick: () -> Unit,
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
  val controlRowRequester = remember { FocusRequester() }
  // The control to come back to when the viewer moves down off the seek bar.
  //
  // Tracked here rather than through `saveFocusedChild` / `restoreFocusedChild`: those store the
  // saved child on the group's focus node, and it did not survive focus leaving the group by an
  // explicit `FocusRequester` jump — the restore always reported nothing saved. This is the same
  // information, held where it cannot be cleared underneath us.
  var lastControlTarget by remember { mutableStateOf(uiState.defaultControllerFocusTarget()) }
  val resolvedFocusTarget = focusTarget.takeIf(uiState::isControllerTargetAvailable)
    ?: uiState.defaultControllerFocusTarget()

  LaunchedEffect(Unit) {
    delay(PlayerControllerDefaults.ActivationGuardMillis)
    isActivationEnabled = true
  }

  // Placed once per appearance, not on every change of the live target. Keying this on the target
  // made it re-fire each time focus moved inside the row — every move updates the target, which
  // re-ran the effect, which re-requested focus — and that fought the row's restorer into a loop
  // that also kept resetting the auto-hide timer.
  val entryFocusTarget = remember { resolvedFocusTarget }
  LaunchedEffect(Unit) {
    // Two frames, not one: the first lets this subtree finish entering, the second lets the reveal
    // key's KeyUp drain. Requesting focus earlier lands it on a control that is still measuring.
    withFrameMillis { }
    withFrameMillis { }
    focusRequesters[entryFocusTarget]?.requestFocus()
  }

  // The frame strip stands in for the title while the viewer scrubs and stands down once they stop.
  // Showing it for as long as the seek bar merely holds focus would hide the title for no reason,
  // since the seek bar is one Up press away from every control.
  LaunchedEffect(seekKey) {
    if (seekKey == 0) return@LaunchedEffect
    isScrubbing = true
    delay(PlayerControllerDefaults.ScrubIdleMillis)
    isScrubbing = false
  }

  val isFramePreviewVisible = isScrubbing && isSeekBarFocused && uiState.details.seekPreview.isAvailable
  val titleAlpha by animateFloatAsState(
    targetValue = if (isFramePreviewVisible) 0f else 1f,
    animationSpec = tween(durationMillis = PlayerControllerDefaults.ChromeFadeMillis),
    label = "PlayerControllerTitleAlpha",
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colorStops = arrayOf(
            0f to StreamTvColors.TransparentBlack60,
            PlayerControllerDefaults.TopScrimStop to StreamTvColors.Transparent,
            PlayerControllerDefaults.BottomScrimStop to StreamTvColors.Transparent,
            1f to StreamTvColors.TransparentBlack80,
          ),
        ),
      )
      .focusGroup()
      .testTag("player-controller"),
  ) {
    PlayerControllerTitleBlock(
      uiState = uiState,
      modifier = Modifier
        .align(Alignment.TopStart)
        .padding(
          start = PlayerControllerDefaults.HorizontalPadding,
          top = PlayerControllerDefaults.TopPadding,
          end = PlayerControllerDefaults.HorizontalPadding,
        )
        .width(PlayerControllerDefaults.TitleWidth)
        .alpha(titleAlpha),
    )

    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .padding(
          start = PlayerControllerDefaults.HorizontalPadding,
          end = PlayerControllerDefaults.HorizontalPadding,
          bottom = PlayerControllerDefaults.BottomPadding,
        ),
      verticalArrangement = Arrangement.spacedBy(PlayerControllerDefaults.SeekBarToControlsSpacing),
    ) {
      PlayerSeekPreviewLane(
        seekPreview = uiState.details.seekPreview,
        progressFraction = uiState.progressFraction,
        isVisible = isFramePreviewVisible,
      )

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
          onMoveDown = { focusRequesters.getValue(lastControlTarget).requestFocus() },
          modifier = Modifier
            .focusRequester(focusRequesters.getValue(PlayerControllerFocusTarget.Progress))
            .focusProperties { up = FocusRequester.Cancel },
        )
      } else {
        PlayerLiveTimeRow(uiState = uiState)
      }

      PlayerControlRow(
        uiState = uiState,
        focusRequesters = focusRequesters,
        focusRowRequester = controlRowRequester,
        onMoveUp = if (uiState.isSeekable) {
          { focusRequesters.getValue(PlayerControllerFocusTarget.Progress).requestFocus() }
        } else {
          null
        },
        onFocusTargetChanged = { target ->
          lastControlTarget = target
          onFocusTargetChanged(target)
        },
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
        onDescriptionClick = { if (isActivationEnabled) onDescriptionClick() },
        onLikeClick = {
          onInteraction()
          if (isActivationEnabled) onLikeClick()
        },
        onSaveClick = {
          onInteraction()
          if (isActivationEnabled) onSaveClick()
        },
        onCommentClick = { if (isActivationEnabled) onCommentClick() },
        onSettingsClick = { if (isActivationEnabled) onSettingsClick() },
      )
    }
  }
}

/**
 * Title, then collection and release line. Not focusable.
 *
 * The metadata section is reached from the Description pill in the control row instead, which keeps
 * the whole interactive surface on one horizontal band along the bottom.
 */
@Composable
private fun PlayerControllerTitleBlock(uiState: PlayerUiState, modifier: Modifier = Modifier) {
  val subtitle = listOf(
    uiState.details.metadata.collectionTitle,
    uiState.details.metadata.releaseYear,
  ).filter(String::isNotBlank).joinToString(separator = " • ")

  Column(modifier = modifier.testTag("player-controller-title")) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      if (!uiState.isSeekable) {
        PlayerLiveBadge()
        Spacer(modifier = Modifier.width(10.dp))
      }
      Text(
        text = uiState.title,
        color = StreamTvColors.NeutralWhite,
        style = StreamTvTheme.typography.headlineLarge.copy(fontSize = 30.sp, lineHeight = 38.sp),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
    }
    if (subtitle.isNotBlank()) {
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = subtitle,
        color = StreamTvColors.Neutral20,
        style = StreamTvTheme.typography.labelMedium.copy(fontSize = 14.sp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

/** Stands in for the seek bar on a live stream, which has no duration to scrub through. */
@Composable
private fun PlayerLiveTimeRow(uiState: PlayerUiState, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(PlayerSeekBarDefaults.ControlHeight),
    contentAlignment = Alignment.CenterStart,
  ) {
    PlayerTimeLabel(position = uiState.position, duration = uiState.duration)
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF102838)
@Composable
private fun PlayerControllerPreview() {
  val requesters = remember {
    PlayerControllerFocusTarget.entries.associateWith { FocusRequester() }
  }
  StreamTvTheme {
    PlayerController(
      uiState = playerControllerPreviewUiState(),
      focusTarget = PlayerControllerFocusTarget.PlayPause,
      focusRequesters = requesters,
      onFocusTargetChanged = {},
      onInteraction = {},
      onTogglePlayPause = {},
      onSeekForward = {},
      onSeekBack = {},
      onDescriptionClick = {},
      onLikeClick = {},
      onSaveClick = {},
      onCommentClick = {},
      onSettingsClick = {},
    )
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF102838)
@Composable
private fun PlayerControllerLivePreview() {
  val requesters = remember {
    PlayerControllerFocusTarget.entries.associateWith { FocusRequester() }
  }
  StreamTvTheme {
    PlayerController(
      uiState = playerControllerPreviewUiState().copy(duration = kotlin.time.Duration.ZERO),
      focusTarget = PlayerControllerFocusTarget.PlayPause,
      focusRequesters = requesters,
      onFocusTargetChanged = {},
      onInteraction = {},
      onTogglePlayPause = {},
      onSeekForward = {},
      onSeekBack = {},
      onDescriptionClick = {},
      onLikeClick = {},
      onSaveClick = {},
      onCommentClick = {},
      onSettingsClick = {},
    )
  }
}

/** Shared by the previews of every piece the controller is assembled from. */
internal fun playerControllerPreviewUiState(): PlayerUiState = PlayerUiState.Initial.copy(
  title = "Relaxing Music with Soft Rain Sounds - Deep Sleep Instantly In 10 Minutes",
  isPlaying = true,
  position = 91.seconds,
  duration = 1_970.seconds,
  bufferedPosition = 154.seconds,
  details = PlayerDetailsUiState.Empty.copy(
    metadata = PlayerMetadataUiState.Empty.copy(
      collectionTitle = "Peaceful Rainfall Music",
      releaseYear = "1 year ago",
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
