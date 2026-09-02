package com.congnguyencn.stream_tv.feature.home.data

import com.congnguyencn.stream_tv.feature.home.data.repository.DummyHomeRepository
import com.congnguyencn.stream_tv.feature.home.data.source.HomeDummyDataSource
import com.congnguyencn.stream_tv.feature.home.domain.model.Content
import com.congnguyencn.stream_tv.feature.home.domain.model.HomeSectionViewType
import com.congnguyencn.stream_tv.feature.home.domain.model.Series
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DummyHomeRepositoryTest {
  @Test
  fun `dummy source maps every supported view type with valid content`() = runTest {
    val sections = DummyHomeRepository(HomeDummyDataSource()).getHomeSections()

    assertEquals(HomeSectionViewType.entries.toSet(), sections.map { it.viewType }.toSet())
    assertTrue(sections.all { section -> section.items.all(section.viewType::accepts) })
  }

  @Test
  fun `every dummy item carries a playable hls stream`() = runTest {
    val content = dummyContent()

    // The player is only reachable from Home, so a single item without a stream is a dead end the
    // viewer can still navigate into.
    assertTrue(content.all { it.videoUrl.startsWith("https://") })
    assertTrue(content.all { it.videoUrl.contains(".m3u8") })
    assertTrue(content.all { it.thumbnailUrl.startsWith("https://images.pexels.com/") })
  }

  @Test
  fun `every dummy item carries a trailer that is a different stream from its feature`() = runTest {
    val content = dummyContent()

    assertTrue(content.all { it.trailerUrl.startsWith("https://") })
    assertTrue(content.all { it.trailerUrl.contains(".m3u8") })
    // The banner hands over from thumbnail to trailer in place. With the same stream on both fields
    // the hand-off is invisible, so the dummy rotation offsets them and this keeps it that way.
    assertTrue(content.none { it.trailerUrl == it.videoUrl })
  }

  @Test
  fun `dummy trailers avoid live streams so the banner's loop-back runs`() = runTest {
    // A live manifest never reaches its end, so a live trailer would silently stop covering the
    // replay path the banner depends on.
    assertTrue(dummyContent().none { it.trailerUrl.contains("/live/") || it.trailerUrl.contains("live-assets") })
  }

  @Test
  fun `live channels use live streams so the seek bar stays suppressed`() = runTest {
    val channels = DummyHomeRepository(HomeDummyDataSource())
      .getHomeSections()
      .first { it.viewType == HomeSectionViewType.Channels }
      .items

    // A live manifest reports no duration, which is what exercises the LIVE badge and the
    // seek-suppressed path in PlayerScreen. A VOD stream here would silently stop covering that.
    assertTrue(channels.all { it.videoUrl.contains("/live/") || it.videoUrl.contains("live-assets") })
  }

  @Test
  fun `dummy logo urls remain empty`() = runTest {
    assertTrue(dummyContent().all { it.logoUrl.isEmpty() })
  }

  @Test
  fun `dummy home contains both looping and finite content rows`() = runTest {
    val sectionsByType = DummyHomeRepository(HomeDummyDataSource())
      .getHomeSections()
      .associateBy { it.viewType }

    assertTrue(sectionsByType.getValue(HomeSectionViewType.Videos).items.size > 5)
    assertTrue(sectionsByType.getValue(HomeSectionViewType.VideosPopular).items.size > 5)
    assertTrue(sectionsByType.getValue(HomeSectionViewType.Channels).items.size > 5)
    assertTrue(sectionsByType.getValue(HomeSectionViewType.Shorts).items.size > 5)
    assertTrue(sectionsByType.getValue(HomeSectionViewType.ShortPopular).items.size > 5)
    assertTrue(sectionsByType.getValue(HomeSectionViewType.ListSeries).items.size <= 5)
  }

  private suspend fun dummyContent(): List<Content> = DummyHomeRepository(HomeDummyDataSource())
    .getHomeSections()
    .flatMap { it.items }
    .flatMap { item -> listOf(item) + item.episodesOrEmpty() }

  private fun Content.episodesOrEmpty(): List<Content> = if (this is Series) episodes else emptyList()
}
