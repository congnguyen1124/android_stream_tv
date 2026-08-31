package com.congnguyencn.stream_tv.feature.home.data

import com.congnguyencn.stream_tv.feature.home.data.repository.DummyHomeRepository
import com.congnguyencn.stream_tv.feature.home.data.source.HomeDummyDataSource
import com.congnguyencn.stream_tv.feature.home.domain.model.Content
import com.congnguyencn.stream_tv.feature.home.domain.model.HomeSectionViewType
import com.congnguyencn.stream_tv.feature.home.domain.model.Series
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DummyHomeRepositoryTest {
    @Test
    fun `dummy source maps every supported view type with valid content`() {
        val sections = DummyHomeRepository(HomeDummyDataSource()).getHomeSections()

        assertEquals(HomeSectionViewType.entries.toSet(), sections.map { it.viewType }.toSet())
        assertTrue(sections.all { section -> section.items.all(section.viewType::accepts) })
    }

    @Test
    fun `dummy playback and logo urls remain empty`() {
        val content = DummyHomeRepository(HomeDummyDataSource())
            .getHomeSections()
            .flatMap { it.items }
            .flatMap { item -> listOf(item) + item.episodesOrEmpty() }

        assertTrue(content.all { it.videoUrl.isEmpty() })
        assertTrue(content.all { it.logoUrl.isEmpty() })
        assertTrue(content.all { it.thumbnailUrl.startsWith("https://images.pexels.com/") })
    }

    private fun Content.episodesOrEmpty(): List<Content> =
        if (this is Series) episodes else emptyList()
}
