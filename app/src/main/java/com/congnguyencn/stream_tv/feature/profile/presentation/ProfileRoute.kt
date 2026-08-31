package com.congnguyencn.stream_tv.feature.profile.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
internal fun ProfileRoute(
    contentFocusRequester: FocusRequester,
    topBarFocusRequester: FocusRequester,
    viewModel: ProfileViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileScreen(
        uiState = uiState,
        contentFocusRequester = contentFocusRequester,
        topBarFocusRequester = topBarFocusRequester,
        onPrimaryActionClick = viewModel::openProfile,
    )
}
