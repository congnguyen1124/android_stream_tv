package com.congnguyencn.stream_tv.feature.home.presentation

import com.congnguyencn.stream_tv.feature.home.domain.model.HomeSection
import com.congnguyencn.stream_tv.feature.home.domain.model.HomeSectionViewType
import com.congnguyencn.stream_tv.feature.home.domain.model.Video
import com.congnguyencn.stream_tv.feature.home.domain.repository.HomeRepository
import com.congnguyencn.stream_tv.feature.home.domain.usecase.GetHomeSectionsUseCase
import com.congnguyencn.stream_tv.feature.home.presentation.mapper.HomeUiMapper
import com.congnguyencn.stream_tv.feature.home.presentation.model.HomeSectionViewTypeUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class HomeViewModelTest {
    @Test
    fun `initial load maps repository data into content state`() {
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
            getHomeSections = GetHomeSectionsUseCase(repository),
            uiMapper = HomeUiMapper(),
        )

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(HomeSectionViewTypeUi.Banner, state.sections.single().viewType)
        assertEquals("Featured", state.sections.single().title)
    }

    private fun testVideo() = Video(
        id = "video-1",
        videoUrl = "",
        thumbnailUrl = "https://example.com/video.jpg",
        vastUrl = "",
        title = "Video",
        description = "Description",
        ageRestriction = "P",
        logoUrl = "",
    )
}
