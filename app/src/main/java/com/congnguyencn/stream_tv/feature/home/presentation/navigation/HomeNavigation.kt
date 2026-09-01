package com.congnguyencn.stream_tv.feature.home.presentation.navigation

import androidx.compose.ui.focus.FocusRequester
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.congnguyencn.stream_tv.feature.home.presentation.HomeRoute as HomeRouteContent
import com.congnguyencn.stream_tv.feature.home.presentation.model.HomeContentUiItem

const val HomeRoute = "home"

fun NavGraphBuilder.homeScreen(
  contentFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
  onItemClick: (HomeContentUiItem) -> Unit,
) {
  composable(route = HomeRoute) {
    HomeRouteContent(
      contentFocusRequester = contentFocusRequester,
      topBarFocusRequester = topBarFocusRequester,
      onItemClick = onItemClick,
    )
  }
}
