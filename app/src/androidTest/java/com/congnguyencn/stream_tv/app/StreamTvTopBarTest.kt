package com.congnguyencn.stream_tv.app

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvSurface
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import org.junit.Rule
import org.junit.Test

class StreamTvTopBarTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun destinationLabelIsOnlyVisibleWhileItemIsFocused() {
        composeRule.setContent {
            StreamTvTheme {
                StreamTvSurface {
                    StreamTvApp()
                }
            }
        }

        composeRule.onNodeWithText("Home").assertDoesNotExist()

        composeRule
            .onNodeWithText("Bắt đầu trải nghiệm")
            .performKeyInput { pressKey(Key.DirectionUp) }

        composeRule.onNodeWithText("Home").assertIsDisplayed()
        composeRule.onNodeWithText("Search").assertDoesNotExist()
        composeRule.onNodeWithText("Setting").assertDoesNotExist()
        composeRule.onNodeWithText("Profile").assertDoesNotExist()
    }
}
