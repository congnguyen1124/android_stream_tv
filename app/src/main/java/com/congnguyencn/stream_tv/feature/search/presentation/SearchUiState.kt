package com.congnguyencn.stream_tv.feature.search.presentation

import androidx.compose.runtime.Immutable
import com.congnguyencn.stream_tv.feature.search.presentation.model.SearchSectionUiItem

@Immutable
data class SearchUiState(
  val query: String = "",
  val cursorPosition: Int = 0,
  val isKeyboardVisible: Boolean = true,
  val isLoading: Boolean = true,
  val isSearching: Boolean = false,
  val submittedQuery: String? = null,
  val suggestions: List<String> = emptyList(),
  val sections: List<SearchSectionUiItem> = emptyList(),
  val errorMessage: String? = null,
)
