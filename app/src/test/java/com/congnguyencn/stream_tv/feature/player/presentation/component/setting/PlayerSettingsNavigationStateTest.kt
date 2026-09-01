package com.congnguyencn.stream_tv.feature.player.presentation.component.setting

import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingCategory
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingOptionUiItem
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingUiItem
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingsUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerSettingsNavigationStateTest {
  @Test
  fun `back pops child before closing the settings root`() {
    val navigationState = PlayerSettingsNavigationState()

    navigationState.open(settings)
    navigationState.openCategory(PlayerSettingCategory.Subtitles, settings)

    assertTrue(navigationState.pop())
    assertTrue(navigationState.isVisible)
    assertNull(navigationState.activeCategory)
    assertEquals(PlayerSettingCategory.Subtitles, navigationState.rootFocusCategory)

    assertTrue(navigationState.pop())
    assertFalse(navigationState.isVisible)
  }

  @Test
  fun `track removal returns an unavailable child to the first valid root row`() {
    val navigationState = PlayerSettingsNavigationState()
    navigationState.open(settings)
    navigationState.openCategory(PlayerSettingCategory.Subtitles, settings)

    val qualityOnly = PlayerSettingsUiState(
      items = settings.items.filter { item -> item.category == PlayerSettingCategory.Quality },
    )
    navigationState.sync(qualityOnly)

    assertTrue(navigationState.isVisible)
    assertNull(navigationState.activeCategory)
    assertEquals(PlayerSettingCategory.Quality, navigationState.rootFocusCategory)
  }

  @Test
  fun `empty track state closes the settings tree`() {
    val navigationState = PlayerSettingsNavigationState()
    navigationState.open(settings)

    navigationState.sync(PlayerSettingsUiState.Empty)

    assertFalse(navigationState.isVisible)
    assertNull(navigationState.rootFocusCategory)
  }

  private companion object {
    val settings = PlayerSettingsUiState(
      items = listOf(
        setting(PlayerSettingCategory.Quality),
        setting(PlayerSettingCategory.Subtitles),
        setting(PlayerSettingCategory.Audio),
      ),
    )

    fun setting(category: PlayerSettingCategory): PlayerSettingUiItem = PlayerSettingUiItem(
      category = category,
      selectedLabel = "Selected",
      options = listOf(
        PlayerSettingOptionUiItem(id = category.name, label = category.name, isSelected = true),
      ),
    )
  }
}
