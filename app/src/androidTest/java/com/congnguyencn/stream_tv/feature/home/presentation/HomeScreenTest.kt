package com.congnguyencn.stream_tv.feature.home.presentation

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvSurface
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.home.presentation.model.HomeSectionUiItem
import com.congnguyencn.stream_tv.feature.home.presentation.model.HomeSectionViewTypeUi
import com.congnguyencn.stream_tv.feature.home.presentation.model.VideoUiItem
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun bannerCarouselReceivesInitialFocus() {
        val contentFocusRequester = FocusRequester()
        val topBarFocusRequester = FocusRequester()

        composeRule.setContent {
            StreamTvTheme {
                StreamTvSurface {
                    HomeScreen(
                        uiState = HomeUiState(
                            isLoading = false,
                            sections = listOf(testBannerSection()),
                        ),
                        contentFocusRequester = contentFocusRequester,
                        topBarFocusRequester = topBarFocusRequester,
                    )
                }
            }
        }

        composeRule
            .onNodeWithTag("home-banner-carousel")
            .assertIsFocused()
    }

    private fun testBannerSection() = HomeSectionUiItem(
        id = "featured",
        title = "Featured today",
        viewType = HomeSectionViewTypeUi.Banner,
        items = listOf(
            VideoUiItem(
                id = "video-1",
                videoUrl = "",
                thumbnailUrl = "https://example.com/video.jpg",
                vastUrl = "",
                title = "Pulse of the court",
                description = "Description",
                ageRestriction = "P",
                logoUrl = "",
            ),
        ),
    )
}
