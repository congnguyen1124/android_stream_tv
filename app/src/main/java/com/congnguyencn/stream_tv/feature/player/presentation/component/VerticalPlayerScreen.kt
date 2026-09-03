package com.congnguyencn.stream_tv.feature.player.presentation.component

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.player.presentation.PlayerUiState
import com.congnguyencn.stream_tv.feature.player.presentation.component.section.PlayerPendingFocusTarget
import com.congnguyencn.stream_tv.feature.player.presentation.component.section.PlayerSection
import com.congnguyencn.stream_tv.feature.player.presentation.component.section.PlayerSideSection
import com.congnguyencn.stream_tv.feature.player.presentation.component.section.awaitPlayerSectionFrame
import com.congnguyencn.stream_tv.feature.player.presentation.component.section.rememberPlayerSectionNavigationState
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerDetailsUiState
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerMetadataUiState
import com.congnguyencn.streamplayer.StreamTvPlayerManager
import com.congnguyencn.streamplayer.ui.StreamTvPlayerSurface

private object VerticalPlayerScreenDefaults {
  const val PortraitAspectRatio = 9f / 16f
  const val BackgroundPlayerStop = 0.46f
  const val BackgroundPanelStop = 0.68f
  val PlayerHorizontalOffset = 24.dp
  val SideExpansion = 24.dp
  val MinimumSideWidth = 300.dp
  val TitleShape = RoundedCornerShape(12.dp)
  val PlayerVerticalPadding = 4.dp
  val SidePadding = 24.dp
  val SideEndPadding = 12.dp
  val ActionButtonSize = 40.dp
  val ActionSpacing = 16.dp
  val TitleToActionsSpacing = 28.dp
  val ActionsBottomSpacing = 56.dp
}

