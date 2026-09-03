package com.congnguyencn.stream_tv.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceColors
import androidx.tv.material3.SurfaceDefaults
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.core.designsystem.tokens.StreamTvDimensions

private object StreamTvAppBarDefaults {
  val LogoWidth = 128.dp
}

@Composable
fun StreamTvAppBar(modifier: Modifier = Modifier, colors: SurfaceColors = SurfaceDefaults.colors()) {
  Surface(
    modifier = modifier.height(StreamTvDimensions.AppBarHeight),
    colors = colors,
  ) {
    Image(
      modifier = Modifier
        .width(StreamTvAppBarDefaults.LogoWidth)
        .fillMaxHeight()
        .align(Alignment.CenterStart),
      painter = painterResource(R.drawable.img_logo_app),
      contentDescription = null,
    )
  }
}

@Preview(device = Devices.TV_1080p)
@Composable
private fun TvAppbarPreview() {
  StreamTvTheme {
    StreamTvAppBar(
      modifier = Modifier.fillMaxWidth(),
    )
  }
}
