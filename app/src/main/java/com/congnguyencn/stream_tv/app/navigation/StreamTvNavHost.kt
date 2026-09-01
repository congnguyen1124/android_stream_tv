package com.congnguyencn.stream_tv.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.congnguyencn.stream_tv.feature.home.presentation.navigation.HomePlayerTarget
import com.congnguyencn.stream_tv.feature.home.presentation.navigation.HomeRoute
import com.congnguyencn.stream_tv.feature.home.presentation.navigation.homeScreen
import com.congnguyencn.stream_tv.feature.home.presentation.navigation.playerTarget
import com.congnguyencn.stream_tv.feature.player.presentation.navigation.navigateToPlayer
import com.congnguyencn.stream_tv.feature.player.presentation.navigation.navigateToVerticalPlayer
import com.congnguyencn.stream_tv.feature.player.presentation.navigation.playerScreen
import com.congnguyencn.stream_tv.feature.player.presentation.navigation.verticalPlayerScreen
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
      onItemClick = { item ->
        // Portrait content has to be framed portrait, so the content type — not the section it was
        // tapped in — chooses the destination.
        when (item.playerTarget()) {
          HomePlayerTarget.Horizontal -> navController.navigateToPlayer(
            videoUrl = item.videoUrl,
            title = item.title,
          )

          HomePlayerTarget.Vertical -> navController.navigateToVerticalPlayer(
            videoUrl = item.videoUrl,
            title = item.title,
          )
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
    playerScreen(onBack = { navController.popBackStack() })
    verticalPlayerScreen(onBack = { navController.popBackStack() })
  }
}
