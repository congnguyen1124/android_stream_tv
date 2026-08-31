package com.congnguyencn.stream_tv.feature.setting.presentation.navigation

import androidx.compose.ui.focus.FocusRequester
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.congnguyencn.stream_tv.feature.setting.presentation.SettingRoute as SettingRouteContent

const val SettingRoute = "setting"

fun NavGraphBuilder.settingScreen(
    contentFocusRequester: FocusRequester,
    topBarFocusRequester: FocusRequester,
) {
    composable(route = SettingRoute) {
        SettingRouteContent(
            contentFocusRequester = contentFocusRequester,
            topBarFocusRequester = topBarFocusRequester,
        )
    }
}
