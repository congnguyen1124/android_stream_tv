package com.congnguyencn.stream_tv.feature.player.presentation.navigation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.congnguyencn.stream_tv.feature.player.presentation.PlayerRoute as PlayerRouteContent
import com.congnguyencn.stream_tv.feature.player.presentation.VerticalPlayerRoute as VerticalPlayerRouteContent

private const val PlayerRouteBase = "player"
private const val VerticalPlayerRouteBase = "verticalPlayer"
private const val ArgVideoUrl = "videoUrl"
private const val ArgTitle = "title"
private const val ArgDescription = "description"
private const val ArgAgeRestriction = "ageRestriction"

/** Landscape playback, for videos, series episodes and live channels. */
const val PlayerRoute: String =
  "$PlayerRouteBase?$ArgVideoUrl={$ArgVideoUrl}&$ArgTitle={$ArgTitle}&" +
    "$ArgDescription={$ArgDescription}&$ArgAgeRestriction={$ArgAgeRestriction}"

/** Portrait playback, for shorts and the vertical banner. */
const val VerticalPlayerRoute: String =
  "$VerticalPlayerRouteBase?$ArgVideoUrl={$ArgVideoUrl}&$ArgTitle={$ArgTitle}&" +
    "$ArgDescription={$ArgDescription}&$ArgAgeRestriction={$ArgAgeRestriction}"

/**
 * What a player screen needs to start.
 *
 * The stream URL travels in the route rather than a content id because playback has no reason to
 * depend on the home feature's repository — an id would force the player to know where content comes
 * from, and every future caller would have to publish into the same catalogue.
 */
internal data class PlayerArgs(
  val videoUrl: String,
  val title: String,
  val description: String,
  val ageRestriction: String?,
) {
  companion object {
    fun from(savedStateHandle: SavedStateHandle): PlayerArgs = PlayerArgs(
      videoUrl = savedStateHandle.get<String>(ArgVideoUrl).orEmpty(),
      title = savedStateHandle.get<String>(ArgTitle).orEmpty(),
      description = savedStateHandle.get<String>(ArgDescription).orEmpty(),
      ageRestriction = savedStateHandle.get<String>(ArgAgeRestriction)?.takeIf(String::isNotBlank),
    )
  }
}

fun NavController.navigateToPlayer(videoUrl: String, title: String, description: String, ageRestriction: String?) {
  navigate(
    route = buildPlayerRoute(
      base = PlayerRouteBase,
      videoUrl = videoUrl,
      title = title,
      description = description,
      ageRestriction = ageRestriction,
    ),
  )
}

fun NavController.navigateToVerticalPlayer(
  videoUrl: String,
  title: String,
  description: String,
  ageRestriction: String?,
) {
  navigate(
    route = buildPlayerRoute(
      base = VerticalPlayerRouteBase,
      videoUrl = videoUrl,
      title = title,
      description = description,
      ageRestriction = ageRestriction,
    ),
  )
}

fun NavGraphBuilder.playerScreen(onBack: () -> Unit) {
  composable(
    route = PlayerRoute,
    arguments = playerArguments(),
  ) {
    PlayerRouteContent(onBack = onBack)
  }
}

fun NavGraphBuilder.verticalPlayerScreen(onBack: () -> Unit) {
  composable(
    route = VerticalPlayerRoute,
    arguments = playerArguments(),
  ) {
    VerticalPlayerRouteContent(onBack = onBack)
  }
}

private fun playerArguments() = listOf(
  navArgument(ArgVideoUrl) {
    type = NavType.StringType
    defaultValue = ""
  },
  navArgument(ArgTitle) {
    type = NavType.StringType
    defaultValue = ""
  },
  navArgument(ArgDescription) {
    type = NavType.StringType
    defaultValue = ""
  },
  navArgument(ArgAgeRestriction) {
    type = NavType.StringType
    nullable = true
    defaultValue = null
  },
)

/**
 * Percent-encodes both values before they enter the route.
 *
 * An HLS URL carries `/`, `?` and `&`, every one of which Navigation Compose would otherwise read as
 * route structure — the destination would simply not match.
 */
private fun buildPlayerRoute(
  base: String,
  videoUrl: String,
  title: String,
  description: String,
  ageRestriction: String?,
): String = "$base?$ArgVideoUrl=${Uri.encode(videoUrl)}&$ArgTitle=${Uri.encode(title)}&" +
  "$ArgDescription=${Uri.encode(description)}&$ArgAgeRestriction=${Uri.encode(ageRestriction.orEmpty())}"
