package com.congnguyencn.stream_tv.feature.player.presentation.component.section

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.dp
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvSurface
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.player.presentation.PlayerUiState
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerCommentUiItem
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerDetailsUiState
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerMetadataUiState
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingCategory
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingOptionUiItem
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingUiItem
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingsUiState
import org.junit.Rule
import org.junit.Test

class PlayerSideSectionTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun commentReplyBackRestoresTheSelectedComment() {
    lateinit var navigationState: PlayerSectionNavigationState

    setSectionContent(
      initialSection = PlayerSection.Comments,
      onStateReady = { navigationState = it },
    )

    composeRule
      .onNodeWithTag("player-comment-1")
      .assertIsDisplayed()
      .assertIsFocused()
      .performKeyInput { pressKey(Key.DirectionCenter) }

    composeRule.waitUntil(timeoutMillis = 2_000) {
      navigationState.panelSection == PlayerSection.Replies(commentId = 1) &&
        navigationState.isPanelSettled
    }
    composeRule
      .onNodeWithTag("player-comment-101")
      .assertIsDisplayed()
      .assertIsFocused()
      .performKeyInput { pressKey(Key.DirectionLeft) }

    composeRule.waitUntil(timeoutMillis = 2_000) {
      navigationState.panelSection == PlayerSection.Comments && navigationState.isPanelSettled
    }
    composeRule.onNodeWithTag("player-comment-1").assertIsFocused()
  }

  @Test
  fun settingChildBackRestoresTheCategoryThatOpenedIt() {
    lateinit var navigationState: PlayerSectionNavigationState

    setSectionContent(
      initialSection = PlayerSection.Settings,
      onStateReady = { navigationState = it },
    )

    composeRule
      .onNodeWithTag("player-setting-quality")
      .assertIsDisplayed()
      .assertIsFocused()
      .performKeyInput { pressKey(Key.DirectionCenter) }

    composeRule.waitUntil(timeoutMillis = 2_000) {
      navigationState.panelSection == PlayerSection.SettingOptions(PlayerSettingCategory.Quality) &&
        navigationState.isPanelSettled
    }
    composeRule
      .onNodeWithTag("player-setting-option-1080")
      .assertIsDisplayed()
      .assertIsFocused()
      .performKeyInput { pressKey(Key.DirectionLeft) }

    composeRule.waitUntil(timeoutMillis = 2_000) {
      navigationState.panelSection == PlayerSection.Settings && navigationState.isPanelSettled
    }
    composeRule.onNodeWithTag("player-setting-quality").assertIsFocused()
  }

  @Test
  fun metadataOwnsFocusAndDisplaysLaunchDetails() {
    setSectionContent(initialSection = PlayerSection.Metadata)

    composeRule.onNodeWithText("Tokyo: Tradition in motion").assertIsDisplayed()
    composeRule.onNodeWithText("A journey through Asakusa.").assertIsDisplayed()
  }

  private fun setSectionContent(
    initialSection: PlayerSection,
    onStateReady: (PlayerSectionNavigationState) -> Unit = {},
  ) {
    composeRule.setContent {
      val navigationState = rememberPlayerSectionNavigationState()
      val pendingFocusRequester = remember { FocusRequester() }
      onStateReady(navigationState)

      StreamTvTheme {
        StreamTvSurface {
          Box(modifier = Modifier.size(width = 400.dp, height = 650.dp)) {
            PlayerPendingFocusTarget(focusRequester = pendingFocusRequester)
            PlayerSideSection(
              uiState = uiState,
              navigationState = navigationState,
              pendingFocusRequester = pendingFocusRequester,
              dismissOnLeft = true,
              onQualitySelected = {},
              onSubtitleSelected = {},
              onAudioSelected = {},
              onCommentLikeToggle = {},
              onRootDismissed = {},
              modifier = Modifier.matchParentSize(),
            )
          }
        }
      }

      LaunchedEffect(Unit) {
        pendingFocusRequester.requestFocus()
        navigationState.openRoot(initialSection)
      }
    }
  }

  private companion object {
    val parentComment = PlayerCommentUiItem(
      id = 1,
      parentId = null,
      authorName = "StreamTV",
      authorAvatarUrl = null,
      isAdmin = true,
      isPinned = true,
      postedAtLabel = "1 day ago",
      content = "What detail stayed with you?",
      replyCount = 1,
      likeCount = 10,
      isLiked = false,
    )
    val reply = parentComment.copy(
      id = 101,
      parentId = 1,
      authorName = "Anna Lee",
      isAdmin = false,
      isPinned = false,
      content = "The final wide shot.",
      replyCount = 0,
    )
    val settings = PlayerSettingsUiState(
      items = listOf(
        PlayerSettingUiItem(
          category = PlayerSettingCategory.Quality,
          selectedLabel = "1080p",
          options = listOf(
            PlayerSettingOptionUiItem(id = "auto", label = "Auto", isSelected = false),
            PlayerSettingOptionUiItem(id = "1080", label = "1080p", isSelected = true),
          ),
        ),
      ),
    )
    val details = PlayerDetailsUiState(
      metadata = PlayerMetadataUiState(
        description = "A journey through Asakusa.",
        longDescription = "A portrait of living traditions.",
        collectionTitle = "StreamTV Originals",
        seasonTitle = "Featured Stories",
        releaseYear = "2026",
        genres = "Culture, Documentary",
        directors = "Kenji Mori",
        producers = "Olivia Reed",
        writers = "Emma Clark",
        cast = "Aiko Tanaka",
        ageRestriction = "P",
      ),
      comments = listOf(parentComment),
      repliesByCommentId = mapOf(1L to listOf(reply)),
    )
    val uiState = PlayerUiState.Initial.copy(
      title = "Tokyo: Tradition in motion",
      details = details,
      settings = settings,
    )
  }
}
