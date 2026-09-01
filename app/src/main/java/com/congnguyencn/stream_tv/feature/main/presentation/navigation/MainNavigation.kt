package com.congnguyencn.stream_tv.feature.main.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.congnguyencn.stream_tv.feature.main.presentation.MainScreen

/** The browsing shell: top bar plus its own nested navigation. */
const val MainRoute = "main"

fun NavGraphBuilder.mainScreen(
  onOpenPlayer: (videoUrl: String, title: String, description: String, ageRestriction: String?) -> Unit,
  onOpenVerticalPlayer: (videoUrl: String, title: String, description: String, ageRestriction: String?) -> Unit,
) {
  composable(route = MainRoute) {
    MainScreen(
      onOpenPlayer = onOpenPlayer,
      onOpenVerticalPlayer = onOpenVerticalPlayer,
    )
  }
}
