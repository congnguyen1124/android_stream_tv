package com.congnguyencn.stream_tv.feature.player.presentation.component.setting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingCategory
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingsUiState

/** Small stack for the shared Settings root and its one active child section. */
@Stable
internal class PlayerSettingsNavigationState {
  var isVisible: Boolean by mutableStateOf(false)
    private set

  var activeCategory: PlayerSettingCategory? by mutableStateOf(null)
    private set

  var rootFocusCategory: PlayerSettingCategory? by mutableStateOf(null)
    private set

  fun open(settings: PlayerSettingsUiState) {
    if (!settings.isAvailable) return

    activeCategory = null
    rootFocusCategory = settings.items.first().category
    isVisible = true
  }

  fun openCategory(category: PlayerSettingCategory, settings: PlayerSettingsUiState) {
    if (!isVisible || settings.item(category) == null) return

    rootFocusCategory = category
    activeCategory = category
  }

  /** Pops a child first; returns false only when no setting section was open. */
  fun pop(): Boolean {
    if (!isVisible) return false

    if (activeCategory != null) {
      activeCategory = null
    } else {
      dismiss()
    }
    return true
  }

  fun dismiss() {
    isVisible = false
    activeCategory = null
  }

  /** Keeps navigation valid when Media3 publishes a new track snapshot. */
  fun sync(settings: PlayerSettingsUiState) {
    if (!settings.isAvailable) {
      dismiss()
      rootFocusCategory = null
      return
    }

    if (settings.item(activeCategory ?: rootFocusCategory ?: settings.items.first().category) == null) {
      activeCategory = null
      rootFocusCategory = settings.items.first().category
    }
  }
}

@Composable
internal fun rememberPlayerSettingsNavigationState(): PlayerSettingsNavigationState =
  remember { PlayerSettingsNavigationState() }
