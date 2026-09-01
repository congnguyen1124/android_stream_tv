package com.congnguyencn.stream_tv.feature.profile.presentation

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
internal fun ProfileScreen(
  contentFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
  modifier: Modifier = Modifier,
  viewModel: ProfileViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  StreamTvActionScreen(
    title = stringResource(R.string.profile_title),
    description = stringResource(
      if (uiState.isProfileReady) {
        R.string.profile_ready_message
      } else {
        R.string.profile_description
      },
    ),
    actionText = stringResource(R.string.profile_primary_action),
    contentFocusRequester = contentFocusRequester,
    topBarFocusRequester = topBarFocusRequester,
    onActionClick = viewModel::openProfile,
    modifier = modifier,
  )
}
