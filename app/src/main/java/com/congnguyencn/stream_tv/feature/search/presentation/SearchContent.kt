package com.congnguyencn.stream_tv.feature.search.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.core.designsystem.tokens.StreamTvDimensions
import com.congnguyencn.stream_tv.feature.search.presentation.component.SearchResults
import com.congnguyencn.stream_tv.feature.search.presentation.component.SearchUiDefaults
import com.congnguyencn.stream_tv.feature.search.presentation.component.SearchWorkspace
import com.congnguyencn.stream_tv.feature.search.presentation.model.SearchContentTypeUi
import com.congnguyencn.stream_tv.feature.search.presentation.model.SearchContentUiItem
import com.congnguyencn.stream_tv.feature.search.presentation.model.SearchSectionUiItem
import kotlinx.coroutines.android.awaitFrame

private enum class PendingSearchFocus {
  None,
  KeyboardSearch,
  Results,
  TopBar,
}

@Composable
internal fun SearchContent(
  uiState: SearchUiState,
  contentFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
  onKey: (String) -> Unit,
  onBackspace: () -> Unit,
  onClear: () -> Unit,
  onCursorLeft: () -> Unit,
  onCursorRight: () -> Unit,
  onSearch: () -> Unit,
  onSuggestionClick: (String) -> Unit,
  onShowKeyboard: () -> Unit,
  onHideKeyboard: () -> Unit,
  onItemClick: (SearchContentUiItem) -> Unit,
  modifier: Modifier = Modifier,
) {
  val parkingFocusRequester = remember { FocusRequester() }
  val firstKeyboardKeyFocusRequester = remember { FocusRequester() }
  val searchKeyFocusRequester = remember { FocusRequester() }
  val firstResultFocusRequester = remember { FocusRequester() }
  val resultFocusRequesters = remember(uiState.sections.map(SearchSectionUiItem::id)) {
    uiState.sections.map { FocusRequester() }
  }
  val resultsListState = rememberLazyListState()
  val keyboardVisibilityState = remember {
    MutableTransitionState(uiState.isKeyboardVisible)
  }
  var pendingFocus by remember { mutableStateOf(PendingSearchFocus.None) }
  var contentHasFocus by remember { mutableStateOf(false) }
  val resultRequester = resultFocusRequesters.firstOrNull() ?: firstResultFocusRequester

  SideEffect {
    keyboardVisibilityState.targetState = uiState.isKeyboardVisible
  }

  fun submitAndMoveFocus() {
    if (uiState.query.isBlank() || uiState.isSearching) return
    parkingFocusRequester.requestFocus()
    pendingFocus = PendingSearchFocus.Results
    onSearch()
  }

  fun selectSuggestionAndMoveFocus(suggestion: String) {
    parkingFocusRequester.requestFocus()
    pendingFocus = PendingSearchFocus.Results
    onSuggestionClick(suggestion)
  }

  fun moveFromKeyboardToResults(): Boolean {
    if (uiState.sections.isEmpty() || uiState.isSearching) return false
    parkingFocusRequester.requestFocus()
    pendingFocus = PendingSearchFocus.Results
    if (uiState.isKeyboardVisible) onHideKeyboard()
    return true
  }

  fun moveFromResultsToKeyboard(): Boolean {
    parkingFocusRequester.requestFocus()
    pendingFocus = PendingSearchFocus.KeyboardSearch
    if (!uiState.isKeyboardVisible) onShowKeyboard()
    return true
  }

  fun closeKeyboardFromBack() {
    parkingFocusRequester.requestFocus()
    pendingFocus = if (uiState.sections.isNotEmpty()) {
      PendingSearchFocus.Results
    } else {
      PendingSearchFocus.TopBar
    }
    onHideKeyboard()
  }

  LaunchedEffect(
    pendingFocus,
    keyboardVisibilityState.currentState,
    keyboardVisibilityState.targetState,
    keyboardVisibilityState.isIdle,
    uiState.isSearching,
    uiState.submittedQuery,
    uiState.sections,
  ) {
    when (pendingFocus) {
      PendingSearchFocus.KeyboardSearch ->
        if (
          keyboardVisibilityState.isIdle &&
          keyboardVisibilityState.currentState &&
          keyboardVisibilityState.targetState
        ) {
          awaitFrame()
          searchKeyFocusRequester.requestFocus()
          pendingFocus = PendingSearchFocus.None
        }

      PendingSearchFocus.Results ->
        if (
          keyboardVisibilityState.isIdle &&
          !keyboardVisibilityState.currentState &&
          !keyboardVisibilityState.targetState &&
          !uiState.isSearching &&
          uiState.sections.isNotEmpty()
        ) {
          resultsListState.scrollToItem(0)
          awaitFrame()
          resultRequester.requestFocus()
          pendingFocus = PendingSearchFocus.None
        }

      PendingSearchFocus.TopBar ->
        if (
          keyboardVisibilityState.isIdle &&
          !keyboardVisibilityState.currentState &&
          !keyboardVisibilityState.targetState
        ) {
          awaitFrame()
          topBarFocusRequester.requestFocus()
          pendingFocus = PendingSearchFocus.None
        }

      PendingSearchFocus.None -> Unit
    }
  }

  BackHandler(enabled = uiState.isKeyboardVisible && contentHasFocus) {
    closeKeyboardFromBack()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(StreamTvColors.NeutralBlack)
      .onFocusChanged { contentHasFocus = it.hasFocus }
      .focusGroup(),
  ) {
    Box(
      modifier = Modifier
        .size(1.dp)
        .alpha(0f)
        .focusRequester(parkingFocusRequester)
        .focusable()
        .testTag("search-focus-parking"),
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(
          start = SearchUiDefaults.HorizontalPadding,
          top = StreamTvDimensions.TopBarHeight + 12.dp,
          end = 24.dp,
          bottom = 18.dp,
        ),
    ) {
      SearchWorkspace(
        query = uiState.query,
        cursorPosition = uiState.cursorPosition,
        showCaret = uiState.isKeyboardVisible,
        suggestions = uiState.suggestions,
        keyboardVisibilityState = keyboardVisibilityState,
        contentFocusRequester = contentFocusRequester,
        topBarFocusRequester = topBarFocusRequester,
        firstKeyboardKeyFocusRequester = firstKeyboardKeyFocusRequester,
        searchKeyFocusRequester = searchKeyFocusRequester,
        onKey = onKey,
        onBackspace = onBackspace,
        onClear = onClear,
        onCursorLeft = onCursorLeft,
        onCursorRight = onCursorRight,
        onSearch = ::submitAndMoveFocus,
        onSuggestionClick = ::selectSuggestionAndMoveFocus,
        onMoveToResults = ::moveFromKeyboardToResults,
      )

      Spacer(modifier = Modifier.height(if (uiState.isKeyboardVisible) 10.dp else 18.dp))

      SearchResults(
        uiState = uiState,
        resultFocusRequesters = resultFocusRequesters,
        firstFallbackFocusRequester = firstResultFocusRequester,
        listState = resultsListState,
        onFirstRowNavigateUp = ::moveFromResultsToKeyboard,
        onItemClick = onItemClick,
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF010810)
@Composable
private fun SearchContentPreview() {
  StreamTvTheme {
    SearchContent(
      uiState = SearchUiState(
        query = "Japanese culture",
        cursorPosition = 8,
        isKeyboardVisible = true,
        isLoading = false,
        suggestions = listOf(
          "Japanese culture",
          "Japanese ceremony",
          "Tokyo: Tradition in motion",
          "Travel across Japan",
          "Traditional cuisine",
          "Life in Kyoto",
        ),
        sections = SearchPreviewSections,
      ),
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

private val SearchPreviewSections = listOf(
  SearchSectionUiItem(
    id = "preview-videos",
    title = "Videos",
    type = SearchContentTypeUi.Video,
    items = listOf(
      SearchContentUiItem(
        id = "preview-video-1",
        videoUrl = "",
        thumbnailUrl = "",
        title = "Grace in every gesture",
        description = "The discipline and meaning of a Japanese ceremony.",
        ageRestriction = "P",
        type = SearchContentTypeUi.Video,
      ),
      SearchContentUiItem(
        id = "preview-video-2",
        videoUrl = "",
        thumbnailUrl = "",
        title = "Tokyo: Tradition in motion",
        description = "Ancient temples meet modern city life.",
        ageRestriction = "P",
        type = SearchContentTypeUi.Video,
      ),
    ),
  ),
)
