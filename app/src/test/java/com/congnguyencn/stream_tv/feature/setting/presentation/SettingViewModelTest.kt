package com.congnguyencn.stream_tv.feature.setting.presentation

import com.congnguyencn.stream_tv.core.testing.MainDispatcherRule
import com.congnguyencn.stream_tv.feature.setting.domain.model.SettingSystemInfo
import com.congnguyencn.stream_tv.feature.setting.domain.repository.SettingRepository
import com.congnguyencn.stream_tv.feature.setting.presentation.model.SettingItemUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingViewModelTest {
  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  @Test
  fun `system info is read once the screen opens`() = runTest(mainDispatcherRule.testDispatcher) {
    val viewModel = SettingViewModel(SettingRepository { SystemInfo })
    advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoadingSystemInfo)
    assertNull(uiState.systemInfoErrorMessage)
    assertEquals("1.0", uiState.systemInfo?.appVersionName)
    assertEquals("google sdk_google_atv64_arm64", uiState.systemInfo?.deviceName)
  }

  @Test
  fun `a device that already names its manufacturer is not repeated`() = runTest(mainDispatcherRule.testDispatcher) {
    val viewModel = SettingViewModel(
      SettingRepository {
        SystemInfo.copy(deviceManufacturer = "Sony", deviceModel = "Sony BRAVIA 4K")
      },
    )
    advanceUntilIdle()

    assertEquals("Sony BRAVIA 4K", viewModel.uiState.value.systemInfo?.deviceName)
  }

  @Test
  fun `a failed read reports its message instead of loading forever`() = runTest(mainDispatcherRule.testDispatcher) {
    val viewModel = SettingViewModel(SettingRepository { error("no build information") })
    advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoadingSystemInfo)
    assertNull(uiState.systemInfo)
    assertEquals("no build information", uiState.systemInfoErrorMessage)
  }

  @Test
  fun `selecting an entry is the whole navigation model of the screen`() = runTest(mainDispatcherRule.testDispatcher) {
    val viewModel = SettingViewModel(SettingRepository { SystemInfo })
    advanceUntilIdle()

    viewModel.selectItem(SettingItemUi.PrivacyPolicy)

    assertEquals(SettingItemUi.PrivacyPolicy, viewModel.uiState.value.selectedItem)
  }

  @Test
  fun `clearing one history leaves the other alone`() = runTest(mainDispatcherRule.testDispatcher) {
    val viewModel = SettingViewModel(SettingRepository { SystemInfo })
    advanceUntilIdle()

    viewModel.clearSearchHistory()

    assertTrue(viewModel.uiState.value.isSearchHistoryCleared)
    assertFalse(viewModel.uiState.value.isWatchHistoryCleared)

    viewModel.clearWatchHistory()

    assertTrue(viewModel.uiState.value.isWatchHistoryCleared)
  }

  private companion object {
    val SystemInfo = SettingSystemInfo(
      appVersionName = "1.0",
      appVersionCode = 1,
      appBuildType = "debug",
      deviceManufacturer = "google",
      deviceModel = "sdk_google_atv64_arm64",
      deviceBrand = "google",
      androidRelease = "16",
      timeZoneId = "Asia/Ho_Chi_Minh",
    )
  }
}
