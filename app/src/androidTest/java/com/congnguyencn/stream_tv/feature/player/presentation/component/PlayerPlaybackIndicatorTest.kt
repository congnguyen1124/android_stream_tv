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
  fun noUserInteractionShowsNoBadge() {
    composeRule.setContent {
      StreamTvTheme {
        PlayerPlaybackIndicator(effect = null)
      }
    }

    composeRule
      .onNodeWithTag("player-playback-badge")
      .assertDoesNotExist()
  }

  @Test
  fun aUserPlaybackInteractionShowsTheBadge() {
    var effect by mutableStateOf<PlayerPlaybackEffect?>(null)
    composeRule.setContent {
      StreamTvTheme {
        PlayerPlaybackIndicator(effect = effect)
      }
    }
    composeRule.mainClock.autoAdvance = false
    effect = PlayerPlaybackEffect(sequence = 1, glyph = PlayerPlaybackGlyph.Play)
    composeRule.mainClock.advanceTimeByFrame()
    composeRule.mainClock.advanceTimeBy(milliseconds = 96L)

    composeRule
      .onNodeWithTag("player-playback-badge")
      .assertIsDisplayed()
  }

  @Test
  fun effectLeavesAfterTheAcknowledgementAnimation() {
    var effect by mutableStateOf<PlayerPlaybackEffect?>(null)
    composeRule.setContent {
      StreamTvTheme {
        PlayerPlaybackIndicator(effect = effect)
      }
    }
    composeRule.mainClock.autoAdvance = false
    effect = PlayerPlaybackEffect(sequence = 1, glyph = PlayerPlaybackGlyph.Pause)
    composeRule.mainClock.advanceTimeByFrame()
    composeRule.mainClock.advanceTimeBy(milliseconds = 700L)

    composeRule
      .onNodeWithTag("player-playback-badge")
      .assertDoesNotExist()
  }
}
