package com.congnguyencn.stream_tv.feature.home.presentation.mapper

import com.congnguyencn.stream_tv.feature.home.domain.model.HomeSection
import com.congnguyencn.stream_tv.feature.home.domain.model.HomeSectionViewType
import com.congnguyencn.stream_tv.feature.home.domain.model.Series
import com.congnguyencn.stream_tv.feature.home.domain.model.Video
import com.congnguyencn.stream_tv.feature.home.presentation.model.SeriesUiItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeUiMapperTest {
  @Test
  fun `series episodes are mapped into typed ui items`() {
    val episode = video(id = "episode")
    val series = Series(
      id = "series",
      videoUrl = "",
      trailerUrl = "",
      thumbnailUrl = "https://example.com/series.jpg",
      vastUrl = "",
      title = "Series",
      description = "Description",
      ageRestriction = "P",
      logoUrl = "",
      episodes = listOf(episode),
    )
    val section = HomeSection(
      id = "series-section",
      title = "Series",
      viewType = HomeSectionViewType.ListSeries,
      items = listOf(series),
    )

    val uiItem = HomeUiMapper().map(listOf(section)).single().items.single()

    assertTrue(uiItem is SeriesUiItem)
    assertEquals("episode", (uiItem as SeriesUiItem).episodes.single().id)
  }

  private fun video(id: String) = Video(
    id = id,
    videoUrl = "",
    trailerUrl = "",
    thumbnailUrl = "https://example.com/$id.jpg",
    vastUrl = "",
    title = id,
    description = "Description",
    ageRestriction = null,
    logoUrl = "",
  )
}
