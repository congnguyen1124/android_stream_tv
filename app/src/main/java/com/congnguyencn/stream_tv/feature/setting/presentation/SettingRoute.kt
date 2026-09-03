package com.congnguyencn.stream_tv.feature.setting.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun SettingScreen(
  contentFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
  onOpenSignIn: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: SettingViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  SettingContent(
    uiState = uiState,
    contentFocusRequester = contentFocusRequester,
    topBarFocusRequester = topBarFocusRequester,
    onSelectItem = viewModel::selectItem,
    onOpenSignIn = onOpenSignIn,
    onClearSearchHistory = viewModel::clearSearchHistory,
    onClearWatchHistory = viewModel::clearWatchHistory,
    modifier = modifier,
  )
}
