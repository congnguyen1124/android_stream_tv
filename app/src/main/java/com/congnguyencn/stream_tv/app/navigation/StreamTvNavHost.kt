package com.congnguyencn.stream_tv.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.congnguyencn.stream_tv.feature.home.presentation.navigation.HomeRoute
import com.congnguyencn.stream_tv.feature.home.presentation.navigation.homeScreen
import com.congnguyencn.stream_tv.feature.profile.presentation.navigation.profileScreen
import com.congnguyencn.stream_tv.feature.search.presentation.navigation.searchScreen
import com.congnguyencn.stream_tv.feature.setting.presentation.navigation.settingScreen

@Composable
internal fun StreamTvNavHost(
    navController: NavHostController,
    contentFocusRequester: FocusRequester,
    topBarFocusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
    ) {
        homeScreen(
            contentFocusRequester = contentFocusRequester,
            topBarFocusRequester = topBarFocusRequester,
        )
        searchScreen(
            contentFocusRequester = contentFocusRequester,
            topBarFocusRequester = topBarFocusRequester,
        )
        settingScreen(
            contentFocusRequester = contentFocusRequester,
            topBarFocusRequester = topBarFocusRequester,
        )
        profileScreen(
            contentFocusRequester = contentFocusRequester,
            topBarFocusRequester = topBarFocusRequester,
        )
    }
}
