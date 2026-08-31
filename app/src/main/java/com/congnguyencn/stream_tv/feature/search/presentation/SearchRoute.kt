package com.congnguyencn.stream_tv.feature.search.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
internal fun SearchRoute(
    contentFocusRequester: FocusRequester,
    topBarFocusRequester: FocusRequester,
    viewModel: SearchViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SearchScreen(
        uiState = uiState,
        contentFocusRequester = contentFocusRequester,
        topBarFocusRequester = topBarFocusRequester,
        onPrimaryActionClick = viewModel::openSearch,
    )
}
