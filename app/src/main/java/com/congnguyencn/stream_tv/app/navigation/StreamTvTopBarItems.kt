package com.congnguyencn.stream_tv.app.navigation

import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvTopBarItem
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvTopBarItemRole
import com.congnguyencn.stream_tv.feature.home.presentation.navigation.HomeRoute
import com.congnguyencn.stream_tv.feature.profile.presentation.navigation.ProfileRoute
import com.congnguyencn.stream_tv.feature.search.presentation.navigation.SearchRoute
import com.congnguyencn.stream_tv.feature.setting.presentation.navigation.SettingRoute

internal object StreamTvTopBarItems {
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

    val Default = listOf(Search, Home, Setting, Profile)

    private val routesByItemId = mapOf(
        Search.id to SearchRoute,
        Home.id to HomeRoute,
        Setting.id to SettingRoute,
        Profile.id to ProfileRoute,
    )

    fun routeFor(item: StreamTvTopBarItem): String = routesByItemId.getValue(item.id)

    fun itemFor(route: String?): StreamTvTopBarItem? = Default.firstOrNull {
        routesByItemId[it.id] == route
    }
}
