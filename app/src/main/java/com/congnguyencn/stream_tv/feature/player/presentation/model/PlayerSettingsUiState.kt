package com.congnguyencn.stream_tv.feature.player.presentation.model

import androidx.compose.runtime.Immutable

/** The three track groups supported by StreamTV's shared player settings tree. */
internal enum class PlayerSettingCategory {
  Quality,
  Subtitles,
  Audio,
}

/** One selectable track inside a player setting detail section. */
@Immutable
internal data class PlayerSettingOptionUiItem(val id: String, val label: String, val isSelected: Boolean)

/** One row in the setting root and all options owned by its child section. */
@Immutable
internal data class PlayerSettingUiItem(
  val category: PlayerSettingCategory,
  val selectedLabel: String,
  val options: List<PlayerSettingOptionUiItem>,
)

/** Complete render state shared by the horizontal and vertical player setting panels. */
@Immutable
internal data class PlayerSettingsUiState(val items: List<PlayerSettingUiItem>) {
  val isAvailable: Boolean
    get() = items.isNotEmpty()

  fun item(category: PlayerSettingCategory): PlayerSettingUiItem? =
    items.firstOrNull { item -> item.category == category }

  companion object {
    val Empty: PlayerSettingsUiState
      get() = PlayerSettingsUiState(items = emptyList())
  }
}
