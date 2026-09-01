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
    fun `vertical banner pads five items on each edge and maps its middle page`() {
        val realItems = listOf("A", "B", "C", "D", "E")

        assertEquals(15, realItems.toLoopingVerticalBannerItems().size)
        assertEquals(2, 7.toVerticalBannerRealIndex(realItemCount = 5, isLoopingEnabled = true))
        assertEquals(4, 4.toVerticalBannerRealIndex(realItemCount = 5, isLoopingEnabled = true))
        assertEquals(0, 10.toVerticalBannerRealIndex(realItemCount = 5, isLoopingEnabled = true))
    }

    @Test
    fun `vertical banner stays finite below five items`() {
        val realItems = listOf("A", "B", "C", "D")

        assertEquals(realItems, realItems.toLoopingVerticalBannerItems())
        assertEquals(3, 8.toVerticalBannerRealIndex(realItemCount = 4, isLoopingEnabled = false))
    }
}
