package com.congnguyencn.stream_tv.feature.player.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.player.presentation.PlayerUiState
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSeekPreviewUiState
import org.junit.Rule
import org.junit.Test

class PlayerControllerSeekPreviewTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun seekBarTakesFocusWhenTheControllerOpens() {
    setController(uiState = seekableUiState())

    composeRule
      .onNodeWithTag("player-seek-control")
      .assertIsFocused()
  }

  @Test
  fun framePreviewStaysAwayUntilTheViewerScrubs() {
    setController(uiState = seekableUiState())

    composeRule
      .onNodeWithTag("player-seek-preview")
      .assertDoesNotExist()
    composeRule
      .onNodeWithTag("player-controller-title")
      .assertIsDisplayed()
  }

  @Test
  fun framePreviewAppearsOnceTheViewerSeeks() {
    setController(uiState = seekableUiState())

    composeRule
      .onNodeWithTag("player-seek-control")
      .performKeyInput { pressKey(Key.DirectionRight) }
    composeRule.waitForIdle()

    composeRule
      .onNodeWithTag("player-seek-preview")
      .assertIsDisplayed()
  }

  @Test
  fun aVideoWithoutAFrameStripSeeksWithoutAPreview() {
    setController(uiState = seekableUiState().copy(details = PlayerUiState.Initial.details))

    composeRule
      .onNodeWithTag("player-seek-control")
      .performKeyInput { pressKey(Key.DirectionRight) }
    composeRule.waitForIdle()

    composeRule
      .onNodeWithTag("player-seek-preview")
      .assertDoesNotExist()
    composeRule
      .onNodeWithTag("player-controller-title")
      .assertIsDisplayed()
  }

  private fun setController(uiState: PlayerUiState) {
    composeRule.setContent {
      ControllerUnderTest(uiState = uiState)
    }
  }

  @Composable
  private fun ControllerUnderTest(uiState: PlayerUiState) {
    val requesters = remember {
      PlayerControllerFocusTarget.entries.associateWith { FocusRequester() }
    }

    StreamTvTheme {
      PlayerController(
        uiState = uiState,
        focusTarget = PlayerControllerFocusTarget.Progress,
        focusRequesters = requesters,
        onFocusTargetChanged = {},
        onInteraction = {},
        onTogglePlayPause = {},
        onSeekForward = {},
        onSeekBack = {},
        onTitleClick = {},
        onLikeClick = {},
        onSaveClick = {},
        onCommentClick = {},
        onSettingsClick = {},
      )
    }
  }

  private fun seekableUiState(): PlayerUiState = playerControllerPreviewUiState().let { state ->
    state.copy(
      details = state.details.copy(
        seekPreview = PlayerSeekPreviewUiState(frameUrls = List(size = 6) { index -> "frame-$index" }),
      ),
    )
  }
}
