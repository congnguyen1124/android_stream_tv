package com.congnguyencn.stream_tv.feature.home.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.focus.FocusRequester
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.congnguyencn.stream_tv.feature.home.presentation.component.HomeBannerTrailer
import com.congnguyencn.stream_tv.feature.home.presentation.component.HomeContent
import com.congnguyencn.stream_tv.feature.home.presentation.model.HomeContentUiItem

@Composable
internal fun HomeScreen(
  contentFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
  onItemClick: (HomeContentUiItem) -> Unit,
  onTopBarOverlayVisibilityChange: (Boolean) -> Unit,
  viewModel: HomeViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  HomeContent(
    uiState = uiState,
    contentFocusRequester = contentFocusRequester,
    topBarFocusRequester = topBarFocusRequester,
    onItemClick = onItemClick,
    onTopBarOverlayVisibilityChange = onTopBarOverlayVisibilityChange,
    // Only the route builds a player, so a banner rendered anywhere else — a preview, a Compose test
    // — is thumbnail-only without having to say so.
    bannerTrailer = { item, isBannerFocused ->
      HomeBannerTrailer(
        item = item,
        isBannerFocused = isBannerFocused,
      )
    },
  )
}
