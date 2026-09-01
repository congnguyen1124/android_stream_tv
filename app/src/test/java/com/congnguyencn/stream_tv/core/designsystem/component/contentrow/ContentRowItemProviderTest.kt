package com.congnguyencn.stream_tv.core.designsystem.component.contentrow

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentRowItemProviderTest {
    @Test
    fun `more than five items append one complete duplicate cycle`() {
        val provider = loopingProvider(itemCount = 6)

        assertTrue(provider.isLoopingEnabled)
        assertEquals(12, provider.itemCount)
        assertNotEquals(provider.getKey(0), provider.getKey(6))
        assertEquals(provider.getContentType(0), provider.getContentType(6))
    }

    @Test
    fun `five items keep a single finite cycle`() {
        val provider = loopingProvider(itemCount = 5)

        assertFalse(provider.isLoopingEnabled)
        assertEquals(5, provider.itemCount)
    }

    private fun loopingProvider(itemCount: Int): LoopingContentRowItemProvider {
        val source = ContentRowScopeImpl().apply {
            items(
                count = itemCount,
                key = { index -> "item-$index" },
                contentType = { "content" },
            ) { }
        }.build()
        return LoopingContentRowItemProvider(source)
    }
}
