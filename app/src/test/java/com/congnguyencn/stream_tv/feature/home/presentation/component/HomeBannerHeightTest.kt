package com.congnguyencn.stream_tv.feature.home.presentation.component

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeBannerHeightTest {
  @Test
  fun `banner gives up the peek height so the next section stays visible`() {
    val viewportHeight = 540.dp

    val bannerHeight = homeBannerHeight(viewportHeight = viewportHeight)

    assertEquals(viewportHeight - HomeBannerDefaults.NextSectionPeekHeight, bannerHeight)
    assertTrue(bannerHeight < viewportHeight)
  }

  @Test
  fun `a viewport too short to spare the peek still leaves room for the info block`() {
    val bannerHeight = homeBannerHeight(viewportHeight = 300.dp)

    assertEquals(HomeBannerDefaults.MinHeight, bannerHeight)
  }

  @Test
  fun `a tall viewport stops at the height the artwork was framed at`() {
    val bannerHeight = homeBannerHeight(viewportHeight = 1_200.dp)

    assertEquals(HomeBannerDefaults.MaxHeight, bannerHeight)
  }

  @Test
  fun `every TV viewport in range keeps the banner shorter than the screen`() {
    val viewportHeights = listOf(480.dp, 540.dp, 541.dp, 600.dp, 720.dp)

    viewportHeights.forEach { viewportHeight ->
      assertTrue(
        "banner must not fill a $viewportHeight viewport",
        homeBannerHeight(viewportHeight = viewportHeight) < viewportHeight,
      )
    }
  }
}
