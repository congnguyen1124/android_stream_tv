package com.congnguyencn.stream_tv.feature.player.presentation

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.test.espresso.Espresso
import com.congnguyencn.stream_tv.MainActivity
import org.junit.Rule
import org.junit.Test

class PlayerNavigationTest {
  @get:Rule
  val composeRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun dpadShowsControllerThenBackHidesItBeforeReturningToHome() {
    composeRule.mainClock.autoAdvance = false
    composeRule
      .onNodeWithTag("home-banner-carousel")
      .performKeyInput { pressKey(Key.DirectionCenter) }
    settleUi()

    composeRule.onNodeWithTag("player-screen").assertIsDisplayed()
    composeRule.onNodeWithTag("player-controller").assertDoesNotExist()

    composeRule.onNodeWithTag("player-input-target")
      .assertIsFocused()
      .performKeyInput { pressKey(Key.DirectionCenter) }
    settleUi()
    composeRule.onNodeWithTag("player-controller").assertIsDisplayed()

    Espresso.pressBack()
    settleUi()
    composeRule.onNodeWithTag("player-controller").assertDoesNotExist()
    composeRule.onNodeWithTag("player-screen").assertIsDisplayed()
    composeRule.onNodeWithTag("home-banner-carousel").assertDoesNotExist()

    Espresso.pressBack()
    settleUi(durationMillis = 1_500)

    composeRule.onNodeWithTag("player-screen").assertDoesNotExist()
    composeRule.onNodeWithTag("home-banner-carousel").assertIsDisplayed()
  }

  private fun settleUi(durationMillis: Long = 500) {
    composeRule.mainClock.advanceTimeByFrame()
    composeRule.waitForIdle()
    composeRule.mainClock.advanceTimeBy(durationMillis)
    composeRule.waitForIdle()
  }
}
