package com.congnguyencn.stream_tv.feature.player.presentation.component.section

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.player.presentation.PlayerUiState
import com.congnguyencn.stream_tv.feature.player.presentation.component.playerControllerPreviewUiState
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingCategory

private object PlayerSideSectionDefaults {
  val Shape = RoundedCornerShape(14.dp)
  val ContentPadding = PaddingValues(20.dp)
  val CompactHeaderHeight = 24.dp
  val CompactHeaderSpacing = 12.dp
  val CompactRootItemHeight = 54.dp
  val CompactOptionItemHeight = 42.dp
  val CompactItemSpacing = 6.dp
  val CompactMinimumHeight = 108.dp
  val CompactMaximumHeight = 430.dp
}

/**
 * Shared retained section tree for landscape and portrait playback.
 *
 * The screen owns only the base focus target. This module owns section stacking, transition focus
 * parking, parent retention, selected-row restoration, and Settings/Comment child routing.
 */
@Composable
@Suppress("LongParameterList", "CognitiveComplexMethod")
internal fun PlayerSideSection(
  uiState: PlayerUiState,
  navigationState: PlayerSectionNavigationState,
  pendingFocusRequester: FocusRequester,
  dismissOnLeft: Boolean,
  onQualitySelected: (String) -> Unit,
  onSubtitleSelected: (String) -> Unit,
  onAudioSelected: (String) -> Unit,
  onCommentLikeToggle: (Long) -> Unit,
  onRootDismissed: () -> Unit,
  modifier: Modifier = Modifier,
  containerColor: Color = StreamTvColors.TransparentBlack80,
  shape: Shape = PlayerSideSectionDefaults.Shape,
  contentPadding: PaddingValues = PlayerSideSectionDefaults.ContentPadding,
  compactSettings: Boolean = false,
) {
  var selectedCommentId by remember(uiState.title) { mutableStateOf<Long?>(null) }
  var selectedReplyId by remember(uiState.title) { mutableStateOf<Long?>(null) }
  var restoredSettingCategory by remember(uiState.title) {
    mutableStateOf<PlayerSettingCategory?>(null)
  }

  val dismissTopSection: () -> Unit = {
    val exitingSection = navigationState.panelSection
    when (exitingSection) {
      is PlayerSection.Replies -> selectedCommentId = exitingSection.commentId

      is PlayerSection.ReplyDetail -> selectedReplyId = exitingSection.replyId

      is PlayerSection.SettingOptions -> restoredSettingCategory = exitingSection.category

      PlayerSection.Metadata,
      PlayerSection.Comments,
      PlayerSection.Settings,
      null,
      -> Unit
    }
    pendingFocusRequester.requestFocus()
    navigationState.dismissCurrentSection().let { }
  }

  BackHandler(enabled = navigationState.hasSectionInPlay, onBack = dismissTopSection)

  LaunchedEffect(uiState.settings, navigationState.panelSection) {
    when (val section = navigationState.panelSection) {
      is PlayerSection.SettingOptions -> {
        if (uiState.settings.item(section.category) == null && navigationState.isPanelSettled) {
          restoredSettingCategory = uiState.settings.items.firstOrNull()?.category
          dismissTopSection()
        }
      }

      PlayerSection.Settings -> {
        if (!uiState.settings.isAvailable) {
          navigationState.reset()
          onRootDismissed()
        }
      }

      PlayerSection.Metadata,
      PlayerSection.Comments,
      is PlayerSection.Replies,
      is PlayerSection.ReplyDetail,
      null,
      -> Unit
    }
  }

  if (!navigationState.hasSectionInPlay) return

  val compactPanelHeight = navigationState.panelSection
    ?.takeIf { compactSettings && it.isSettingSection() }
    ?.let { section -> compactSettingPanelHeight(section = section, uiState = uiState) }

  Box(modifier = modifier.fillMaxSize()) {
    Surface(
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .fillMaxWidth()
        .then(
          if (compactPanelHeight != null) {
            Modifier.height(compactPanelHeight)
          } else {
            Modifier.fillMaxSize()
          },
        )
        .testTag("player-side-section"),
      shape = shape,
      colors = SurfaceDefaults.colors(containerColor = containerColor),
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        navigationState.sectionLayers.forEach { section ->
          key(section) {
            val isPanelSection = section == navigationState.panelSection
            AnimatedPlayerSection(
              modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = if (isPanelSection) 1f else 0f }
                .then(hiddenSectionSemantics(isPanelSection)),
              isEntering = isPanelSection && navigationState.isPanelEntering,
              isExiting = isPanelSection && navigationState.isPanelExiting,
              onEnterAnimationFinished = navigationState::onSectionEnterFinished,
              onExitAnimationFinished = {
                navigationState.onSectionExitFinished()
                if (navigationState.isAtBaseLevel) onRootDismissed()
              },
            ) {
              PlayerSectionContent(
                section = section,
                uiState = uiState,
                navigationState = navigationState,
                selectedCommentId = selectedCommentId,
                selectedReplyId = selectedReplyId,
                restoredSettingCategory = restoredSettingCategory,
                pendingFocusRequester = pendingFocusRequester,
                dismissOnLeft = dismissOnLeft,
                onCommentSelected = { commentId ->
                  selectedCommentId = commentId
                  selectedReplyId = null
                  pendingFocusRequester.requestFocus()
                  navigationState.openChild(PlayerSection.Replies(commentId))
                },
                onReplySelected = { commentId, replyId ->
                  selectedReplyId = replyId
                  pendingFocusRequester.requestFocus()
                  navigationState.openChild(PlayerSection.ReplyDetail(commentId, replyId))
                },
                onSettingCategorySelected = { category ->
                  restoredSettingCategory = category
                  pendingFocusRequester.requestFocus()
                  navigationState.openChild(PlayerSection.SettingOptions(category))
                },
                onQualitySelected = onQualitySelected,
                onSubtitleSelected = onSubtitleSelected,
                onAudioSelected = onAudioSelected,
                onCommentLikeToggle = onCommentLikeToggle,
                onBack = dismissTopSection,
                modifier = Modifier
                  .fillMaxSize()
                  .padding(contentPadding),
              )
            }
          }
        }
      }
    }
  }
}

