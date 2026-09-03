package com.congnguyencn.stream_tv.feature.setting.data.repository

import android.os.Build
import com.congnguyencn.stream_tv.BuildConfig
import com.congnguyencn.stream_tv.feature.setting.domain.model.SettingSystemInfo
import com.congnguyencn.stream_tv.feature.setting.domain.repository.SettingRepository
import java.util.TimeZone

/**
 * Reports what this build actually is, rather than dummy values.
 *
 * Reading `Build` and the default time zone is the reason this is a repository at all: the fields
 * are platform state, and keeping them here is what lets the ViewModel stay free of the Android
 * framework.
 */
internal class BuildSettingRepository : SettingRepository {
  override suspend fun getSystemInfo(): SettingSystemInfo = SettingSystemInfo(
    appVersionName = BuildConfig.VERSION_NAME,
    appVersionCode = BuildConfig.VERSION_CODE,
    appBuildType = BuildConfig.BUILD_TYPE,
    deviceManufacturer = Build.MANUFACTURER,
    deviceModel = Build.MODEL,
    deviceBrand = Build.BRAND,
    androidRelease = Build.VERSION.RELEASE,
    timeZoneId = TimeZone.getDefault().id,
  )
}
