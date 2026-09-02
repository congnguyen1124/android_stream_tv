package com.congnguyencn.stream_tv.feature.calendar.presentation.navigation

import androidx.compose.ui.focus.FocusRequester
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.congnguyencn.stream_tv.feature.calendar.presentation.CalendarRoute as CalendarRouteScreen

const val CalendarRoute = "calendar"

fun NavGraphBuilder.calendarScreen(
  contentFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
  isTopBarFocused: Boolean,
  onTopBarOverlayVisibilityChange: (Boolean) -> Unit,
) {
  composable(route = CalendarRoute) {
    CalendarRouteScreen(
      contentFocusRequester = contentFocusRequester,
      topBarFocusRequester = topBarFocusRequester,
      isTopBarFocused = isTopBarFocused,
      onTopBarOverlayVisibilityChange = onTopBarOverlayVisibilityChange,
    )
  }
}
