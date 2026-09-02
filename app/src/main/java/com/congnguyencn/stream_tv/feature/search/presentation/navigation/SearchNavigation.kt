package com.congnguyencn.stream_tv.feature.search.presentation.navigation

import androidx.compose.ui.focus.FocusRequester
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.congnguyencn.stream_tv.feature.search.presentation.SearchScreen
import com.congnguyencn.stream_tv.feature.search.presentation.model.SearchContentUiItem

const val SearchRoute = "search"

fun NavGraphBuilder.searchScreen(
  contentFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
  onItemClick: (SearchContentUiItem) -> Unit,
) {
  composable(route = SearchRoute) {
    SearchScreen(
      contentFocusRequester = contentFocusRequester,
      topBarFocusRequester = topBarFocusRequester,
      onItemClick = onItemClick,
    )
  }
}
