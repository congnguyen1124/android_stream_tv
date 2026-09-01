package com.congnguyencn.stream_tv.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.MaterialTheme
import com.congnguyencn.stream_tv.app.navigation.StreamTvNavHost
import com.congnguyencn.stream_tv.app.navigation.StreamTvTopBarItems
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvTopBar
import com.congnguyencn.stream_tv.feature.home.presentation.navigation.HomeRoute
import com.congnguyencn.stream_tv.feature.player.presentation.navigation.isPlayerRoute

@Composable
fun StreamTvApp(modifier: Modifier = Modifier, navController: NavHostController = rememberNavController()) {
  val contentFocusRequester = remember { FocusRequester() }
  val topBarFocusRequester = remember { FocusRequester() }
  var isTopBarFocused by remember { mutableStateOf(false) }
  val currentBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = currentBackStackEntry?.destination?.route
  val selectedItemId = StreamTvTopBarItems.itemFor(currentRoute)?.id
  val isPlayerVisible = isPlayerRoute(currentRoute)

  Box(modifier = modifier.fillMaxSize()) {
    StreamTvNavHost(
      navController = navController,
      contentFocusRequester = contentFocusRequester,
      topBarFocusRequester = topBarFocusRequester,
      modifier = Modifier.fillMaxSize(),
    )

    AnimatedVisibility(
      visible = isTopBarFocused && !isPlayerVisible,
      modifier = Modifier
        .fillMaxSize()
        .zIndex(ScreenOverlayZIndex),
      enter = fadeIn(animationSpec = tween(ScreenOverlayAnimationDurationMillis)),
      exit = fadeOut(animationSpec = tween(ScreenOverlayAnimationDurationMillis)),
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(MaterialTheme.colorScheme.surface.copy(alpha = ScreenOverlayAlpha))
          .testTag("stream-tv-screen-overlay"),
      )
    }

    if (isPlayerVisible) return@Box

    StreamTvTopBar(
      items = StreamTvTopBarItems.Default,
      selectedItemId = selectedItemId,
      contentFocusRequester = contentFocusRequester,
      onFocusStateChanged = { hasFocus -> isTopBarFocused = hasFocus },
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
        .zIndex(TopBarZIndex)
        .testTag("stream-tv-top-bar")
        .focusRequester(topBarFocusRequester),
    )
  }
}

private const val ScreenOverlayAlpha = 0.42f
private const val ScreenOverlayAnimationDurationMillis = 160
private const val ScreenOverlayZIndex = 5f
private const val TopBarZIndex = 10f
