package com.congnguyencn.stream_tv.feature.calendar.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.android.awaitFrame

@Composable
internal fun CalendarRoute(
  contentFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
  isTopBarFocused: Boolean,
  onTopBarOverlayVisibilityChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: CalendarViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  DisposableEffect(onTopBarOverlayVisibilityChange) {
    onTopBarOverlayVisibilityChange(true)
    onDispose { onTopBarOverlayVisibilityChange(false) }
  }

  LaunchedEffect(isTopBarFocused, uiState.schedule) {
    if (!isTopBarFocused && uiState.schedule != null) {
      awaitFrame()
      contentFocusRequester.requestFocus()
    }
  }

  CalendarScreen(
    uiState = uiState,
    contentFocusRequester = contentFocusRequester,
    topBarFocusRequester = topBarFocusRequester,
    modifier = modifier,
  )
}
