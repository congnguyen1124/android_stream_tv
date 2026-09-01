package com.congnguyencn.stream_tv.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.congnguyencn.stream_tv.app.navigation.StreamTvNavHost

@Composable
fun StreamTvApp(modifier: Modifier = Modifier, navController: NavHostController = rememberNavController()) {
  StreamTvNavHost(
    navController = navController,
    modifier = modifier.fillMaxSize(),
  )
}
