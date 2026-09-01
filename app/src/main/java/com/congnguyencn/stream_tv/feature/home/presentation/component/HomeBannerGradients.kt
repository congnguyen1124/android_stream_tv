package com.congnguyencn.stream_tv.feature.home.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme

/**
 * The two scrims that make the banner's text readable over arbitrary artwork.
 *
 * Both fade into `colorScheme.surface` rather than to black, so the banner dissolves into the rest of
 * Home instead of ending on a visible seam.
 */
@Composable
internal fun HomeBannerHorizontalGradient(horizontalGradientHeight: Dp, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(horizontalGradientHeight)
      .background(
        Brush.verticalGradient(
          colors = listOf(StreamTvColors.Transparent, MaterialTheme.colorScheme.surface),
        ),
      ),
  )
}

@Composable
internal fun HomeBannerVerticalGradient(verticalGradientWidth: Dp, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .fillMaxHeight()
      .width(verticalGradientWidth)
      .background(
        Brush.horizontalGradient(
          colors = listOf(MaterialTheme.colorScheme.surface, StreamTvColors.Transparent),
        ),
      ),
  )
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF0B0B0F)
@Composable
private fun HomeBannerGradientsPreview() {
  StreamTvTheme {
    // Over a flat mid-grey rather than artwork, so each scrim's falloff is what the preview shows.
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(StreamTvColors.Neutral50),
    ) {
      HomeBannerVerticalGradient(
        verticalGradientWidth = 640.dp,
        modifier = Modifier.align(Alignment.CenterStart),
      )
      HomeBannerHorizontalGradient(
        horizontalGradientHeight = 240.dp,
        modifier = Modifier.align(Alignment.BottomCenter),
      )
    }
  }
}
