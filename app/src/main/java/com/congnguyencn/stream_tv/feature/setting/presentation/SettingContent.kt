package com.congnguyencn.stream_tv.feature.setting.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.core.designsystem.tokens.StreamTvDimensions
import com.congnguyencn.stream_tv.feature.setting.presentation.component.SettingDetailPane
import com.congnguyencn.stream_tv.feature.setting.presentation.component.SettingMenu
import com.congnguyencn.stream_tv.feature.setting.presentation.component.SettingUiDefaults
import com.congnguyencn.stream_tv.feature.setting.presentation.component.hasDetailAction
import com.congnguyencn.stream_tv.feature.setting.presentation.model.SettingItemUi
import com.congnguyencn.stream_tv.feature.setting.presentation.model.SettingSystemInfoUi

/**
 * Settings as a menu beside the pane it describes.
 *
 * The two halves are one focus loop: Right hands focus from the selected entry to the pane's
 * control when it has one, Left hands it back. Nothing here claims focus on appearance — Settings
 * is only ever reached from the top bar, which keeps focus until Down is pressed.
 */
@Composable
internal fun SettingContent(
  uiState: SettingUiState,
  contentFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
  onSelectItem: (SettingItemUi) -> Unit,
  onOpenSignIn: () -> Unit,
  onClearSearchHistory: () -> Unit,
  onClearWatchHistory: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val detailActionFocusRequester = remember { FocusRequester() }
  val hasDetailAction = uiState.selectedItem.hasDetailAction(
    isSearchHistoryCleared = uiState.isSearchHistoryCleared,
    isWatchHistoryCleared = uiState.isWatchHistoryCleared,
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(
        start = StreamTvDimensions.ScreenHorizontalPadding,
        top = SettingUiDefaults.ScreenTopPadding,
        end = StreamTvDimensions.ScreenHorizontalPadding,
        bottom = SettingUiDefaults.ScreenBottomPadding,
      )
      .testTag("setting-screen"),
  ) {
    Text(
      text = stringResource(R.string.setting_title),
      color = StreamTvColors.NeutralWhite,
      style = StreamTvTheme.typography.headlineLarge.copy(
        fontSize = SettingUiDefaults.TitleFontSize,
      ),
    )

    Spacer(modifier = Modifier.height(SettingUiDefaults.TitleToBodySpacing))

    Row(modifier = Modifier.fillMaxWidth()) {
      SettingMenu(
        selectedItem = uiState.selectedItem,
        hasDetailAction = hasDetailAction,
        contentFocusRequester = contentFocusRequester,
        topBarFocusRequester = topBarFocusRequester,
        detailActionFocusRequester = detailActionFocusRequester,
        onSelectItem = onSelectItem,
      )

      Spacer(modifier = Modifier.width(SettingUiDefaults.PaneGap))

      SettingDetailPane(
        uiState = uiState,
        actionFocusRequester = detailActionFocusRequester,
        menuFocusRequester = contentFocusRequester,
        onOpenSignIn = onOpenSignIn,
        onClearSearchHistory = onClearSearchHistory,
        onClearWatchHistory = onClearWatchHistory,
        modifier = Modifier.fillMaxSize(),
      )
    }
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF010810)
@Composable
private fun SettingContentPreview() {
  StreamTvTheme {
    SettingContent(
      uiState = SettingPreviewUiState,
      contentFocusRequester = remember { FocusRequester() },
      topBarFocusRequester = remember { FocusRequester() },
      onSelectItem = {},
      onOpenSignIn = {},
      onClearSearchHistory = {},
      onClearWatchHistory = {},
    )
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF010810)
@Composable
private fun SettingContentSystemInfoPreview() {
  StreamTvTheme {
    SettingContent(
      uiState = SettingPreviewUiState.copy(
        selectedItem = SettingItemUi.ManageDevices,
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
      ),
      contentFocusRequester = remember { FocusRequester() },
      topBarFocusRequester = remember { FocusRequester() },
      onSelectItem = {},
      onOpenSignIn = {},
      onClearSearchHistory = {},
      onClearWatchHistory = {},
    )
  }
}

private val SettingPreviewUiState = SettingUiState(isLoadingSystemInfo = false)
