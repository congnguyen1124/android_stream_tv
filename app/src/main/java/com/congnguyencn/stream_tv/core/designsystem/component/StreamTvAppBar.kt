package com.congnguyencn.stream_tv.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceColors
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.core.designsystem.tokens.StreamTvDimensions

@Composable
fun StreamTvAppBar(modifier: Modifier = Modifier, colors: SurfaceColors = SurfaceDefaults.colors()) {
  Surface(
    modifier = modifier.height(StreamTvDimensions.AppBarHeight),
    colors = colors,
  ) {
    Row(
      modifier = Modifier
        .width(144.dp)
        .fillMaxHeight(),
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "STREAM",
        color = StreamTvColors.NeutralWhite,
        style = StreamTvTheme.typography.titleLarge,
      )
      Text(
        text = "TV",
        color = StreamTvColors.Primary40,
        style = StreamTvTheme.typography.titleLarge,
      )
    }
  }
}
