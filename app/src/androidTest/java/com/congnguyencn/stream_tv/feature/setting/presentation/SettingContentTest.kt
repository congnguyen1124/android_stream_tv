package com.congnguyencn.stream_tv.feature.setting.presentation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.tv.material3.Button
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvSurface
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.setting.presentation.model.SettingItemUi
import com.congnguyencn.stream_tv.feature.setting.presentation.model.SettingSystemInfoUi
import org.junit.Rule
import org.junit.Test

class SettingContentTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun screenDoesNotClaimFocusFromTheTopBar() {
    setSettingContent()

    composeRule.onNodeWithTag("stub-top-bar-item").assertIsFocused()
  }

  @Test
  fun topBarDownKeyFocusesTheSelectedEntry() {
    setSettingContent()

    pressOnTopBar(Key.DirectionDown)

    composeRule.onNodeWithTag("setting-item-ManageSubscription").assertIsFocused()
  }

  @Test
  fun upFromTheFirstEntryReturnsToTheTopBar() {
    setSettingContent()

    pressOnTopBar(Key.DirectionDown)
    composeRule
      .onNodeWithTag("setting-item-ManageSubscription")
      .performKeyInput { pressKey(Key.DirectionUp) }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag("stub-top-bar-item").assertIsFocused()
  }

  @Test
  fun movingThroughTheMenuSwapsTheDetailPaneWithoutACenterPress() {
    setSettingContent()

    pressOnTopBar(Key.DirectionDown)
    composeRule
      .onNodeWithTag("setting-item-ManageSubscription")
      .performKeyInput { pressKey(Key.DirectionDown) }
    composeRule
      .onNodeWithTag("setting-item-PaymentHistory")
      .performKeyInput { pressKey(Key.DirectionDown) }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag("setting-item-ManageDevices").assertIsFocused()
    waitForTag("setting-version-card")
    composeRule.onNodeWithTag("setting-device-card").assertIsDisplayed()
  }

  @Test
  fun rightEntersThePaneActionAndLeftReturnsToTheEntry() {
    setSettingContent()

    pressOnTopBar(Key.DirectionDown)
    composeRule
      .onNodeWithTag("setting-item-ManageSubscription")
      .performKeyInput { pressKey(Key.DirectionRight) }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag("setting-detail-action").assertIsFocused()

    composeRule
      .onNodeWithTag("setting-detail-action")
      .performKeyInput { pressKey(Key.DirectionLeft) }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag("setting-item-ManageSubscription").assertIsFocused()
  }

  @Test
  fun rightStaysPutWhenThePaneHasNoAction() {
    setSettingContent(initialItem = SettingItemUi.TermsOfService)

    pressOnTopBar(Key.DirectionDown)
    composeRule
      .onNodeWithTag("setting-item-TermsOfService")
      .performKeyInput { pressKey(Key.DirectionRight) }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag("setting-item-TermsOfService").assertIsFocused()
  }

  @Test
  fun clearingHistoryParksFocusOnTheMenuEntryItCameFrom() {
    setSettingContent(initialItem = SettingItemUi.ClearSearchHistory)

    pressOnTopBar(Key.DirectionDown)
    composeRule
      .onNodeWithTag("setting-item-ClearSearchHistory")
      .performKeyInput { pressKey(Key.DirectionRight) }
    composeRule.waitForIdle()
    composeRule
      .onNodeWithTag("setting-detail-action")
      .performKeyInput { pressKey(Key.DirectionCenter) }
    composeRule.waitForIdle()

    // The action that had focus is gone; focus has to be on the entry, not adrift or reset to the
    // top of the menu.
    composeRule.onNodeWithTag("setting-item-ClearSearchHistory").assertIsFocused()
    composeRule.onNodeWithText("Search history cleared on this device.").assertIsDisplayed()
  }

  private fun pressOnTopBar(key: Key) {
    composeRule
      .onNodeWithTag("stub-top-bar-item")
      .performKeyInput { pressKey(key) }
    composeRule.waitForIdle()
  }

  private fun waitForTag(tag: String) {
    composeRule.waitUntil(timeoutMillis = WaitTimeoutMillis) {
      composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
    }
  }

  /**
   * Hosts the state the screen normally reads from its ViewModel, so selection can follow focus and
   * clearing can actually remove the action — the two behaviors worth testing here.
   *
   * The stub button stands in for the shell: it owns focus on entry and hands Down to the screen.
   */
  private fun setSettingContent(initialItem: SettingItemUi = SettingItemUi.ManageSubscription) {
    composeRule.setContent {
      val contentFocusRequester = remember { FocusRequester() }
      val topBarFocusRequester = remember { FocusRequester() }
      var uiState by remember {
        mutableStateOf(
          SettingUiState(
            selectedItem = initialItem,
            isLoadingSystemInfo = false,
            systemInfo = SystemInfo,
          ),
        )
      }

      StreamTvTheme {
        StreamTvSurface {
          Button(
            onClick = {},
            modifier = Modifier
              .testTag("stub-top-bar-item")
              .focusRequester(topBarFocusRequester)
              .focusProperties { down = contentFocusRequester },
          ) {
            Text(text = "Setting")
          }

          SettingContent(
            uiState = uiState,
            contentFocusRequester = contentFocusRequester,
            topBarFocusRequester = topBarFocusRequester,
            onSelectItem = { item -> uiState = uiState.copy(selectedItem = item) },
            onOpenSignIn = {},
            onClearSearchHistory = { uiState = uiState.copy(isSearchHistoryCleared = true) },
            onClearWatchHistory = { uiState = uiState.copy(isWatchHistoryCleared = true) },
          )
        }
      }

      LaunchedEffect(Unit) { topBarFocusRequester.requestFocus() }
    }
    composeRule.waitForIdle()
  }

  private companion object {
    const val WaitTimeoutMillis = 3_000L

    val SystemInfo = SettingSystemInfoUi(
      appVersionName = "1.0",
      appVersionCode = "1",
      appBuildType = "debug",
      deviceName = "google sdk_google_atv64_arm64",
      deviceBrand = "google",
      deviceModel = "sdk_google_atv64_arm64",
      androidRelease = "16",
      timeZoneId = "Asia/Ho_Chi_Minh",
    )
  }
}
