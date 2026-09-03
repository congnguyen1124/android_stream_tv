package com.congnguyencn.stream_tv.feature.setting.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.setting.presentation.model.SettingSystemInfoUi

/** The pane for Manage devices: what this build is, and which device is running it. */
@Composable
internal fun SettingSystemInfoDetail(
  isLoading: Boolean,
  systemInfo: SettingSystemInfoUi?,
  errorMessage: String?,
  modifier: Modifier = Modifier,
) {
  when {
    systemInfo != null -> SettingSystemInfoCards(systemInfo = systemInfo, modifier = modifier)

    isLoading -> SettingDetailPlaceholder(text = stringResource(R.string.setting_system_loading))

    else -> SettingDetailPlaceholder(
      text = errorMessage ?: stringResource(R.string.setting_system_error),
    )
  }
}

@Composable
private fun SettingSystemInfoCards(systemInfo: SettingSystemInfoUi, modifier: Modifier = Modifier) {
  Column(modifier = modifier.fillMaxSize()) {
    SettingDetailLabel(text = stringResource(R.string.setting_system_version_label))

    Spacer(modifier = Modifier.height(SettingUiDefaults.DetailSectionLabelSpacing))

    SettingDetailCard(
      title = stringResource(R.string.setting_system_app_version, systemInfo.appVersionName),
      subtitle = stringResource(
        R.string.setting_system_app_build,
        systemInfo.appVersionCode,
        systemInfo.appBuildType,
      ),
      modifier = Modifier.testTag("setting-version-card"),
    )

    Spacer(modifier = Modifier.height(SettingUiDefaults.DetailSectionSpacing))

    SettingDetailLabel(text = stringResource(R.string.setting_system_device_label))

    Spacer(modifier = Modifier.height(SettingUiDefaults.DetailSectionLabelSpacing))

    SettingDetailCard(
      title = systemInfo.deviceName,
      subtitle = stringResource(
        R.string.setting_system_device_summary,
        systemInfo.androidRelease,
        systemInfo.deviceBrand,
        systemInfo.deviceModel,
        systemInfo.timeZoneId,
      ),
      modifier = Modifier.testTag("setting-device-card"),
    )
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF010810)
@Composable
private fun SettingSystemInfoDetailPreview() {
  StreamTvTheme {
    SettingSystemInfoDetail(
      isLoading = false,
      systemInfo = SettingSystemInfoUi(
        appVersionName = "1.0",
        appVersionCode = "1",
        appBuildType = "debug",
        deviceName = "google sdk_google_atv64_arm64",
        deviceBrand = "google",
        deviceModel = "sdk_google_atv64_arm64",
        androidRelease = "16",
        timeZoneId = "Asia/Ho_Chi_Minh",
      ),
      errorMessage = null,
    )
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF010810)
@Composable
private fun SettingSystemInfoDetailLoadingPreview() {
  StreamTvTheme {
    SettingSystemInfoDetail(
      isLoading = true,
      systemInfo = null,
      errorMessage = null,
    )
  }
}
