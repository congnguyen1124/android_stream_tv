package com.congnguyencn.stream_tv.feature.home.presentation.navigation

import androidx.compose.ui.focus.FocusRequester
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.congnguyencn.stream_tv.feature.home.presentation.HomeRoute as HomeRouteContent

const val HomeRoute = "home"

fun NavGraphBuilder.homeScreen(
    contentFocusRequester: FocusRequester,
    topBarFocusRequester: FocusRequester,
) {
    composable(route = HomeRoute) {
        HomeRouteContent(
            contentFocusRequester = contentFocusRequester,
            topBarFocusRequester = topBarFocusRequester,
        )
    }
}
