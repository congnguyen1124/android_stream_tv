package com.congnguyencn.stream_tv.feature.player.presentation.component.section

import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerSectionNavigationStateTest {
  @Test
  fun `comment tree retains its parents and pops one layer at a time`() {
    val state = PlayerSectionNavigationState()

    state.openRoot(PlayerSection.Comments)
    assertTrue(state.isPanelEntering)
    assertTrue(state.shouldParkFocus)
    state.onSectionEnterFinished()

    state.openChild(PlayerSection.Replies(commentId = 7))
    state.onSectionEnterFinished()
    state.openChild(PlayerSection.ReplyDetail(commentId = 7, replyId = 70))
    state.onSectionEnterFinished()

    assertEquals(
      listOf(
        PlayerSection.Comments,
        PlayerSection.Replies(commentId = 7),
        PlayerSection.ReplyDetail(commentId = 7, replyId = 70),
      ),
      state.sectionLayers,
    )

    assertTrue(state.dismissCurrentSection())
    assertTrue(state.shouldParkFocus)
    state.onSectionExitFinished()
    assertEquals(PlayerSection.Replies(commentId = 7), state.panelSection)

    assertTrue(state.dismissCurrentSection())
    state.onSectionExitFinished()
    assertEquals(PlayerSection.Comments, state.panelSection)

    assertTrue(state.dismissCurrentSection())
    assertTrue(state.isReturningToBase)
    state.onSectionExitFinished()
    assertTrue(state.isAtBaseLevel)
  }

  @Test
  fun `settings child can only be pushed below the settings root`() {
    val state = PlayerSectionNavigationState()

    state.openRoot(PlayerSection.Metadata)
    state.onSectionEnterFinished()
    state.openChild(PlayerSection.SettingOptions(PlayerSettingCategory.Quality))

    assertEquals(PlayerSection.Metadata, state.panelSection)
    assertEquals(listOf(PlayerSection.Metadata), state.sectionLayers)

    state.reset()
    state.openRoot(PlayerSection.Settings)
    state.onSectionEnterFinished()
    state.openChild(PlayerSection.SettingOptions(PlayerSettingCategory.Quality))

    assertEquals(PlayerSection.SettingOptions(PlayerSettingCategory.Quality), state.panelSection)
  }

  @Test
  fun `overlapping transitions are ignored`() {
    val state = PlayerSectionNavigationState()

    state.openRoot(PlayerSection.Comments)
    state.openChild(PlayerSection.Replies(commentId = 1))
    assertEquals(listOf(PlayerSection.Comments), state.sectionLayers)

    state.dismissCurrentSection()
    assertFalse(state.dismissCurrentSection())
    state.openRoot(PlayerSection.Metadata)
    assertEquals(PlayerSection.Comments, state.exitingSection)

    state.onSectionExitFinished()
    assertNull(state.panelSection)
  }
}
