package com.congnguyencn.stream_tv.feature.setting.presentation.model

import androidx.compose.runtime.Immutable
import com.congnguyencn.stream_tv.feature.setting.domain.model.SettingSystemInfo

@Immutable
internal data class SettingSystemInfoUi(
  val appVersionName: String,
  val appVersionCode: String,
  val appBuildType: String,
  val deviceName: String,
  val deviceBrand: String,
  val deviceModel: String,
  val androidRelease: String,
  val timeZoneId: String,
)

/**
 * Keeps the panel free of the domain model, and settles the one piece of shaping the UI cannot do
 * with a format string: some devices already carry the manufacturer in the model name, and
 * repeating it reads like a bug.
 */
internal fun SettingSystemInfo.toUiModel(): SettingSystemInfoUi = SettingSystemInfoUi(
  appVersionName = appVersionName,
  appVersionCode = appVersionCode.toString(),
  appBuildType = appBuildType,
  deviceName = if (deviceModel.startsWith(prefix = deviceManufacturer, ignoreCase = true)) {
    deviceModel
  } else {
    "$deviceManufacturer $deviceModel"
  },
  deviceBrand = deviceBrand,
  deviceModel = deviceModel,
  androidRelease = androidRelease,
  timeZoneId = timeZoneId,
)
