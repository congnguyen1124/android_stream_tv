package com.congnguyencn.stream_tv.feature.main.presentation.navigation

import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvTopBarItem
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvTopBarItemRole
import com.congnguyencn.stream_tv.feature.calendar.presentation.navigation.CalendarRoute
import com.congnguyencn.stream_tv.feature.home.presentation.navigation.HomeRoute
import com.congnguyencn.stream_tv.feature.profile.presentation.navigation.ProfileRoute
import com.congnguyencn.stream_tv.feature.search.presentation.navigation.SearchRoute
import com.congnguyencn.stream_tv.feature.setting.presentation.navigation.SettingRoute

/**
 * The top bar's items and the main graph destinations they stand for.
 *
 * Lives with [MainNavHost] rather than the design system because the mapping is about this app's
 * navigation, not about how a top bar looks.
 */
internal object MainTopBarItems {
  val Search = StreamTvTopBarItem(
    id = "search",
    iconResId = R.drawable.ic_search,
    titleResId = R.string.top_bar_search,
  )

  val Home = StreamTvTopBarItem(
    id = "home",
    iconResId = R.drawable.ic_home,
    titleResId = R.string.top_bar_home,
  )

  val Calendar = StreamTvTopBarItem(
    id = "calendar",
    iconResId = R.drawable.ic_calendar,
    titleResId = R.string.top_bar_calendar,
  )

  val Setting = StreamTvTopBarItem(
    id = "setting",
    iconResId = R.drawable.ic_setting,
    titleResId = R.string.top_bar_setting,
  )

  val Profile = StreamTvTopBarItem(
    id = "profile",
    iconResId = R.drawable.ic_profile,
    titleResId = R.string.top_bar_profile,
    role = StreamTvTopBarItemRole.Profile,
  )

  val Default = listOf(Search, Home, Calendar, Setting, Profile)

  private val routesByItemId = mapOf(
    Search.id to SearchRoute,
    Home.id to HomeRoute,
    Calendar.id to CalendarRoute,
    Setting.id to SettingRoute,
    Profile.id to ProfileRoute,
  )

  fun routeFor(item: StreamTvTopBarItem): String = routesByItemId.getValue(item.id)

  fun itemFor(route: String?): StreamTvTopBarItem? = Default.firstOrNull {
    routesByItemId[it.id] == route
  }
}
