package com.congnguyencn.stream_tv.feature.main.presentation

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
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
    composeRule.onNodeWithText("Calendar").assertDoesNotExist()
    composeRule.onNodeWithText("Setting").assertDoesNotExist()
    composeRule.onNodeWithText("Profile").assertDoesNotExist()
  }

  @Test
  fun calendarItemNavigatesWithoutTakingFocusOffTheTopBar() {
    composeRule
      .onNodeWithTag("home-banner-carousel")
      .performKeyInput {
        pressKey(Key.DirectionUp)
        pressKey(Key.DirectionRight)
        pressKey(Key.DirectionCenter)
      }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag("calendar-focused-stack").assertIsDisplayed()
    composeRule.onNodeWithText("Calendar").assertIsFocused()
  }

  @Test
  fun calendarTopBarDownKeyHandsFocusToTheProgramSelector() {
    composeRule
      .onNodeWithTag("home-banner-carousel")
      .performKeyInput {
        pressKey(Key.DirectionUp)
        pressKey(Key.DirectionRight)
        pressKey(Key.DirectionCenter)
      }
    composeRule.waitUntil(timeoutMillis = 3_000) {
      composeRule.onAllNodesWithTag("calendar-selected-program").fetchSemanticsNodes().isNotEmpty()
    }

    composeRule
      .onNodeWithText("Calendar")
      .performKeyInput { pressKey(Key.DirectionDown) }
    composeRule.waitForIdle()

    composeRule.onNodeWithTag("calendar-selected-program").assertIsFocused()
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
  fun searchItemNavigatesWithoutTakingFocusOffTheTopBar() {
    composeRule
      .onNodeWithTag("home-banner-carousel")
      .performKeyInput {
        pressKey(Key.DirectionUp)
        pressKey(Key.DirectionLeft)
        pressKey(Key.DirectionCenter)
      }
    composeRule.waitForIdle()

    composeRule
      .onNodeWithTag("search-query")
      .assertIsDisplayed()
      .assertIsNotFocused()
    composeRule
      .onNodeWithTag("stream-tv-top-bar-item-search")
      .assertIsFocused()
  }

  @Test
  fun topBarDownKeyHandsFocusToTheDestinationContent() {
    composeRule
      .onNodeWithTag("home-banner-carousel")
      .performKeyInput {
        pressKey(Key.DirectionUp)
        pressKey(Key.DirectionLeft)
        pressKey(Key.DirectionCenter)
      }
    composeRule.waitForIdle()

    composeRule
      .onNodeWithTag("stream-tv-top-bar-item-search")
      .performKeyInput { pressKey(Key.DirectionDown) }
    composeRule.waitForIdle()

    composeRule
      .onNodeWithTag("search-query")
      .assertIsFocused()
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
