package com.congnguyencn.stream_tv.feature.profile.presentation.navigation

import androidx.compose.ui.focus.FocusRequester
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.congnguyencn.stream_tv.feature.profile.presentation.ProfileRoute as ProfileRouteContent

const val ProfileRoute = "profile"

fun NavGraphBuilder.profileScreen(
    contentFocusRequester: FocusRequester,
    topBarFocusRequester: FocusRequester,
) {
    composable(route = ProfileRoute) {
        ProfileRouteContent(
            contentFocusRequester = contentFocusRequester,
            topBarFocusRequester = topBarFocusRequester,
        )
    }
}
