package com.congnguyencn.stream_tv.feature.player.presentation.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import org.junit.Rule
import org.junit.Test

class PlayerPlaybackIndicatorTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun aPausedPlayerRestsUnderTheBadge() {
    composeRule.setContent {
      StreamTvTheme {
        PlayerPlaybackIndicator(isPlaying = false, isIdleBadgeVisible = true)
      }
    }

    composeRule
      .onNodeWithTag("player-playback-badge")
      .assertIsDisplayed()
  }

  @Test
  fun aPlayingPlayerShowsNothingUntilSomethingChanges() {
    composeRule.setContent {
      StreamTvTheme {
        PlayerPlaybackIndicator(isPlaying = true, isIdleBadgeVisible = false)
      }
    }

    composeRule
      .onNodeWithTag("player-playback-badge")
      .assertDoesNotExist()
  }

  @Test
  fun togglingPlaybackPulsesTheBadge() {
    var isPlaying by mutableStateOf(true)
    composeRule.setContent {
      StreamTvTheme {
        PlayerPlaybackIndicator(isPlaying = isPlaying, isIdleBadgeVisible = false)
      }
    }
    composeRule.onNodeWithTag("player-playback-badge").assertDoesNotExist()

    composeRule.mainClock.autoAdvance = false
    isPlaying = false
    composeRule.mainClock.advanceTimeByFrame()
    composeRule.mainClock.advanceTimeBy(milliseconds = 120L)

    composeRule
      .onNodeWithTag("player-playback-badge")
      .assertIsDisplayed()
  }
}
