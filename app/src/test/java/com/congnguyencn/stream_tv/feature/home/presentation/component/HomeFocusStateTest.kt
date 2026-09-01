package com.congnguyencn.stream_tv.feature.home.presentation.component

import androidx.compose.runtime.saveable.SaverScope
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeFocusStateTest {
  @Test
  fun `opens on the first section`() {
    assertEquals(0, HomeFocusState(initialFocusedSectionIndex = 0).focusedSectionIndex)
  }

  @Test
  fun `records the section that takes focus`() {
    val state = HomeFocusState(initialFocusedSectionIndex = 0)

    state.focusSection(index = 2)

    assertEquals(2, state.focusedSectionIndex)
  }

  @Test
  fun `ignores a negative index rather than recording an unreachable section`() {
    val state = HomeFocusState(initialFocusedSectionIndex = 1)

    state.focusSection(index = -1)

    assertEquals(1, state.focusedSectionIndex)
  }

  @Test
  fun `clamps a remembered index into a catalogue that came back shorter`() {
    val state = HomeFocusState(initialFocusedSectionIndex = 5)

    state.updateSectionCount(sectionCount = 3)

    assertEquals(2, state.focusedSectionIndex)
  }

  @Test
  fun `keeps the remembered index while the catalogue is still empty`() {
    val state = HomeFocusState(initialFocusedSectionIndex = 3)

    state.updateSectionCount(sectionCount = 0)

    assertEquals(3, state.focusedSectionIndex)
  }

  @Test
  fun `survives a save and restore round trip`() {
    val state = HomeFocusState(initialFocusedSectionIndex = 0).apply { focusSection(index = 4) }

    val saverScope = SaverScope { true }
    val saved = requireNotNull(with(HomeFocusState.Saver) { saverScope.save(state) })
    val restored = requireNotNull(HomeFocusState.Saver.restore(saved))

    assertEquals(4, restored.focusedSectionIndex)
  }
}
