package com.congnguyencn.stream_tv.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.congnguyencn.stream_tv.feature.main.presentation.navigation.MainRoute
import com.congnguyencn.stream_tv.feature.main.presentation.navigation.mainScreen
import com.congnguyencn.stream_tv.feature.player.presentation.navigation.navigateToPlayer
import com.congnguyencn.stream_tv.feature.player.presentation.navigation.navigateToVerticalPlayer
import com.congnguyencn.stream_tv.feature.player.presentation.navigation.playerScreen
import com.congnguyencn.stream_tv.feature.player.presentation.navigation.verticalPlayerScreen

/**
 * The app's outer graph: the browsing shell and, alongside it, the full-screen players.
 *
 * Keeping the players out here is what lets them fill the window — the top bar belongs to the shell,
 * so playback never has to ask for it to be hidden.
 */
@Composable
internal fun StreamTvNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
  NavHost(
    navController = navController,
    startDestination = MainRoute,
    modifier = modifier,
  ) {
    mainScreen(
      onOpenPlayer = { videoUrl, title, description, ageRestriction ->
        navController.navigateToPlayer(
          videoUrl = videoUrl,
          title = title,
          description = description,
          ageRestriction = ageRestriction,
        )
      },
      onOpenVerticalPlayer = { videoUrl, title, description, ageRestriction ->
        navController.navigateToVerticalPlayer(
          videoUrl = videoUrl,
          title = title,
          description = description,
          ageRestriction = ageRestriction,
        )
      },
    )
    playerScreen(onBack = { navController.popBackStack() })
    verticalPlayerScreen(onBack = { navController.popBackStack() })
  }
}
