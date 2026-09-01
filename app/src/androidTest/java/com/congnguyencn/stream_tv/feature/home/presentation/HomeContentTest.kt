package com.congnguyencn.stream_tv.feature.home.presentation

import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvSurface
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.home.presentation.component.HomeContent
import com.congnguyencn.stream_tv.feature.home.presentation.model.HomeSectionUiItem
import com.congnguyencn.stream_tv.feature.home.presentation.model.HomeSectionViewTypeUi
import com.congnguyencn.stream_tv.feature.home.presentation.model.VideoUiItem
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeContentTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun bannerCarouselReceivesInitialFocus() {
    setHomeContent(uiState = HomeUiState(isLoading = false, sections = listOf(bannerSection())))

    composeRule
      .onNodeWithTag("home-banner-carousel")
      .assertIsFocused()
  }

  @Test
  fun topBarOverlayStaysHiddenWhileTheOpeningSectionIsFocused() {
    val overlayVisibilityChanges = mutableListOf<Boolean>()

    setHomeContent(
      uiState = HomeUiState(isLoading = false, sections = listOf(bannerSection(), videoSection())),
      onTopBarOverlayVisibilityChange = overlayVisibilityChanges::add,
    )

    assertEquals(listOf(false), overlayVisibilityChanges)
  }

  @Test
  fun topBarOverlayIsRaisedOnceFocusLeavesTheOpeningSection() {
    val overlayVisibilityChanges = mutableListOf<Boolean>()

    setHomeContent(
      uiState = HomeUiState(isLoading = false, sections = listOf(bannerSection(), videoSection())),
      onTopBarOverlayVisibilityChange = overlayVisibilityChanges::add,
    )

    composeRule
      .onNodeWithTag("home-banner-carousel")
      .performKeyInput { pressKey(Key.DirectionDown) }
    composeRule.waitForIdle()

    assertEquals(listOf(false, true), overlayVisibilityChanges)
  }

  private fun setHomeContent(uiState: HomeUiState, onTopBarOverlayVisibilityChange: (Boolean) -> Unit = {}) {
    composeRule.setContent {
      val contentFocusRequester = remember { FocusRequester() }
      val topBarFocusRequester = remember { FocusRequester() }

      StreamTvTheme {
        StreamTvSurface {
          HomeContent(
            uiState = uiState,
            contentFocusRequester = contentFocusRequester,
            topBarFocusRequester = topBarFocusRequester,
            onItemClick = {},
            onTopBarOverlayVisibilityChange = onTopBarOverlayVisibilityChange,
          )
        }
      }
    }
  }

  private fun bannerSection() = HomeSectionUiItem(
    id = "featured",
    title = "Featured today",
    viewType = HomeSectionViewTypeUi.Banner,
    items = listOf(video(id = "video-1", title = "Pulse of the court")),
  )

  private fun videoSection() = HomeSectionUiItem(
    id = "trending",
    title = "Trending now",
    viewType = HomeSectionViewTypeUi.Videos,
    items = listOf(video(id = "video-2", title = "Second wind")),
  )

  private fun video(id: String, title: String) = VideoUiItem(
    id = id,
    videoUrl = "",
    thumbnailUrl = "https://example.com/$id.jpg",
    vastUrl = "",
    title = title,
    description = "Description",
    ageRestriction = "P",
    logoUrl = "",
  )
}