private fun PlayerSection.isSettingSection(): Boolean =
  this == PlayerSection.Settings || this is PlayerSection.SettingOptions

private fun compactSettingPanelHeight(section: PlayerSection, uiState: PlayerUiState) = when (section) {
  PlayerSection.Settings -> uiState.settings.items.size to PlayerSideSectionDefaults.CompactRootItemHeight
  is PlayerSection.SettingOptions ->
    (uiState.settings.item(section.category)?.options?.size ?: 0) to
      PlayerSideSectionDefaults.CompactOptionItemHeight

  else -> 0 to 0.dp
}.let { (itemCount, itemHeight) ->
  val listHeight = itemHeight * itemCount +
    PlayerSideSectionDefaults.CompactItemSpacing * (itemCount - 1).coerceAtLeast(0)
  (
    PlayerSideSectionDefaults.ContentPadding.calculateTopPadding() +
      PlayerSideSectionDefaults.CompactHeaderHeight +
      PlayerSideSectionDefaults.CompactHeaderSpacing +
      listHeight +
      PlayerSideSectionDefaults.ContentPadding.calculateBottomPadding()
    ).coerceIn(
    PlayerSideSectionDefaults.CompactMinimumHeight,
    PlayerSideSectionDefaults.CompactMaximumHeight,
  )
}

@Composable
@Suppress("LongParameterList", "CyclomaticComplexMethod")
private fun PlayerSectionContent(
  section: PlayerSection,
  uiState: PlayerUiState,
  navigationState: PlayerSectionNavigationState,
  selectedCommentId: Long?,
  selectedReplyId: Long?,
  restoredSettingCategory: PlayerSettingCategory?,
  pendingFocusRequester: FocusRequester,
  dismissOnLeft: Boolean,
  onCommentSelected: (Long) -> Unit,
  onReplySelected: (Long, Long) -> Unit,
  onSettingCategorySelected: (PlayerSettingCategory) -> Unit,
  onQualitySelected: (String) -> Unit,
  onSubtitleSelected: (String) -> Unit,
  onAudioSelected: (String) -> Unit,
  onCommentLikeToggle: (Long) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val focusRequester = remember(section) { FocusRequester() }
  val isFocusEnabled = section == navigationState.panelSection && navigationState.isPanelSettled

  when (section) {
    PlayerSection.Metadata ->
      PlayerMetadataSection(
        title = uiState.title,
        metadata = uiState.details.metadata,
        isFocusEnabled = isFocusEnabled,
        focusRequester = focusRequester,
        onBack = onBack,
        dismissOnLeft = dismissOnLeft,
        modifier = modifier,
      )

    PlayerSection.Comments ->
      PlayerCommentsSection(
        comments = uiState.details.comments,
        totalCommentCount = uiState.details.totalCommentCount,
        selectedCommentId = selectedCommentId,
        isFocusEnabled = isFocusEnabled,
        focusRequester = focusRequester,
        onCommentSelected = onCommentSelected,
        onBack = onBack,
        dismissOnLeft = dismissOnLeft,
        modifier = modifier,
      )

    is PlayerSection.Replies ->
      PlayerRepliesSection(
        parentComment = uiState.details.findComment(section.commentId),
        replies = uiState.details.replies(section.commentId),
        selectedReplyId = selectedReplyId,
        isFocusEnabled = isFocusEnabled,
        focusRequester = focusRequester,
        onReplySelected = { replyId -> onReplySelected(section.commentId, replyId) },
        onParentCommentLikeClick = onCommentLikeToggle,
        onBack = onBack,
        dismissOnLeft = dismissOnLeft,
        modifier = modifier,
      )

    is PlayerSection.ReplyDetail -> {
      val reply = uiState.details.findReply(section.commentId, section.replyId)
      if (reply != null) {
        PlayerReplyDetailSection(
          reply = reply,
          isFocusEnabled = isFocusEnabled,
          focusRequester = focusRequester,
          onLikeClick = onCommentLikeToggle,
          onBack = onBack,
          dismissOnLeft = dismissOnLeft,
          modifier = modifier,
        )
      }
    }

    PlayerSection.Settings ->
      PlayerSettingRootSection(
        settings = uiState.settings,
        restoredCategory = restoredSettingCategory,
        isFocusEnabled = isFocusEnabled,
        focusRequester = focusRequester,
        onCategorySelected = onSettingCategorySelected,
        onBack = onBack,
        dismissOnLeft = dismissOnLeft,
        modifier = modifier,
      )

    is PlayerSection.SettingOptions -> {
      val item = uiState.settings.item(section.category)
      if (item != null) {
        PlayerSettingOptionsSection(
          item = item,
          isFocusEnabled = isFocusEnabled,
          focusRequester = focusRequester,
          onOptionSelected = { option ->
            when (section.category) {
              PlayerSettingCategory.Quality -> onQualitySelected(option.id)
              PlayerSettingCategory.Subtitles -> onSubtitleSelected(option.id)
              PlayerSettingCategory.Audio -> onAudioSelected(option.id)
            }
          },
          onBack = onBack,
          dismissOnLeft = dismissOnLeft,
          modifier = modifier,
        )
      } else if (isFocusEnabled) {
        LaunchedEffect(section) {
          pendingFocusRequester.requestFocus()
        }
      }
    }
  }
}

