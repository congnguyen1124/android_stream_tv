package com.congnguyencn.stream_tv.feature.search.presentation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.dp
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvSurface
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.search.presentation.model.SearchContentTypeUi
import com.congnguyencn.stream_tv.feature.search.presentation.model.SearchContentUiItem
import com.congnguyencn.stream_tv.feature.search.presentation.model.SearchSectionUiItem
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SearchContentTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun contentEntrySkipsDisplayOnlyQueryAndFocusesFirstSuggestion() {
    composeRule.setContent {
      val contentFocusRequester = remember { FocusRequester() }

      StreamTvTheme {
        StreamTvSurface {
          SearchContent(
            uiState = searchState(),
            contentFocusRequester = contentFocusRequester,
            topBarFocusRequester = remember { FocusRequester() },
            onKey = {},
            onBackspace = {},
            onClear = {},
            onCursorLeft = {},
            onCursorRight = {},
            onSearch = {},
            onSuggestionClick = {},
            onShowKeyboard = {},
            onHideKeyboard = {},
            onItemClick = {},
          )
        }
      }

      LaunchedEffect(Unit) { contentFocusRequester.requestFocus() }
    }

    composeRule.onNodeWithTag("search-query")
      .assert(!hasClickAction())
      .assertIsNotFocused()
    composeRule.onNodeWithTag("search-suggestion-0").assertIsFocused()

    composeRule
      .onNodeWithTag("search-suggestion-0")
      .performKeyInput { pressKey(Key.DirectionRight) }
    composeRule.onNodeWithTag("search-key-a").assertIsFocused()
  }

  @Test
  fun downFromKeyboardToResultsClosesKeyboardBeforeFocusingFirstRow() {
    var uiState by mutableStateOf(searchState(query = "tiger"))

    composeRule.setContent {
      StreamTvTheme {
        StreamTvSurface {
          SearchContent(
            uiState = uiState,
            contentFocusRequester = remember { FocusRequester() },
            topBarFocusRequester = remember { FocusRequester() },
            onKey = {},
            onBackspace = {},
            onClear = {},
            onCursorLeft = {},
            onCursorRight = {},
            onSearch = {},
            onSuggestionClick = {},
            onShowKeyboard = {},
            onHideKeyboard = { uiState = uiState.copy(isKeyboardVisible = false) },
            onItemClick = {},
          )
        }
      }
    }

    composeRule.onNodeWithTag("search-key-search")
      .performSemanticsAction(SemanticsActions.RequestFocus)
      .assertIsFocused()
      .performKeyInput { pressKey(Key.DirectionDown) }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag("search-key-search").assertDoesNotExist()
    composeRule.onNodeWithTag("search-content-row-search-videos-selection").assertIsFocused()
  }

  @Test
  fun upFromFirstResultRestoresKeyboardAndItsSearchKey() {
    var uiState by mutableStateOf(searchState(query = "tiger").copy(isKeyboardVisible = false))

    composeRule.setContent {
      StreamTvTheme {
        StreamTvSurface {
          SearchContent(
            uiState = uiState,
            contentFocusRequester = remember { FocusRequester() },
            topBarFocusRequester = remember { FocusRequester() },
            onKey = {},
            onBackspace = {},
            onClear = {},
            onCursorLeft = {},
            onCursorRight = {},
            onSearch = {},
            onSuggestionClick = {},
            onShowKeyboard = { uiState = uiState.copy(isKeyboardVisible = true) },
            onHideKeyboard = {},
            onItemClick = {},
          )
        }
      }
    }

    composeRule.onNodeWithTag("search-content-row-search-videos-selection")
      .performSemanticsAction(SemanticsActions.RequestFocus)
      .assertIsFocused()
      .performKeyInput { pressKey(Key.DirectionUp) }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag("search-key-search").assertExists().assertIsFocused()
  }

  @Test
  fun submitParksFocusBeforeKeyboardClosesThenFocusesFirstResultRow() {
    var uiState by mutableStateOf(searchState(query = "tiger"))

    composeRule.setContent {
      StreamTvTheme {
        StreamTvSurface {
          SearchContent(
            uiState = uiState,
            contentFocusRequester = remember { FocusRequester() },
            topBarFocusRequester = remember { FocusRequester() },
            onKey = {},
            onBackspace = {},
            onClear = {},
            onCursorLeft = {},
            onCursorRight = {},
            onSearch = {
              uiState = uiState.copy(
                isKeyboardVisible = false,
                submittedQuery = "tiger",
              )
            },
            onSuggestionClick = {},
            onShowKeyboard = {},
            onHideKeyboard = {},
            onItemClick = {},
          )
        }
      }
    }

    composeRule.onNodeWithTag("search-key-search")
      .performSemanticsAction(SemanticsActions.RequestFocus)
      .performKeyInput { pressKey(Key.DirectionCenter) }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag("search-key-search").assertDoesNotExist()
    composeRule
      .onNodeWithTag("search-content-row-search-videos-selection")
      .assertIsFocused()
  }

  @Test
  fun firstRecommendationRowFitsInsideViewportWhileKeyboardIsVisible() {
    composeRule.setContent {
      StreamTvTheme {
        StreamTvSurface {
          SearchContent(
            uiState = searchState(),
            contentFocusRequester = remember { FocusRequester() },
            topBarFocusRequester = remember { FocusRequester() },
            onKey = {},
            onBackspace = {},
            onClear = {},
            onCursorLeft = {},
            onCursorRight = {},
            onSearch = {},
            onSuggestionClick = {},
            onShowKeyboard = {},
            onHideKeyboard = {},
            onItemClick = {},
          )
        }
      }
    }

    val firstRowBounds = composeRule
      .onNodeWithTag("search-content-row-search-videos-selection")
      .fetchSemanticsNode()
      .boundsInRoot
    val rootBottom = composeRule.onRoot().fetchSemanticsNode().boundsInRoot.bottom
    val firstRowTopDp = with(composeRule.density) { firstRowBounds.top.toDp() }
    val firstRowBottomDp = with(composeRule.density) { firstRowBounds.bottom.toDp() }
    val rootBottomDp = with(composeRule.density) { rootBottom.toDp() }

    assertTrue(
      "First recommendation row starts at $firstRowTopDp; expected it above 450dp",
      firstRowTopDp < 450.dp,
    )
    assertTrue(
      "First recommendation row ends at $firstRowBottomDp outside the $rootBottomDp viewport",
      firstRowBottomDp <= rootBottomDp,
    )
  }

  @Test
  fun leftAtFirstRecommendationKeepsTheRowFocused() {
    composeRule.setContent {
      StreamTvTheme {
        StreamTvSurface {
          SearchContent(
            uiState = searchState().copy(isKeyboardVisible = false),
            contentFocusRequester = remember { FocusRequester() },
            topBarFocusRequester = remember { FocusRequester() },
            onKey = {},
            onBackspace = {},
            onClear = {},
            onCursorLeft = {},
            onCursorRight = {},
            onSearch = {},
            onSuggestionClick = {},
            onShowKeyboard = {},
            onHideKeyboard = {},
            onItemClick = {},
          )
        }
      }
    }

    composeRule.onNodeWithTag("search-content-row-search-videos-selection")
      .performSemanticsAction(SemanticsActions.RequestFocus)
      .assertIsFocused()
      .performKeyInput { pressKey(Key.DirectionRight) }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag("search-content-row-search-videos-selection")
      .performKeyInput { pressKey(Key.DirectionLeft) }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag("search-content-row-search-videos-selection")
      .performKeyInput { pressKey(Key.DirectionLeft) }

    composeRule.onNodeWithTag("search-content-row-search-videos-selection").assertIsFocused()
  }

  private fun searchState(query: String = "") = SearchUiState(
    query = query,
    cursorPosition = query.length,
    isKeyboardVisible = true,
    isLoading = false,
    suggestions = listOf("Wildlife documentaries", "Japanese culture"),
    sections = listOf(
      SearchSectionUiItem(
        id = "search-videos",
        title = "Videos",
        type = SearchContentTypeUi.Video,
        items = listOf(
          SearchContentUiItem(
            id = "video-tiger",
            videoUrl = "",
            thumbnailUrl = "",
            title = "Realm of the tiger",
            description = "A journey through the wild.",
            ageRestriction = "T13",
            type = SearchContentTypeUi.Video,
          ),
          SearchContentUiItem(
            id = "video-tokyo",
            videoUrl = "",
            thumbnailUrl = "",
            title = "Tokyo: Tradition in motion",
            description = "Ancient temples meet modern city life.",
            ageRestriction = "P",
            type = SearchContentTypeUi.Video,
          ),
        ),
      ),
    ),
  )
}
