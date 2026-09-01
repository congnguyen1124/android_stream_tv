package com.congnguyencn.stream_tv.feature.main.presentation

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import com.congnguyencn.stream_tv.MainActivity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MainScreenTest {
  @get:Rule
  val composeRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun destinationLabelIsOnlyVisibleWhileItemIsFocused() {
    composeRule.onNodeWithText("Home").assertDoesNotExist()

    composeRule
      .onNodeWithTag("home-banner-carousel")
      .performKeyInput { pressKey(Key.DirectionUp) }

    composeRule.onNodeWithText("Home").assertIsDisplayed()
    composeRule.onNodeWithText("Search").assertDoesNotExist()
    composeRule.onNodeWithText("Setting").assertDoesNotExist()
    composeRule.onNodeWithText("Profile").assertDoesNotExist()
  }

  @Test
  fun focusingTopBarDimsScreenUntilFocusReturnsToContent() {
    composeRule.onNodeWithTag("stream-tv-screen-overlay").assertDoesNotExist()

    composeRule
      .onNodeWithTag("home-banner-carousel")
      .performKeyInput { pressKey(Key.DirectionUp) }

    composeRule.onNodeWithTag("stream-tv-screen-overlay").assertIsDisplayed()

    composeRule
      .onNodeWithText("Home")
      .performKeyInput { pressKey(Key.DirectionDown) }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag("stream-tv-screen-overlay").assertDoesNotExist()
  }

  @Test
  fun topBarOverlayFollowsTheFocusedHomeSection() {
    composeRule.onNodeWithTag("stream-tv-top-bar-overlay").assertDoesNotExist()

    composeRule
      .onNodeWithTag("home-banner-carousel")
      .performKeyInput { pressKey(Key.DirectionDown) }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag("stream-tv-top-bar-overlay").assertIsDisplayed()
  }

  @Test
  fun searchItemNavigatesAndMovesFocusToSearchContent() {
    composeRule
      .onNodeWithTag("home-banner-carousel")
      .performKeyInput {
        pressKey(Key.DirectionUp)
        pressKey(Key.DirectionLeft)
        pressKey(Key.DirectionCenter)
      }

    composeRule
      .onNodeWithText("Open search")
      .assertIsDisplayed()
  }

  @Test
  fun homeBannerStartsBehindTopBar() {
    val bannerBounds = composeRule
      .onNodeWithTag("home-banner-container")
      .fetchSemanticsNode()
      .boundsInRoot
    val topBarBounds = composeRule
      .onNodeWithTag("stream-tv-top-bar")
      .fetchSemanticsNode()
      .boundsInRoot

    assertTrue(bannerBounds.top <= topBarBounds.top)
    assertTrue(bannerBounds.bottom > topBarBounds.bottom)
  }
}
