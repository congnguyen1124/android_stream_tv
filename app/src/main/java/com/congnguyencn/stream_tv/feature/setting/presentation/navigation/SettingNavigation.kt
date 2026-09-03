package com.congnguyencn.stream_tv.feature.setting.presentation.navigation

import androidx.compose.ui.focus.FocusRequester
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.congnguyencn.stream_tv.feature.setting.presentation.SettingScreen

const val SettingRoute = "setting"

fun NavGraphBuilder.settingScreen(
  contentFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
  onOpenSignIn: () -> Unit,
) {
  composable(route = SettingRoute) {
    SettingScreen(
      contentFocusRequester = contentFocusRequester,
      topBarFocusRequester = topBarFocusRequester,
      onOpenSignIn = onOpenSignIn,
    )
  }
}
