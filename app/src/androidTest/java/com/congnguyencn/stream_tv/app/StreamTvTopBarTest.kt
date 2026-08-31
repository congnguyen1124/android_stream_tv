package com.congnguyencn.stream_tv.app

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import com.congnguyencn.stream_tv.MainActivity
import org.junit.Rule
import org.junit.Test

class StreamTvTopBarTest {
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
}
