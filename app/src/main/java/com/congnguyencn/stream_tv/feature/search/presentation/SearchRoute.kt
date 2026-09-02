package com.congnguyencn.stream_tv.feature.search.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.core.designsystem.tokens.StreamTvDimensions
import com.congnguyencn.stream_tv.feature.search.presentation.component.SearchContentRow
import com.congnguyencn.stream_tv.feature.search.presentation.component.SearchCursorTextField
import com.congnguyencn.stream_tv.feature.search.presentation.component.SearchVirtualKeyboard
import com.congnguyencn.stream_tv.feature.search.presentation.model.SearchContentTypeUi
import com.congnguyencn.stream_tv.feature.search.presentation.model.SearchContentUiItem
import com.congnguyencn.stream_tv.feature.search.presentation.model.SearchSectionUiItem
import kotlinx.coroutines.android.awaitFrame

private object SearchScreenDefaults {
  const val KeyboardAnimationDurationMillis = 180
  val HorizontalPadding = 48.dp
  val SearchFieldWidth = 720.dp
  val SearchFieldHeight = 54.dp
  val SearchWorkspaceHeight = 276.dp
}

private enum class PendingSearchFocus {
  None,
  Query,
  Results,
}

@Composable
internal fun SearchScreen(
  contentFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
  onItemClick: (SearchContentUiItem) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: SearchViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  SearchContent(
    uiState = uiState,
    contentFocusRequester = contentFocusRequester,
    topBarFocusRequester = topBarFocusRequester,
    onKey = viewModel::onKeyInput,
    onBackspace = viewModel::onBackspace,
    onClear = viewModel::onClearInput,
    onCursorLeft = viewModel::onCursorLeft,
    onCursorRight = viewModel::onCursorRight,
    onSearch = viewModel::submitSearch,
    onSuggestionClick = viewModel::onSuggestionClick,
    onShowKeyboard = viewModel::showKeyboard,
    onHideKeyboard = viewModel::hideKeyboard,
    onItemClick = onItemClick,
    modifier = modifier,
  )
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
  val firstSuggestionFocusRequester = remember { FocusRequester() }
  val firstKeyboardKeyFocusRequester = remember { FocusRequester() }
  val searchKeyFocusRequester = remember { FocusRequester() }
  val firstResultFocusRequester = remember { FocusRequester() }
  val resultFocusRequesters = remember(uiState.sections.map(SearchSectionUiItem::id)) {
    uiState.sections.map { FocusRequester() }
  }
  var pendingFocus by remember { mutableStateOf(PendingSearchFocus.None) }
  var contentHasFocus by remember { mutableStateOf(false) }
  val resultRequester = resultFocusRequesters.firstOrNull() ?: firstResultFocusRequester
  val queryDownRequester = if (uiState.isKeyboardVisible) {
    if (uiState.suggestions.isNotEmpty()) firstSuggestionFocusRequester else firstKeyboardKeyFocusRequester
  } else {
    resultRequester
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

  fun closeKeyboardAndRestoreQueryFocus() {
    parkingFocusRequester.requestFocus()
    pendingFocus = PendingSearchFocus.Query
    onHideKeyboard()
  }

  LaunchedEffect(
    pendingFocus,
    uiState.isKeyboardVisible,
    uiState.isSearching,
    uiState.submittedQuery,
    uiState.sections,
  ) {
    when (pendingFocus) {
      PendingSearchFocus.Query -> if (!uiState.isKeyboardVisible) {
        awaitFrame()
        contentFocusRequester.requestFocus()
        pendingFocus = PendingSearchFocus.None
      }

      PendingSearchFocus.Results ->
        if (!uiState.isKeyboardVisible && !uiState.isSearching && uiState.sections.isNotEmpty()) {
          awaitFrame()
          resultRequester.requestFocus()
          pendingFocus = PendingSearchFocus.None
        }

      PendingSearchFocus.None -> Unit
    }
  }

  BackHandler(enabled = uiState.isKeyboardVisible && contentHasFocus) {
    closeKeyboardAndRestoreQueryFocus()
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
          start = SearchScreenDefaults.HorizontalPadding,
          top = StreamTvDimensions.TopBarHeight + 12.dp,
          end = 24.dp,
          bottom = 18.dp,
        ),
    ) {
      SearchQuerySurface(
        query = uiState.query,
        cursorPosition = uiState.cursorPosition,
        showCaret = uiState.isKeyboardVisible,
        contentFocusRequester = contentFocusRequester,
        topBarFocusRequester = topBarFocusRequester,
        downFocusRequester = queryDownRequester,
        onClick = onShowKeyboard,
      )

      AnimatedVisibility(
        visible = uiState.isKeyboardVisible,
        enter = fadeIn(tween(SearchScreenDefaults.KeyboardAnimationDurationMillis)) +
          expandVertically(
            animationSpec = tween(SearchScreenDefaults.KeyboardAnimationDurationMillis),
            expandFrom = Alignment.Top,
          ),
        exit = fadeOut(tween(SearchScreenDefaults.KeyboardAnimationDurationMillis)) +
          shrinkVertically(
            animationSpec = tween(SearchScreenDefaults.KeyboardAnimationDurationMillis),
            shrinkTowards = Alignment.Top,
          ),
      ) {
        SearchWorkspace(
          suggestions = uiState.suggestions,
          contentFocusRequester = contentFocusRequester,
          firstSuggestionFocusRequester = firstSuggestionFocusRequester,
          firstKeyboardKeyFocusRequester = firstKeyboardKeyFocusRequester,
          searchKeyFocusRequester = searchKeyFocusRequester,
          resultFocusRequester = resultRequester,
          onKey = onKey,
          onBackspace = onBackspace,
          onClear = onClear,
          onCursorLeft = onCursorLeft,
          onCursorRight = onCursorRight,
          onSearch = ::submitAndMoveFocus,
          onSuggestionClick = ::selectSuggestionAndMoveFocus,
        )
      }

      Spacer(modifier = Modifier.height(if (uiState.isKeyboardVisible) 12.dp else 18.dp))

      SearchResults(
        uiState = uiState,
        resultFocusRequesters = resultFocusRequesters,
        firstFallbackFocusRequester = firstResultFocusRequester,
        firstRowUpFocusRequester = if (uiState.isKeyboardVisible) searchKeyFocusRequester else contentFocusRequester,
        onItemClick = onItemClick,
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun SearchQuerySurface(
  query: String,
  cursorPosition: Int,
  showCaret: Boolean,
  contentFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
  downFocusRequester: FocusRequester,
  onClick: () -> Unit,
) {
  Surface(
    onClick = onClick,
    modifier = Modifier
      .width(SearchScreenDefaults.SearchFieldWidth)
      .height(SearchScreenDefaults.SearchFieldHeight)
      .focusRequester(contentFocusRequester)
      .focusProperties {
        up = topBarFocusRequester
        down = downFocusRequester
      }
      .testTag("search-query"),
    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(28.dp)),
    scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
    border = ClickableSurfaceDefaults.border(
      focusedBorder = Border(
        border = BorderStroke(2.dp, StreamTvColors.NeutralWhite),
        shape = RoundedCornerShape(28.dp),
      ),
    ),
    colors = ClickableSurfaceDefaults.colors(
      containerColor = StreamTvColors.Neutral90,
      focusedContainerColor = StreamTvColors.Neutral80,
      pressedContainerColor = StreamTvColors.Neutral70,
    ),
  ) {
    SearchCursorTextField(
      text = query,
      cursorPosition = cursorPosition,
      showCaret = showCaret,
      modifier = Modifier.fillMaxSize(),
    )
  }
}

@Composable
private fun SearchWorkspace(
  suggestions: List<String>,
  contentFocusRequester: FocusRequester,
  firstSuggestionFocusRequester: FocusRequester,
  firstKeyboardKeyFocusRequester: FocusRequester,
  searchKeyFocusRequester: FocusRequester,
  resultFocusRequester: FocusRequester,
  onKey: (String) -> Unit,
  onBackspace: () -> Unit,
  onClear: () -> Unit,
  onCursorLeft: () -> Unit,
  onCursorRight: () -> Unit,
  onSearch: () -> Unit,
  onSuggestionClick: (String) -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(SearchScreenDefaults.SearchWorkspaceHeight)
      .padding(top = 12.dp),
    horizontalArrangement = Arrangement.spacedBy(32.dp),
  ) {
    SearchSuggestions(
      suggestions = suggestions,
      firstFocusRequester = firstSuggestionFocusRequester,
      searchFieldFocusRequester = contentFocusRequester,
      keyboardFocusRequester = firstKeyboardKeyFocusRequester,
      resultFocusRequester = resultFocusRequester,
      onSuggestionClick = onSuggestionClick,
      modifier = Modifier.width(390.dp),
    )

    SearchVirtualKeyboard(
      onKey = onKey,
      onBackspace = onBackspace,
      onClear = onClear,
      onCursorLeft = onCursorLeft,
      onCursorRight = onCursorRight,
      onSearch = onSearch,
      firstKeyFocusRequester = firstKeyboardKeyFocusRequester,
      searchKeyFocusRequester = searchKeyFocusRequester,
      searchFieldFocusRequester = contentFocusRequester,
      leftExitFocusRequester = if (suggestions.isEmpty()) contentFocusRequester else firstSuggestionFocusRequester,
      resultFocusRequester = resultFocusRequester,
      modifier = Modifier
        .width(610.dp)
        .fillMaxSize(),
    )
  }
}

@Composable
private fun SearchSuggestions(
  suggestions: List<String>,
  firstFocusRequester: FocusRequester,
  searchFieldFocusRequester: FocusRequester,
  keyboardFocusRequester: FocusRequester,
  resultFocusRequester: FocusRequester,
  onSuggestionClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    suggestions.take(6).forEachIndexed { index, suggestion ->
      Surface(
        onClick = { onSuggestionClick(suggestion) },
        modifier = Modifier
          .fillMaxWidth()
          .height(38.dp)
          .then(if (index == 0) Modifier.focusRequester(firstFocusRequester) else Modifier)
          .focusProperties {
            if (index == 0) up = searchFieldFocusRequester
            right = keyboardFocusRequester
            if (index == suggestions.lastIndex.coerceAtMost(5)) down = resultFocusRequester
          }
          .testTag("search-suggestion-$index"),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
        colors = ClickableSurfaceDefaults.colors(
          containerColor = StreamTvColors.Transparent,
          contentColor = StreamTvColors.Neutral20,
          focusedContainerColor = StreamTvColors.TransparentWhite10,
          focusedContentColor = StreamTvColors.NeutralWhite,
        ),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            modifier = Modifier.size(19.dp),
          )
          Text(
            text = suggestion,
            style = StreamTvTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }
  }
}

@Composable
private fun SearchResults(
  uiState: SearchUiState,
  resultFocusRequesters: List<FocusRequester>,
  firstFallbackFocusRequester: FocusRequester,
  firstRowUpFocusRequester: FocusRequester,
  onItemClick: (SearchContentUiItem) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(22.dp),
  ) {
    item(key = "search-results-heading") {
      Text(
        text = uiState.submittedQuery?.let { "Search results for “$it”" } ?: "Recommended for you",
        color = StreamTvColors.Neutral20,
        style = StreamTvTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 2.dp),
      )
    }

    if (uiState.isLoading || uiState.isSearching) {
      item(key = "search-loading") {
        Text(
          text = if (uiState.isSearching) "Searching…" else "Loading recommendations…",
          color = StreamTvColors.Neutral40,
          style = StreamTvTheme.typography.bodyLarge,
        )
      }
    } else if (uiState.errorMessage != null) {
      item(key = "search-error") {
        Text(
          text = uiState.errorMessage,
          color = StreamTvColors.Neutral20,
          style = StreamTvTheme.typography.bodyLarge,
        )
      }
    } else {
      itemsIndexed(
        items = uiState.sections,
        key = { _, section -> section.id },
      ) { index, section ->
        val focusRequester = resultFocusRequesters.getOrNull(index) ?: firstFallbackFocusRequester
        SearchContentRow(
          section = section,
          focusRequester = focusRequester,
          upFocusRequester = if (index == 0) firstRowUpFocusRequester else resultFocusRequesters[index - 1],
          downFocusRequester = resultFocusRequesters.getOrNull(index + 1),
          onItemClick = onItemClick,
        )
      }
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
