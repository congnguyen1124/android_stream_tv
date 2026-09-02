package com.congnguyencn.stream_tv.feature.player.presentation.component

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.player.presentation.PlayerUiState
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerCommentUiItem
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerDetailsUiState
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerMetadataUiState
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSeekPreviewUiState
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingCategory
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingOptionUiItem
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingUiItem
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingsUiState
import com.congnguyencn.streamplayer.StreamTvPlayerCommand
import com.congnguyencn.streamplayer.StreamTvPlayerManager
import com.congnguyencn.streamplayer.StreamTvPlayerQuery
import com.congnguyencn.streamplayer.model.StreamTvAdEvent
import com.congnguyencn.streamplayer.model.StreamTvPlayerState
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test

class PlayerScreenFocusTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun horizontalSectionsRestoreTheirExactControllerOpeners() {
    setHorizontalPlayer()

    composeRule.onNodeWithTag("player-input-target")
      .assertIsFocused()
      .performKeyInput { pressKey(Key.DirectionCenter) }
    settleFocusTransition()

    composeRule.onNodeWithTag("player-controller").assertIsDisplayed()
    composeRule.onNodeWithTag("player-seek-control")
      .assertIsFocused()
      .performKeyInput {
        pressKey(Key.DirectionUp)
        pressKey(Key.DirectionLeft)
        pressKey(Key.DirectionCenter)
      }

    settleFocusTransition()
    composeRule.onNodeWithTag("player-side-section").assertIsDisplayed()
    composeRule.onNodeWithTag("player-comment-1")
      .assertIsFocused()
      .performKeyInput { pressKey(Key.Back) }
    settleFocusTransition()
    composeRule.onNodeWithTag("player-controller-CommentButton")
      .assertIsFocused()
      .performKeyInput {
        pressKey(Key.DirectionLeft)
        pressKey(Key.DirectionLeft)
        pressKey(Key.DirectionLeft)
        pressKey(Key.DirectionCenter)
      }

    settleFocusTransition()
    composeRule.onNodeWithTag("player-metadata-section")
      .assertIsFocused()
      .performKeyInput { pressKey(Key.Back) }
    settleFocusTransition()
    composeRule.onNodeWithTag("player-controller-title")
      .assertIsFocused()
      .performKeyInput {
        pressKey(Key.DirectionRight)
        pressKey(Key.DirectionRight)
        pressKey(Key.DirectionRight)
        pressKey(Key.DirectionRight)
        pressKey(Key.DirectionCenter)
      }

