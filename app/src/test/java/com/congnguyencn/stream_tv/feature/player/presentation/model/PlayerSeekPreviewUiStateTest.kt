package com.congnguyencn.stream_tv.feature.player.presentation.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerSeekPreviewUiStateTest {
  @Test
  fun `reports no frames until a strip is fetched`() {
    assertFalse(PlayerSeekPreviewUiState.Empty.isAvailable)
    assertNull(PlayerSeekPreviewUiState.Empty.frameUrlAt(fraction = 0.5f))
  }

  @Test
  fun `splits the video into one equal slice per frame`() {
    val state = PlayerSeekPreviewUiState(frameUrls = listOf("a", "b", "c", "d"))

    assertEquals("a", state.frameUrlAt(fraction = 0f))
    assertEquals("a", state.frameUrlAt(fraction = 0.24f))
    assertEquals("b", state.frameUrlAt(fraction = 0.25f))
    assertEquals("c", state.frameUrlAt(fraction = 0.6f))
    assertEquals("d", state.frameUrlAt(fraction = 0.99f))
  }

  @Test
  fun `the end of the video resolves to the last frame rather than past it`() {
    val state = PlayerSeekPreviewUiState(frameUrls = listOf("a", "b", "c"))

    assertEquals("c", state.frameUrlAt(fraction = 1f))
  }

  @Test
  fun `a seek that overshoots either end clamps instead of failing`() {
    val state = PlayerSeekPreviewUiState(frameUrls = listOf("a", "b", "c"))

    assertEquals("a", state.frameUrlAt(fraction = -0.4f))
    assertEquals("c", state.frameUrlAt(fraction = 3.2f))
  }

  @Test
  fun `a single frame covers the whole video`() {
    val state = PlayerSeekPreviewUiState(frameUrls = listOf("only"))

    assertTrue(state.isAvailable)
    assertEquals("only", state.frameUrlAt(fraction = 0f))
    assertEquals("only", state.frameUrlAt(fraction = 1f))
  }
}
