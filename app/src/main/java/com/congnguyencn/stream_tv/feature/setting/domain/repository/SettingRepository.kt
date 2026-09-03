package com.congnguyencn.stream_tv.feature.setting.domain.repository

import com.congnguyencn.stream_tv.feature.setting.domain.model.SettingSystemInfo

/** Reads the build and device facts the Settings screen reports. */
internal fun interface SettingRepository {
  suspend fun getSystemInfo(): SettingSystemInfo
}
