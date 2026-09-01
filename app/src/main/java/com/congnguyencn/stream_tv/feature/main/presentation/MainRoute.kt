package com.congnguyencn.stream_tv.feature.main.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvTopBar
import com.congnguyencn.stream_tv.feature.home.presentation.navigation.HomeRoute
import com.congnguyencn.stream_tv.feature.main.presentation.navigation.MainNavHost
import com.congnguyencn.stream_tv.feature.main.presentation.navigation.MainTopBarItems

private object MainScreenDefaults {
  const val ScreenOverlayAnimationDurationMillis = 160
  const val ScreenOverlayAlpha = 0.42f
  const val ScreenOverlayZIndex = 5f
  const val TopBarZIndex = 10f
}

/**
 * The browsing shell — top bar over its own [MainNavHost].
 *
 * Playback is deliberately not part of this graph: the players are siblings of this screen in the
 * app's outer graph, so they get the whole window without the top bar having to know they exist.
 */
@Composable
internal fun MainScreen(
  onOpenPlayer: (videoUrl: String, title: String) -> Unit,
  onOpenVerticalPlayer: (videoUrl: String, title: String) -> Unit,
  modifier: Modifier = Modifier,
  navController: NavHostController = rememberNavController(),
) {
  val contentFocusRequester = remember { FocusRequester() }
  val topBarFocusRequester = remember { FocusRequester() }
  var isTopBarFocused by remember { mutableStateOf(false) }
  var isTopBarOverlayVisible by remember { mutableStateOf(false) }
  val currentBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = currentBackStackEntry?.destination?.route
  val selectedItemId = MainTopBarItems.itemFor(currentRoute)?.id

  // The overlay is raised by whichever destination is showing, so leaving one has to lower it again
  // — the destination that asked for it is already gone by the time the next one composes.
  LaunchedEffect(currentRoute) {
    isTopBarOverlayVisible = false
  }

  Box(modifier = modifier.fillMaxSize()) {
    MainNavHost(
      navController = navController,
      contentFocusRequester = contentFocusRequester,
      topBarFocusRequester = topBarFocusRequester,
      onTopBarOverlayVisibilityChange = { isVisible -> isTopBarOverlayVisible = isVisible },
      onOpenPlayer = onOpenPlayer,
      onOpenVerticalPlayer = onOpenVerticalPlayer,
      modifier = Modifier.fillMaxSize(),
    )

    AnimatedVisibility(
      visible = isTopBarFocused,
      modifier = Modifier
        .fillMaxSize()
        .zIndex(MainScreenDefaults.ScreenOverlayZIndex),
      enter = fadeIn(animationSpec = tween(MainScreenDefaults.ScreenOverlayAnimationDurationMillis)),
      exit = fadeOut(animationSpec = tween(MainScreenDefaults.ScreenOverlayAnimationDurationMillis)),
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(MaterialTheme.colorScheme.surface.copy(alpha = MainScreenDefaults.ScreenOverlayAlpha))
          .testTag("stream-tv-screen-overlay"),
      )
    }

    StreamTvTopBar(
      items = MainTopBarItems.Default,
      selectedItemId = selectedItemId,
      contentFocusRequester = contentFocusRequester,
      isOverlayVisible = isTopBarOverlayVisible,
      onFocusStateChanged = { hasFocus -> isTopBarFocused = hasFocus },
      onItemClick = { item ->
        val destinationRoute = MainTopBarItems.routeFor(item)
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
        .zIndex(MainScreenDefaults.TopBarZIndex)
        .testTag("stream-tv-top-bar")
        .focusRequester(topBarFocusRequester),
    )
  }
}
