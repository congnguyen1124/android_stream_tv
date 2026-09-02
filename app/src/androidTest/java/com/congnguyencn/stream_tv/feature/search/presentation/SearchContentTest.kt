package com.congnguyencn.stream_tv.feature.search.presentation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvSurface
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.search.presentation.model.SearchContentTypeUi
import com.congnguyencn.stream_tv.feature.search.presentation.model.SearchContentUiItem
import com.congnguyencn.stream_tv.feature.search.presentation.model.SearchSectionUiItem
import org.junit.Rule
import org.junit.Test

class SearchContentTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun downAndRightMoveFromQueryThroughSuggestionToKeyboard() {
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

    composeRule
      .onNodeWithTag("search-query")
      .assertIsFocused()
      .performKeyInput { pressKey(Key.DirectionDown) }
    composeRule.onNodeWithTag("search-suggestion-0").assertIsFocused()

    composeRule
      .onNodeWithTag("search-suggestion-0")
      .performKeyInput { pressKey(Key.DirectionRight) }
    composeRule.onNodeWithTag("search-key-a").assertIsFocused()
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

    composeRule.onNodeWithTag("search-key-search").performClick()
    composeRule.waitForIdle()

    composeRule
      .onNodeWithTag("search-content-row-search-videos-selection")
      .assertIsFocused()
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
        ),
      ),
    ),
  )
}
