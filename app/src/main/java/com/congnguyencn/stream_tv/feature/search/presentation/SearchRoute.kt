package com.congnguyencn.stream_tv.feature.search.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvActionScreen

@Composable
internal fun SearchScreen(
  contentFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
  modifier: Modifier = Modifier,
  viewModel: SearchViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  StreamTvActionScreen(
    title = stringResource(R.string.search_title),
    description = stringResource(
      if (uiState.isSearchReady) {
        R.string.search_ready_message
      } else {
        R.string.search_description
      },
    ),
    actionText = stringResource(R.string.search_primary_action),
    contentFocusRequester = contentFocusRequester,
    topBarFocusRequester = topBarFocusRequester,
    onActionClick = viewModel::openSearch,
    modifier = modifier,
  )
}
