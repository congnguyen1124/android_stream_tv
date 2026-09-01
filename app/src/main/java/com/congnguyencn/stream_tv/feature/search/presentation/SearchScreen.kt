package com.congnguyencn.stream_tv.feature.search.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvActionScreen

@Composable
internal fun SearchScreen(
  uiState: SearchUiState,
  contentFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
  onPrimaryActionClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
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
    onActionClick = onPrimaryActionClick,
    modifier = modifier,
  )
}
