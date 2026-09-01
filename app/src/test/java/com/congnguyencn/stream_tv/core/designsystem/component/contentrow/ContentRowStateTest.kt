package com.congnguyencn.stream_tv.core.designsystem.component.contentrow

import org.junit.Assert.assertEquals
import org.junit.Test

class ContentRowStateTest {
    @Test
    fun `moving forward from the last item rebases to the first real index`() {
        assertEquals(0, contentRowTargetIndex(currentIndex = 9, delta = 1, itemCount = 10))
    }

    @Test
    fun `moving backward from the first item stays at the first real index`() {
        assertEquals(0, contentRowTargetIndex(currentIndex = 0, delta = -1, itemCount = 10))
    }

    @Test
    fun `shrinking data keeps selected index inside the real collection`() {
        val state = ContentRowState(initialSelectedIndex = 9)

        state.updateItemCount(10)
        state.updateItemCount(3)

        assertEquals(0, state.selectedIndex)
    }
}
