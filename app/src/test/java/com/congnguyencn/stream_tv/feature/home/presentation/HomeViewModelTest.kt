package com.congnguyencn.stream_tv.feature.home.presentation

import com.congnguyencn.stream_tv.core.testing.MainDispatcherRule
import com.congnguyencn.stream_tv.feature.home.domain.model.HomeSection
import com.congnguyencn.stream_tv.feature.home.domain.model.HomeSectionViewType
import com.congnguyencn.stream_tv.feature.home.domain.model.Video
import com.congnguyencn.stream_tv.feature.home.domain.repository.HomeRepository
import com.congnguyencn.stream_tv.feature.home.presentation.mapper.HomeUiMapper
import com.congnguyencn.stream_tv.feature.home.presentation.model.HomeSectionViewTypeUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  @Test
  fun `initial load maps repository data into content state`() = runTest(mainDispatcherRule.testDispatcher) {
    val repository = HomeRepository {
      listOf(
        HomeSection(
          id = "featured",
          title = "Featured",
          viewType = HomeSectionViewType.Banner,
          items = listOf(testVideo()),
        ),
      )
    }

    val viewModel = HomeViewModel(
      repository = repository,
      uiMapper = HomeUiMapper(),
    )
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertNull(state.errorMessage)
    assertEquals(HomeSectionViewTypeUi.Banner, state.sections.single().viewType)
    assertEquals("Featured", state.sections.single().title)
  }

  @Test
  fun `repository failure leaves loading and exposes an error`() = runTest(mainDispatcherRule.testDispatcher) {
    val viewModel = HomeViewModel(
      repository = HomeRepository { error("Home unavailable") },
      uiMapper = HomeUiMapper(),
    )
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals("Home unavailable", state.errorMessage)
    assertTrue(state.sections.isEmpty())
  }

  @Test
  fun `reload cancels a stale request and publishes only the latest result`() =
    runTest(mainDispatcherRule.testDispatcher) {
      var requestCount = 0
      val repository = HomeRepository {
        requestCount += 1
        if (requestCount == 1) awaitCancellation()
        listOf(
          HomeSection(
            id = "latest",
            title = "Latest",
            viewType = HomeSectionViewType.Banner,
            items = listOf(testVideo()),
          ),
        )
      }
      val viewModel = HomeViewModel(
        repository = repository,
        uiMapper = HomeUiMapper(),
      )
      runCurrent()

      viewModel.loadHome()
      advanceUntilIdle()

      assertEquals(2, requestCount)
      assertEquals("Latest", viewModel.uiState.value.sections.single().title)
      assertFalse(viewModel.uiState.value.isLoading)
    }

  private fun testVideo() = Video(
    id = "video-1",
    videoUrl = "",
    trailerUrl = "",
    thumbnailUrl = "https://example.com/video.jpg",
    vastUrl = "",
    title = "Video",
    description = "Description",
    ageRestriction = "P",
    logoUrl = "",
  )
}
