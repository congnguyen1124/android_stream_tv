package com.congnguyencn.stream_tv.feature.home.presentation.component

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeBannerPagerTest {
  @Test
  fun `horizontal banner pads two items on each edge and maps them to real indices`() {
    val realItems = listOf("A", "B", "C", "D")

    assertEquals(
      listOf("C", "D", "A", "B", "C", "D", "A", "B"),
      realItems.toLoopingBannerItems(),
    )
    assertEquals(0, 2.toBannerRealIndex(realItemCount = 4, hasLoopingEdges = true))
    assertEquals(3, 5.toBannerRealIndex(realItemCount = 4, hasLoopingEdges = true))
    assertEquals(0, 6.toBannerRealIndex(realItemCount = 4, hasLoopingEdges = true))
    assertEquals(3, 1.toBannerRealIndex(realItemCount = 4, hasLoopingEdges = true))
  }

  @Test
  fun `vertical banner starts in a distant aligned cycle and maps pages by modulo`() {
    val realItemCount = 7
    val initialPage = verticalBannerInitialPage(
      realItemCount = realItemCount,
      initialRealIndex = 3,
    )

    assertEquals(3, initialPage.toVerticalBannerRealIndex(realItemCount, isLoopingEnabled = true))
    assertEquals(4, (initialPage + 1).toVerticalBannerRealIndex(realItemCount, true))
    assertEquals(2, (initialPage + 6).toVerticalBannerRealIndex(realItemCount, true))
  }

  @Test
  fun `vertical banner stays finite below five items`() {
    assertEquals(3, 8.toVerticalBannerRealIndex(realItemCount = 4, isLoopingEnabled = false))
  }
}
