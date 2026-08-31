package com.congnguyencn.stream_tv.feature.setting.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
internal fun SettingRoute(
    contentFocusRequester: FocusRequester,
    topBarFocusRequester: FocusRequester,
    viewModel: SettingViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingScreen(
        uiState = uiState,
        contentFocusRequester = contentFocusRequester,
        topBarFocusRequester = topBarFocusRequester,
        onPrimaryActionClick = viewModel::openSetting,
    )
}
