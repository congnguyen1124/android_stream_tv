package com.congnguyencn.stream_tv.feature.setting.presentation

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
internal fun SettingScreen(
  contentFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
  modifier: Modifier = Modifier,
  viewModel: SettingViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  StreamTvActionScreen(
    title = stringResource(R.string.setting_title),
    description = stringResource(
      if (uiState.isSettingReady) {
        R.string.setting_ready_message
      } else {
        R.string.setting_description
      },
    ),
    actionText = stringResource(R.string.setting_primary_action),
    contentFocusRequester = contentFocusRequester,
    topBarFocusRequester = topBarFocusRequester,
    onActionClick = viewModel::openSetting,
    modifier = modifier,
  )
}