/** Portrait playback using the reference player's focus parking and retained side-section stack. */
@OptIn(UnstableApi::class)
@Composable
@Suppress("LongMethod", "LongParameterList", "CyclomaticComplexMethod", "CognitiveComplexMethod")
internal fun VerticalPlayerScreen(
  uiState: PlayerUiState,
  playerManager: StreamTvPlayerManager,
  onTogglePlayPause: () -> Unit,
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
  val interactionFocusRequester = remember { FocusRequester() }
  val pendingFocusRequester = remember { FocusRequester() }
  val errorRetryFocusRequester = remember { FocusRequester() }
  val playerInteractionSource = remember { MutableInteractionSource() }
  val sectionNavigationState = rememberPlayerSectionNavigationState()

  val openSection: (PlayerSection) -> Unit = { section ->
    if (sectionNavigationState.isAtBaseLevel) {
      pendingFocusRequester.requestFocus()
      sectionNavigationState.openRoot(section)
    }
  }

  LaunchedEffect(
    sectionNavigationState.hasSectionInPlay,
    sectionNavigationState.shouldParkFocus,
    sectionNavigationState.isReturningToBase,
    uiState.error,
  ) {
    if (uiState.error != null) return@LaunchedEffect
    withFrameMillis { }
    when {
      sectionNavigationState.shouldParkFocus -> pendingFocusRequester.requestFocus()

      sectionNavigationState.isReturningToBase || sectionNavigationState.isAtBaseLevel ->
        playerFocusRequester.requestFocus()

      else -> Unit
    }
  }

  LaunchedEffect(uiState.error?.isRetryable) {
    val error = uiState.error ?: return@LaunchedEffect
    sectionNavigationState.reset()
    awaitPlayerSectionFrame()
    if (error.isRetryable) errorRetryFocusRequester.requestFocus()
  }

  BackHandler(enabled = !sectionNavigationState.hasSectionInPlay, onBack = onExitPlayer)

  BoxWithConstraints(
    modifier = modifier
      .fillMaxSize()
      .background(StreamTvColors.NeutralBlack)
      .testTag("vertical-player-screen"),
  ) {
    val portraitPlayerWidth = (
      maxHeight - VerticalPlayerScreenDefaults.PlayerVerticalPadding * 2
      ) * VerticalPlayerScreenDefaults.PortraitAspectRatio
    val sideWidth = (
      (maxWidth - portraitPlayerWidth - 48.dp) / 2 + VerticalPlayerScreenDefaults.SideExpansion
      ).coerceAtLeast(VerticalPlayerScreenDefaults.MinimumSideWidth)
    val isPlayerFocusEnabled = sectionNavigationState.isAtBaseLevel ||
      sectionNavigationState.isReturningToBase

    VerticalPlayerAmbientBackground(modifier = Modifier.fillMaxSize())

    Box(
      modifier = Modifier
        .align(Alignment.Center)
        .offset(x = -VerticalPlayerScreenDefaults.PlayerHorizontalOffset)
        .padding(vertical = VerticalPlayerScreenDefaults.PlayerVerticalPadding)
        .fillMaxHeight()
        .aspectRatio(VerticalPlayerScreenDefaults.PortraitAspectRatio)
        .testTag("vertical-player-stage"),
    ) {
      VerticalPlayerFocusableSurface(
        onClick = onTogglePlayPause,
        interactionSource = playerInteractionSource,
        modifier = Modifier
          .fillMaxSize()
          .focusRequester(playerFocusRequester)
          .focusProperties { canFocus = isPlayerFocusEnabled }
          .onPreviewKeyEvent { event ->
            if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

            when (event.key) {
              Key.MediaPlayPause -> {
                onTogglePlayPause()
                true
              }

              Key.DirectionRight -> {
                if (sectionNavigationState.isAtBaseLevel) interactionFocusRequester.requestFocus()
                true
              }

              else -> false
            }
          }
          .testTag("vertical-player-input-target"),
      ) {
        StreamTvPlayerSurface(
          playerManager = playerManager,
          modifier = Modifier.fillMaxSize(),
          resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
        )

        if (uiState.error == null) {
          VerticalPlayerStageChrome(uiState = uiState)
        }
      }
    }

    PlayerPendingFocusTarget(
      focusRequester = pendingFocusRequester,
      modifier = Modifier.align(Alignment.CenterStart),
    )

    if (uiState.error == null) {
      if (sectionNavigationState.isAtBaseLevel) {
        VerticalPlayerInteractionSection(
          uiState = uiState,
          firstActionFocusRequester = interactionFocusRequester,
          onMoveToPlayer = { playerFocusRequester.requestFocus() },
          onTitleClick = { openSection(PlayerSection.Metadata) },
          onLikeClick = onToggleLike,
          onSaveClick = onToggleSaved,
          onCommentClick = { openSection(PlayerSection.Comments) },
          onSettingsClick = { openSection(PlayerSection.Settings) },
          modifier = Modifier
            .align(Alignment.CenterEnd)
            .width(sideWidth)
            .fillMaxHeight()
            .padding(
              top = VerticalPlayerScreenDefaults.SidePadding,
              end = VerticalPlayerScreenDefaults.SideEndPadding,
            ),
        )
      }

      PlayerSideSection(
        uiState = uiState,
        navigationState = sectionNavigationState,
        pendingFocusRequester = pendingFocusRequester,
        dismissOnLeft = true,
        onQualitySelected = onQualitySelected,
        onSubtitleSelected = onSubtitleSelected,
        onAudioSelected = onAudioSelected,
        onCommentLikeToggle = onCommentLikeToggle,
        onRootDismissed = { playerFocusRequester.requestFocus() },
        containerColor = StreamTvColors.Transparent,
        shape = RectangleShape,
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier
          .align(Alignment.CenterEnd)
          .width(sideWidth)
          .fillMaxHeight()
          .padding(
            top = VerticalPlayerScreenDefaults.SidePadding,
            end = VerticalPlayerScreenDefaults.SideEndPadding,
          ),
      )
    } else {
      PlayerErrorPanel(
        error = uiState.error,
        onRetry = onRetry.takeIf { uiState.error.isRetryable },
        retryFocusRequester = errorRetryFocusRequester,
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
    // Paused state, not an acknowledgement flash: with no control row on this surface the glyph is
    // the only thing telling a paused short apart from a stalled one.
    if (!uiState.isPlaying && !uiState.isBuffering) {
      PlayerPausedBadge(modifier = Modifier.align(Alignment.Center))
    }
    if (uiState.isBuffering) {
      PlayerBufferingIndicator(modifier = Modifier.align(Alignment.Center))
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
@Suppress("LongParameterList")
private fun VerticalPlayerInteractionSection(
  uiState: PlayerUiState,
  firstActionFocusRequester: FocusRequester,
  onMoveToPlayer: () -> Unit,
  onTitleClick: () -> Unit,
  onLikeClick: () -> Unit,
  onSaveClick: () -> Unit,
  onCommentClick: () -> Unit,
  onSettingsClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val titleFocusRequester = remember { FocusRequester() }
  val commentFocusRequester = remember { FocusRequester() }
  val saveFocusRequester = remember { FocusRequester() }
  val settingFocusRequester = remember { FocusRequester() }
  var isSectionFocused by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .onFocusChanged { isSectionFocused = it.hasFocus }
      .focusGroup(),
    verticalArrangement = Arrangement.Bottom,
  ) {
    VerticalPlayerTitleSurface(
      uiState = uiState,
      isSectionFocused = isSectionFocused,
      onClick = onTitleClick,
      modifier = Modifier
        .fillMaxWidth()
        .focusRequester(titleFocusRequester)
        .focusProperties { down = firstActionFocusRequester }
        .onPreviewKeyEvent { event ->
          if (event.type == KeyEventType.KeyDown && event.key == Key.DirectionLeft) {
            onMoveToPlayer()
            true
          } else {
            false
          }
        }
        .testTag("vertical-player-title"),
    )

    Spacer(modifier = Modifier.height(VerticalPlayerScreenDefaults.TitleToActionsSpacing))

    Row(
      modifier = Modifier.padding(horizontal = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(VerticalPlayerScreenDefaults.ActionSpacing),
    ) {
      VerticalPlayerActionButton(
        iconResId = if (uiState.isLiked) R.drawable.ic_heart else R.drawable.ic_heart_outline,
        contentDescription = stringResource(R.string.player_like),
        focusRequester = firstActionFocusRequester,
        left = null,
        right = commentFocusRequester,
        up = titleFocusRequester,
        onMoveToPlayer = onMoveToPlayer,
        onClick = onLikeClick,
        testTag = "vertical-player-like",
      )
      VerticalPlayerActionButton(
        iconResId = R.drawable.ic_player_comment,
        contentDescription = stringResource(R.string.player_comments),
        focusRequester = commentFocusRequester,
        left = firstActionFocusRequester,
        right = saveFocusRequester,
        up = titleFocusRequester,
        onMoveToPlayer = null,
        onClick = onCommentClick,
        testTag = "vertical-player-comments",
      )
      VerticalPlayerActionButton(
        iconResId = if (uiState.isSaved) R.drawable.ic_bookmark else R.drawable.ic_bookmark_outline,
        contentDescription = stringResource(R.string.player_save),
        focusRequester = saveFocusRequester,
        left = commentFocusRequester,
        right = settingFocusRequester.takeIf { uiState.settings.isAvailable },
        up = titleFocusRequester,
        onMoveToPlayer = null,
        onClick = onSaveClick,
        testTag = "vertical-player-save",
      )
      if (uiState.settings.isAvailable) {
        VerticalPlayerActionButton(
          iconResId = R.drawable.ic_settings,
          contentDescription = stringResource(R.string.player_settings),
          focusRequester = settingFocusRequester,
          left = saveFocusRequester,
          right = null,
          up = titleFocusRequester,
          onMoveToPlayer = null,
          onClick = onSettingsClick,
          testTag = "vertical-player-settings-button",
        )
      }
    }

    Spacer(modifier = Modifier.height(VerticalPlayerScreenDefaults.ActionsBottomSpacing))
  }
}

@Composable
private fun VerticalPlayerTitleSurface(
  uiState: PlayerUiState,
  isSectionFocused: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    onClick = onClick,
    modifier = modifier,
    shape = ClickableSurfaceDefaults.shape(shape = VerticalPlayerScreenDefaults.TitleShape),
    scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
    colors = ClickableSurfaceDefaults.colors(
      containerColor = if (isSectionFocused) {
        StreamTvColors.TransparentWhite10
      } else {
        StreamTvColors.Transparent
      },
      focusedContainerColor = StreamTvColors.NeutralWhite,
      contentColor = StreamTvColors.Neutral20,
      focusedContentColor = StreamTvColors.NeutralBlack,
    ),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Text(
        text = uiState.details.metadata.collectionTitle,
        style = StreamTvTheme.typography.labelMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = uiState.title,
        style = StreamTvTheme.typography.headlineLarge,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun VerticalPlayerActionButton(
  iconResId: Int,
  contentDescription: String,
  focusRequester: FocusRequester,
  left: FocusRequester?,
  right: FocusRequester?,
  up: FocusRequester,
  onMoveToPlayer: (() -> Unit)?,
  onClick: () -> Unit,
  testTag: String,
) {
  // Tracked here rather than read from Surface: Surface keeps reporting itself focused after a
  // FocusRequester moves focus away, which leaves the button painted as if it still had the D-pad.
  var isFocused by remember { mutableStateOf(false) }

  PlayerIconButton(
    iconResId = iconResId,
    contentDescription = contentDescription,
    onClick = onClick,
    isFocused = isFocused,
    modifier = Modifier
      .size(VerticalPlayerScreenDefaults.ActionButtonSize)
      .focusRequester(focusRequester)
      .focusProperties {
        left?.let { requester -> this.left = requester }
        right?.let { requester -> this.right = requester }
        this.up = up
      }
      .onFocusChanged { focusState -> isFocused = focusState.isFocused }
      .onPreviewKeyEvent { event ->
        if (event.type == KeyEventType.KeyDown && event.key == Key.DirectionLeft && onMoveToPlayer != null) {
          onMoveToPlayer()
          true
        } else {
          false
        }
      }
      .testTag(testTag),
  )
}

@Preview(device = Devices.TV_720p, showBackground = true, backgroundColor = 0xFF081D2B)
@Composable
private fun VerticalPlayerInteractionSectionPreview() {
  StreamTvTheme {
    VerticalPlayerInteractionSection(
      uiState = PlayerUiState.Initial.copy(
        title = "Into the Wild: Snow Leopards",
        isLiked = true,
        details = PlayerDetailsUiState.Empty.copy(
          metadata = PlayerMetadataUiState.Empty.copy(collectionTitle = "Wildlife Stories"),
        ),
      ),
      firstActionFocusRequester = remember { FocusRequester() },
      onMoveToPlayer = {},
      onTitleClick = {},
      onLikeClick = {},
      onSaveClick = {},
      onCommentClick = {},
      onSettingsClick = {},
      modifier = Modifier
        .width(420.dp)
        .fillMaxHeight()
        .padding(24.dp),
    )
  }
}
