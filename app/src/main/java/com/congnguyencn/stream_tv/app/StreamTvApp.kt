package com.congnguyencn.stream_tv.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.congnguyencn.stream_tv.app.navigation.StreamTvNavHost
import com.congnguyencn.stream_tv.app.navigation.StreamTvTopBarItems
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvTopBar
import com.congnguyencn.stream_tv.feature.home.presentation.navigation.HomeRoute

@Composable
fun StreamTvApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val contentFocusRequester = remember { FocusRequester() }
    val topBarFocusRequester = remember { FocusRequester() }
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val selectedItemId = StreamTvTopBarItems.itemFor(currentRoute)?.id

    Box(modifier = modifier.fillMaxSize()) {
        StreamTvNavHost(
            navController = navController,
            contentFocusRequester = contentFocusRequester,
            topBarFocusRequester = topBarFocusRequester,
            modifier = Modifier.fillMaxSize(),
        )

        StreamTvTopBar(
            items = StreamTvTopBarItems.Default,
            selectedItemId = selectedItemId,
            contentFocusRequester = contentFocusRequester,
            onItemClick = { item ->
                val destinationRoute = StreamTvTopBarItems.routeFor(item)
                if (destinationRoute != currentRoute) {
                    navController.navigate(destinationRoute) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(HomeRoute) {
                            saveState = true
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(10f)
                .testTag("stream-tv-top-bar")
                .focusRequester(topBarFocusRequester),
        )
    }
}
