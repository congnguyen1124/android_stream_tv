package com.congnguyencn.stream_tv.feature.search.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.congnguyencn.stream_tv.feature.search.presentation.model.SearchContentUiItem

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
