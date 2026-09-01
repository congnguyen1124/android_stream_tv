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
  fun `every dummy item carries a playable hls stream`() {
    val content = dummyContent()

    // The player is only reachable from Home, so a single item without a stream is a dead end the
    // viewer can still navigate into.
    assertTrue(content.all { it.videoUrl.startsWith("https://") })
    assertTrue(content.all { it.videoUrl.contains(".m3u8") })
    assertTrue(content.all { it.thumbnailUrl.startsWith("https://images.pexels.com/") })
  }

  @Test
  fun `live channels use live streams so the seek bar stays suppressed`() {
    val channels = DummyHomeRepository(HomeDummyDataSource())
      .getHomeSections()
      .first { it.viewType == HomeSectionViewType.Channels }
      .items

    // A live manifest reports no duration, which is what exercises the LIVE badge and the
    // seek-suppressed path in PlayerScreen. A VOD stream here would silently stop covering that.
    assertTrue(channels.all { it.videoUrl.contains("/live/") || it.videoUrl.contains("live-assets") })
  }

  @Test
  fun `dummy logo urls remain empty`() {
    assertTrue(dummyContent().all { it.logoUrl.isEmpty() })
  }

  @Test
  fun `dummy home contains both looping and finite content rows`() {
    val sectionsByType = DummyHomeRepository(HomeDummyDataSource())
      .getHomeSections()
      .associateBy { it.viewType }

    assertTrue(sectionsByType.getValue(HomeSectionViewType.Videos).items.size > 5)
    assertTrue(sectionsByType.getValue(HomeSectionViewType.Channels).items.size > 5)
    assertTrue(sectionsByType.getValue(HomeSectionViewType.Shorts).items.size > 5)
    assertTrue(sectionsByType.getValue(HomeSectionViewType.ListSeries).items.size <= 5)
  }

  private fun dummyContent(): List<Content> = DummyHomeRepository(HomeDummyDataSource())
    .getHomeSections()
    .flatMap { it.items }
    .flatMap { item -> listOf(item) + item.episodesOrEmpty() }

  private fun Content.episodesOrEmpty(): List<Content> = if (this is Series) episodes else emptyList()
}
