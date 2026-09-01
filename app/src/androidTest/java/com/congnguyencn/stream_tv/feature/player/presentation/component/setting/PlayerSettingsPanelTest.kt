package com.congnguyencn.stream_tv.feature.player.presentation.component.setting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.dp
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvSurface
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingCategory
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingOptionUiItem
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingUiItem
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingsUiState
import org.junit.Rule
import org.junit.Test

class PlayerSettingsPanelTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun childBackRestoresTheRowThatOpenedIt() {
    lateinit var navigationState: PlayerSettingsNavigationState

    composeRule.setContent {
      navigationState = rememberPlayerSettingsNavigationState()

      StreamTvTheme {
        StreamTvSurface {
          Box(modifier = Modifier.size(width = 400.dp, height = 600.dp)) {
            PlayerSettingsPanel(
              settings = settings,
              navigationState = navigationState,
              onQualitySelected = {},
              onSubtitleSelected = {},
              onAudioSelected = {},
              onDismissed = {},
              modifier = Modifier.matchParentSize(),
            )
          }
        }
      }

      LaunchedEffect(Unit) {
        navigationState.open(settings)
      }
    }

    composeRule.onNodeWithText("Settings").assertIsDisplayed()
    composeRule
      .onNodeWithTag("player-setting-quality")
      .assertIsFocused()
      .performKeyInput { pressKey(Key.DirectionCenter) }

    composeRule.waitUntil(timeoutMillis = 2_000) {
      navigationState.activeCategory == PlayerSettingCategory.Quality
    }
    composeRule
      .onNodeWithTag("player-setting-option-1080")
      .assertIsDisplayed()
      .assertIsFocused()
      .performKeyInput { pressKey(Key.DirectionLeft) }

    composeRule
      .onNodeWithTag("player-setting-quality")
      .assertIsDisplayed()
      .assertIsFocused()
  }

  private companion object {
    val settings = PlayerSettingsUiState(
      items = listOf(
        PlayerSettingUiItem(
          category = PlayerSettingCategory.Quality,
          selectedLabel = "1080p",
          options = listOf(
            PlayerSettingOptionUiItem(id = "auto", label = "Auto", isSelected = false),
            PlayerSettingOptionUiItem(id = "1080", label = "1080p", isSelected = true),
          ),
        ),
        PlayerSettingUiItem(
          category = PlayerSettingCategory.Subtitles,
          selectedLabel = "Off",
          options = listOf(
            PlayerSettingOptionUiItem(id = "off", label = "Off", isSelected = true),
          ),
        ),
      ),
    )
  }
}
