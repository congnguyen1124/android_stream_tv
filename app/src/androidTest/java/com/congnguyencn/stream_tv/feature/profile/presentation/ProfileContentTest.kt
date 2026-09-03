package com.congnguyencn.stream_tv.feature.profile.presentation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.tv.material3.Button
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvSurface
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import org.junit.Rule
import org.junit.Test

class ProfileContentTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun pairingPanelShowsTheCodeThatTheQrSymbolCarries() {
    setProfileContent()

    composeRule.onNodeWithTag("profile-pairing-qr").assertIsDisplayed()
    composeRule.onNodeWithTag("profile-pairing-code").assertIsDisplayed()
    composeRule.onNodeWithText(PairingCode).assertIsDisplayed()
  }

  @Test
  fun screenDoesNotClaimFocusFromTheTopBar() {
    setProfileContent()

    composeRule.onNodeWithTag("stub-top-bar-item").assertIsFocused()
  }

  @Test
  fun topBarDownKeyHandsFocusToThePhoneAction() {
    setProfileContent()

    composeRule
      .onNodeWithTag("stub-top-bar-item")
      .performKeyInput { pressKey(Key.DirectionDown) }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag("profile-phone-sign-in").assertIsFocused()
  }

  @Test
  fun phoneActionUpKeyReturnsFocusToTheTopBar() {
    setProfileContent()

    composeRule
      .onNodeWithTag("stub-top-bar-item")
      .performKeyInput { pressKey(Key.DirectionDown) }
    composeRule.waitForIdle()
    composeRule
      .onNodeWithTag("profile-phone-sign-in")
      .performKeyInput { pressKey(Key.DirectionUp) }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag("stub-top-bar-item").assertIsFocused()
  }

  /**
   * Stands in for the shell: a focusable top bar item that owns focus on entry and hands Down to
   * the screen's content requester, which is how Profile is always reached.
   */
  private fun setProfileContent() {
    composeRule.setContent {
      val contentFocusRequester = remember { FocusRequester() }
      val topBarFocusRequester = remember { FocusRequester() }

      StreamTvTheme {
        StreamTvSurface {
          Button(
            onClick = {},
            modifier = Modifier
              .testTag("stub-top-bar-item")
              .focusRequester(topBarFocusRequester)
              .focusProperties { down = contentFocusRequester },
          ) {
            Text(text = "Profile")
          }

          ProfileContent(
            uiState = ProfileUiState(
              pairingUrl = PairingUrl,
              pairingCode = PairingCode,
              pairingValidUntilLabel = "14:17",
              isPhoneSignInSelected = false,
            ),
            contentFocusRequester = contentFocusRequester,
            topBarFocusRequester = topBarFocusRequester,
            onPhoneSignInClick = {},
          )
        }
      }

      LaunchedEffect(Unit) { topBarFocusRequester.requestFocus() }
    }
    composeRule.waitForIdle()
  }

  private companion object {
    const val PairingUrl = "https://tv.streamtv.example.com/pair?code=XHSZ-QBKX"
    const val PairingCode = "XHSZ-QBKX"
  }
}
