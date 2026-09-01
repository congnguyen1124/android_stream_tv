package com.congnguyencn.stream_tv.feature.player.presentation.component

import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.airbnb.lottie.compose.LottieCompositionResult
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PlayerBufferingIndicatorTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun indicatorIsShown() {
    composeRule.setContent {
      StreamTvTheme {
        PlayerBufferingIndicator()
      }
    }

    composeRule
      .onNodeWithTag("player-buffering")
      .assertIsDisplayed()
  }

  /**
   * Guards the raw animation itself. A file Lottie cannot parse leaves the indicator permanently on
   * its fallback arc, which looks like a working spinner — so nothing else would catch it.
   */
  @Test
  fun loadingAnimationParses() {
    lateinit var compositionResult: LottieCompositionResult

    composeRule.setContent {
      compositionResult = rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.loading_lottie),
      )
      StreamTvTheme {
        PlayerBufferingIndicator()
      }
    }

    composeRule.waitUntil { compositionResult.isComplete }

    assertNull(compositionResult.error)
    assertTrue("R.raw.loading_lottie must parse as a Lottie animation", compositionResult.isSuccess)
  }
}
