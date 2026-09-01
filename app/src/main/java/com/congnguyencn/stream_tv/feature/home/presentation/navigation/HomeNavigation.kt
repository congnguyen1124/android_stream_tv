package com.congnguyencn.stream_tv.feature.home.presentation.navigation

import androidx.compose.ui.focus.FocusRequester
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.congnguyencn.stream_tv.feature.home.presentation.HomeScreen
import com.congnguyencn.stream_tv.feature.home.presentation.model.HomeContentUiItem

const val HomeRoute = "home"

fun NavGraphBuilder.homeScreen(
  contentFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
  isTopBarFocused: Boolean,
  onItemClick: (HomeContentUiItem) -> Unit,
  onTopBarOverlayVisibilityChange: (Boolean) -> Unit,
) {
  composable(route = HomeRoute) {
    HomeScreen(
      contentFocusRequester = contentFocusRequester,
      topBarFocusRequester = topBarFocusRequester,
      isTopBarFocused = isTopBarFocused,
      onItemClick = onItemClick,
      onTopBarOverlayVisibilityChange = onTopBarOverlayVisibilityChange,
    )
  }
}
