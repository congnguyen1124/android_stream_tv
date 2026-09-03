package com.congnguyencn.stream_tv.feature.setting.presentation

import androidx.compose.runtime.Immutable
import com.congnguyencn.stream_tv.feature.setting.presentation.model.SettingItemUi
import com.congnguyencn.stream_tv.feature.setting.presentation.model.SettingMenuUi
import com.congnguyencn.stream_tv.feature.setting.presentation.model.SettingSystemInfoUi

/**
 * The Settings screen state.
 *
 * [selectedItem] is the whole navigation model of this screen: the menu shows it as selected and the
 * detail pane renders it. Selection follows focus, so it changes as the viewer moves through the
 * menu rather than only on a center press.
 */
@Immutable
internal data class SettingUiState(
  val selectedItem: SettingItemUi = SettingMenuUi.FirstItem,
  val isLoadingSystemInfo: Boolean = true,
  val systemInfo: SettingSystemInfoUi? = null,
  val systemInfoErrorMessage: String? = null,
  val isSearchHistoryCleared: Boolean = false,
  val isWatchHistoryCleared: Boolean = false,
)
