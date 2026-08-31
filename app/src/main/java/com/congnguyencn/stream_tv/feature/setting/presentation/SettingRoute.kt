package com.congnguyencn.stream_tv.feature.setting.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.focus.FocusRequester
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun SettingRoute(
    contentFocusRequester: FocusRequester,
    topBarFocusRequester: FocusRequester,
    viewModel: SettingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingScreen(
        uiState = uiState,
        contentFocusRequester = contentFocusRequester,
        topBarFocusRequester = topBarFocusRequester,
        onPrimaryActionClick = viewModel::openSetting,
    )
}
