package com.congnguyencn.stream_tv.feature.home.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.focus.FocusRequester
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.congnguyencn.stream_tv.feature.home.presentation.model.HomeContentUiItem

@Composable
internal fun HomeRoute(
  contentFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
  onItemClick: (HomeContentUiItem) -> Unit,
  viewModel: HomeViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  HomeScreen(
    uiState = uiState,
    contentFocusRequester = contentFocusRequester,
    topBarFocusRequester = topBarFocusRequester,
    onItemClick = onItemClick,
  )
}