    settleFocusTransition()
    composeRule.onNodeWithTag("player-setting-quality")
      .assertIsFocused()
      .performKeyInput { pressKey(Key.Back) }
    settleFocusTransition()
    composeRule.onNodeWithTag("player-controller-SettingButton").assertIsFocused()
  }

  @Test
  fun horizontalControllerKeepsFocusAtItsLeftBoundary() {
    setHorizontalPlayer()

    composeRule.onNodeWithTag("player-input-target")
      .assertIsFocused()
      .performKeyInput { pressKey(Key.DirectionCenter) }
    settleFocusTransition()

    composeRule.onNodeWithTag("player-seek-control")
      .assertIsFocused()
      .performKeyInput {
        pressKey(Key.DirectionUp)
        pressKey(Key.DirectionLeft)
        pressKey(Key.DirectionLeft)
        pressKey(Key.DirectionLeft)
        pressKey(Key.DirectionLeft)
      }
    composeRule.onNodeWithTag("player-controller-title")
      .assertIsFocused()
      .performKeyInput { pressKey(Key.DirectionLeft) }

    composeRule.onNodeWithTag("player-controller-title").assertIsFocused()
  }

  @Test
  fun horizontalControllerCanBeFocusedAgainAfterReturningToPlayer() {
    setHorizontalPlayer(screenUiState = uiState.copy(isPlaying = true))

    composeRule.onNodeWithTag("player-input-target")
      .assertIsFocused()
      .performKeyInput { pressKey(Key.DirectionCenter) }
    settleFocusTransition()
    composeRule.onNodeWithTag("player-seek-control").assertIsFocused()

    composeRule.mainClock.advanceTimeBy(5_200)
    settleFocusTransition()
    composeRule.onNodeWithTag("player-input-target")
      .assertIsFocused()
      .performKeyInput { pressKey(Key.DirectionCenter) }
    settleFocusTransition()

    composeRule.onNodeWithTag("player-seek-control").assertIsFocused()
  }

  @Test
  fun verticalLeftPopsChildrenThenReturnsToThePortraitPlayer() {
    setVerticalPlayer()

    composeRule.onNodeWithTag("vertical-player-input-target")
      .assertIsFocused()
      .performKeyInput { pressKey(Key.DirectionRight) }
    composeRule.onNodeWithTag("vertical-player-like")
      .assertIsFocused()
      .performKeyInput {
        pressKey(Key.DirectionRight)
        pressKey(Key.DirectionCenter)
      }

    settleFocusTransition()
    composeRule.onNodeWithTag("player-side-section").assertIsDisplayed()
    composeRule.onNodeWithTag("player-comment-1")
      .assertIsFocused()
      .performKeyInput { pressKey(Key.DirectionLeft) }
    settleFocusTransition()
    composeRule.onNodeWithTag("vertical-player-input-target").assertIsFocused()

    composeRule.onNodeWithTag("vertical-player-input-target")
      .performKeyInput { pressKey(Key.DirectionRight) }
    composeRule.onNodeWithTag("vertical-player-like")
      .performKeyInput { pressKey(Key.DirectionUp) }
    composeRule.onNodeWithTag("vertical-player-title")
      .assertIsFocused()
      .performKeyInput { pressKey(Key.DirectionCenter) }
    settleFocusTransition()
    composeRule.onNodeWithTag("player-metadata-section")
      .assertIsFocused()
      .performKeyInput { pressKey(Key.DirectionLeft) }
    settleFocusTransition()
    composeRule.onNodeWithTag("vertical-player-input-target").assertIsFocused()

    composeRule.onNodeWithTag("vertical-player-input-target")
      .performKeyInput { pressKey(Key.DirectionRight) }
    composeRule.onNodeWithTag("vertical-player-like")
      .performKeyInput {
        pressKey(Key.DirectionRight)
        pressKey(Key.DirectionRight)
        pressKey(Key.DirectionRight)
        pressKey(Key.DirectionCenter)
      }
    settleFocusTransition()
    composeRule.onNodeWithTag("player-setting-quality")
      .assertIsFocused()
      .performKeyInput { pressKey(Key.DirectionCenter) }
    settleFocusTransition()
    composeRule.onNodeWithTag("player-setting-option-1080")
      .assertIsFocused()
      .performKeyInput { pressKey(Key.DirectionLeft) }
    settleFocusTransition()
    composeRule.onNodeWithTag("player-setting-quality")
      .assertIsFocused()
      .performKeyInput { pressKey(Key.DirectionLeft) }
    settleFocusTransition()
    composeRule.onNodeWithTag("vertical-player-input-target").assertIsFocused()
  }

  private fun setHorizontalPlayer(screenUiState: PlayerUiState = uiState) {
    composeRule.mainClock.autoAdvance = false
    composeRule.setContent {
      StreamTvTheme {
        PlayerScreen(
          uiState = screenUiState,
          playerManager = FakeStreamTvPlayerManager,
          onTogglePlayPause = {},
          onSeekForward = {},
          onSeekBack = {},
          onToggleLike = {},
          onToggleSaved = {},
          onCommentLikeToggle = {},
          onQualitySelected = {},
          onSubtitleSelected = {},
          onAudioSelected = {},
          onRetry = {},
          onExitPlayer = {},
        )
      }
    }
    settleFocusTransition()
  }

  private fun setVerticalPlayer() {
    composeRule.mainClock.autoAdvance = false
    composeRule.setContent {
      StreamTvTheme {
        VerticalPlayerScreen(
          uiState = uiState,
          playerManager = FakeStreamTvPlayerManager,
          onTogglePlayPause = {},
          onToggleLike = {},
          onToggleSaved = {},
          onCommentLikeToggle = {},
          onQualitySelected = {},
          onSubtitleSelected = {},
          onAudioSelected = {},
          onRetry = {},
          onExitPlayer = {},
        )
      }
    }
    settleFocusTransition()
  }

  private fun settleFocusTransition() {
    composeRule.mainClock.advanceTimeByFrame()
    composeRule.waitForIdle()
    composeRule.mainClock.advanceTimeBy(400)
    composeRule.waitForIdle()
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
      seekPreview = PlayerSeekPreviewUiState.Empty,
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
    val uiState = PlayerUiState.Initial.copy(
      title = "Tokyo: Tradition in motion",
      duration = 10.minutes,
      details = details,
      settings = settings,
    )
  }
}

private object FakeStreamTvPlayerManager : StreamTvPlayerManager {
  override val playerState = MutableStateFlow(StreamTvPlayerState.Initial)
  override val adEvents: Flow<StreamTvAdEvent> = emptyFlow()

  override fun dispatch(command: StreamTvPlayerCommand) = Unit

  @Suppress("UNCHECKED_CAST")
  override fun <R> query(query: StreamTvPlayerQuery<R>): R = when (query) {
    StreamTvPlayerQuery.ExoPlayer -> null
    is StreamTvPlayerQuery.IsPreparedFor -> false
  } as R

  override fun close() = Unit
}
