package com.congnguyencn.stream_tv.feature.main.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.congnguyencn.stream_tv.feature.home.presentation.navigation.HomePlayerTarget
import com.congnguyencn.stream_tv.feature.home.presentation.navigation.HomeRoute
import com.congnguyencn.stream_tv.feature.home.presentation.navigation.homeScreen
import com.congnguyencn.stream_tv.feature.home.presentation.navigation.playerTarget
import com.congnguyencn.stream_tv.feature.profile.presentation.navigation.profileScreen
import com.congnguyencn.stream_tv.feature.search.presentation.navigation.searchScreen
import com.congnguyencn.stream_tv.feature.setting.presentation.navigation.settingScreen

/**
 * The destinations that live under the top bar.
 *
 * Kept separate from the app's outer graph so that everything reachable from the top bar shares one
 * back stack, and so that full-screen destinations — the players — cannot land inside the shell.
 */
@Composable
internal fun MainNavHost(
  navController: NavHostController,
  contentFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
  onTopBarOverlayVisibilityChange: (Boolean) -> Unit,
  onOpenPlayer: (videoUrl: String, title: String, description: String, ageRestriction: String?) -> Unit,
  onOpenVerticalPlayer: (videoUrl: String, title: String, description: String, ageRestriction: String?) -> Unit,
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
      onTopBarOverlayVisibilityChange = onTopBarOverlayVisibilityChange,
      onItemClick = { item ->
        // Portrait content has to be framed portrait, so the content type — not the section it was
        // tapped in — chooses the destination.
        when (item.playerTarget()) {
          HomePlayerTarget.Horizontal ->
            onOpenPlayer(item.videoUrl, item.title, item.description, item.ageRestriction)

          HomePlayerTarget.Vertical ->
            onOpenVerticalPlayer(item.videoUrl, item.title, item.description, item.ageRestriction)
        }
      },
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
