package com.congnguyencn.stream_tv.feature.setting.domain.model

/**
 * The build and device facts Settings reports back to the viewer.
 *
 * Values are raw rather than formatted: the wording that surrounds them is English UI copy and
 * belongs to the presentation layer, not here.
 */
internal data class SettingSystemInfo(
  val appVersionName: String,
  val appVersionCode: Int,
  val appBuildType: String,
  val deviceManufacturer: String,
  val deviceModel: String,
  val deviceBrand: String,
  val androidRelease: String,
  val timeZoneId: String,
)
