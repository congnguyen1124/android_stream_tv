package com.congnguyencn.stream_tv.feature.search.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.search.presentation.SearchUiState
import com.congnguyencn.stream_tv.feature.search.presentation.model.SearchContentTypeUi
import com.congnguyencn.stream_tv.feature.search.presentation.model.SearchContentUiItem
import com.congnguyencn.stream_tv.feature.search.presentation.model.SearchSectionUiItem

@Composable
internal fun SearchResults(
  uiState: SearchUiState,
  resultFocusRequesters: List<FocusRequester>,
  firstFallbackFocusRequester: FocusRequester,
  listState: LazyListState,
  onFirstRowNavigateUp: () -> Boolean,
  onItemClick: (SearchContentUiItem) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    state = listState,
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(18.dp),
  ) {
    item(key = "search-results-heading") {
      Text(
        text = uiState.submittedQuery?.let { "Search results for “$it”" } ?: "Recommended for you",
        color = StreamTvColors.Neutral20,
        style = StreamTvTheme.typography.titleLarge,
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
          upFocusRequester = resultFocusRequesters.getOrNull(index - 1),
          downFocusRequester = resultFocusRequesters.getOrNull(index + 1),
          onNavigateUp = if (index == 0) onFirstRowNavigateUp else null,
          onItemClick = onItemClick,
        )
      }
    }
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF010810)
@Composable
private fun SearchResultsPreview() {
  val sections = listOf(
    SearchSectionUiItem(
      id = "preview-videos",
      title = "Videos",
      type = SearchContentTypeUi.Video,
      items = listOf(
        previewSearchContent("preview-video-1", "Pulse of the court"),
        previewSearchContent("preview-video-2", "Realm of the Bengal tiger"),
        previewSearchContent("preview-video-3", "Tokyo: Tradition in motion"),
      ),
    ),
  )

  StreamTvTheme {
    SearchResults(
      uiState = SearchUiState(
        isLoading = false,
        sections = sections,
      ),
      resultFocusRequesters = sections.map { remember { FocusRequester() } },
      firstFallbackFocusRequester = remember { FocusRequester() },
      listState = rememberLazyListState(),
      onFirstRowNavigateUp = { true },
      onItemClick = {},
      modifier = Modifier
        .fillMaxSize()
        .background(StreamTvColors.NeutralBlack)
        .padding(48.dp),
    )
  }
}

private fun previewSearchContent(id: String, title: String) = SearchContentUiItem(
  id = id,
  videoUrl = "",
  thumbnailUrl = "",
  title = title,
  description = "A cinematic story selected for StreamTV.",
  ageRestriction = "P",
  type = SearchContentTypeUi.Video,
)