@Composable
internal fun PlayerPendingFocusTarget(focusRequester: FocusRequester, modifier: Modifier = Modifier) {
  val interactionSource = remember { MutableInteractionSource() }
  Box(
    modifier = modifier
      .size(1.dp)
      .alpha(0f)
      .focusRequester(focusRequester)
      .onPreviewKeyEvent { event ->
        event.type == KeyEventType.KeyDown && when (event.key) {
          Key.DirectionLeft,
          Key.DirectionRight,
          Key.DirectionUp,
          Key.DirectionDown,
          Key.DirectionCenter,
          Key.Enter,
          -> true

          else -> false
        }
      }
      .focusable(interactionSource = interactionSource),
  )
}

private fun hiddenSectionSemantics(isPanelSection: Boolean): Modifier =
  if (isPanelSection) Modifier else Modifier.clearAndSetSemantics { }

@Preview(device = Devices.TV_720p, showBackground = true, backgroundColor = 0xFF171717)
@Composable
private fun PlayerSideSectionPortraitPreview() {
  val navigationState = remember {
    PlayerSectionNavigationState().apply {
      openRoot(PlayerSection.Metadata)
      onSectionEnterFinished()
    }
  }
  val pendingFocusRequester = remember { FocusRequester() }

  StreamTvTheme {
    Surface(
      modifier = Modifier.size(width = 420.dp, height = 680.dp),
      colors = SurfaceDefaults.colors(containerColor = StreamTvColors.NeutralBlack),
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        PlayerPendingFocusTarget(focusRequester = pendingFocusRequester)
        PlayerSideSection(
          uiState = playerControllerPreviewUiState(),
          navigationState = navigationState,
          pendingFocusRequester = pendingFocusRequester,
          dismissOnLeft = true,
          onQualitySelected = {},
          onSubtitleSelected = {},
          onAudioSelected = {},
          onCommentLikeToggle = {},
          onRootDismissed = {},
          containerColor = StreamTvColors.Transparent,
          shape = RectangleShape,
          contentPadding = PaddingValues(0.dp),
          modifier = Modifier.fillMaxSize(),
        )
      }
    }
  }
}

@Preview(device = Devices.TV_720p, showBackground = true, backgroundColor = 0xFF171717)
@Composable
private fun PlayerSideSectionCompactSettingsPreview() {
  val navigationState = remember {
    PlayerSectionNavigationState().apply {
      openRoot(PlayerSection.Settings)
      onSectionEnterFinished()
    }
  }
  val pendingFocusRequester = remember { FocusRequester() }

  StreamTvTheme {
    Surface(
      modifier = Modifier.fillMaxSize(),
      colors = SurfaceDefaults.colors(containerColor = StreamTvColors.NeutralBlack),
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        PlayerPendingFocusTarget(focusRequester = pendingFocusRequester)
        PlayerSideSection(
          uiState = playerControllerPreviewUiState(),
          navigationState = navigationState,
          pendingFocusRequester = pendingFocusRequester,
          dismissOnLeft = false,
          onQualitySelected = {},
          onSubtitleSelected = {},
          onAudioSelected = {},
          onCommentLikeToggle = {},
          onRootDismissed = {},
          compactSettings = true,
          modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(top = 30.dp, end = 30.dp, bottom = 30.dp)
            .width(315.dp)
            .fillMaxHeight(),
        )
      }
    }
  }
}
