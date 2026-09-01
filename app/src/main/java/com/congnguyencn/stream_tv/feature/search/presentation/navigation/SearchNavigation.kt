package com.congnguyencn.stream_tv.feature.search.presentation.navigation

import androidx.compose.ui.focus.FocusRequester
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.congnguyencn.stream_tv.feature.search.presentation.SearchRoute as SearchRouteContent

const val SearchRoute = "search"

fun NavGraphBuilder.searchScreen(contentFocusRequester: FocusRequester, topBarFocusRequester: FocusRequester) {
  composable(route = SearchRoute) {
    SearchRouteContent(
      contentFocusRequester = contentFocusRequester,
      topBarFocusRequester = topBarFocusRequester,
    )
  }
}
