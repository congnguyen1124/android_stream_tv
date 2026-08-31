package com.congnguyencn.stream_tv.feature.home.presentation

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvSurface
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun primaryActionReceivesInitialFocus() {
        val contentFocusRequester = FocusRequester()
        val topBarFocusRequester = FocusRequester()

        composeRule.setContent {
            StreamTvTheme {
                StreamTvSurface {
                    HomeScreen(
                        uiState = HomeUiState(),
                        contentFocusRequester = contentFocusRequester,
                        topBarFocusRequester = topBarFocusRequester,
                        onPrimaryActionClick = {},
                    )
                }
            }
        }

        composeRule
            .onNodeWithText("Bắt đầu trải nghiệm")
            .assertIsFocused()
    }
}
