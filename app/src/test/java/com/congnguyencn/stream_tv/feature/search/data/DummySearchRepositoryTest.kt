package com.congnguyencn.stream_tv.feature.search.data

import com.congnguyencn.stream_tv.feature.home.domain.model.HomeSection
import com.congnguyencn.stream_tv.feature.home.domain.model.HomeSectionViewType
import com.congnguyencn.stream_tv.feature.home.domain.model.Short
import com.congnguyencn.stream_tv.feature.home.domain.model.Video
import com.congnguyencn.stream_tv.feature.home.domain.repository.HomeRepository
import com.congnguyencn.stream_tv.feature.search.data.repository.DummySearchRepository
import com.congnguyencn.stream_tv.feature.search.domain.model.SearchContentType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DummySearchRepositoryTest {
  @Test
  fun `search filters matching content and keeps section types valid`() = runTest {
    val repository = DummySearchRepository(
      HomeRepository {
        listOf(
          HomeSection(
            id = "videos",
            title = "Videos",
            viewType = HomeSectionViewType.Videos,
            items = listOf(video("tiger", "Realm of the tiger"), video("sport", "Football focus")),
          ),
          HomeSection(
            id = "shorts",
            title = "Shorts",
            viewType = HomeSectionViewType.Shorts,
            items = listOf(short("tiger-short", "The wild gaze")),
          ),
        )
      },
    )

    val result = repository.search("tiger")

    assertEquals(listOf("tiger"), result.first { it.type == SearchContentType.Video }.items.map { it.id })
    assertTrue(result.all { section -> section.items.all { item -> item.type == section.type } })
  }

  @Test
  fun `unknown query returns deterministic fallback content`() = runTest {
    val repository = DummySearchRepository(
      HomeRepository {
        listOf(
          HomeSection(
            id = "videos",
            title = "Videos",
            viewType = HomeSectionViewType.Videos,
            items = listOf(video("one", "One"), video("two", "Two")),
          ),
        )
      },
    )

    val first = repository.search("no catalog match")
    val second = repository.search("no catalog match")

    assertEquals(first, second)
    assertEquals(2, first.single().items.size)
  }

  private fun video(id: String, title: String) = Video(
    id = id,
    videoUrl = "",
    trailerUrl = "",
    thumbnailUrl = "",
    vastUrl = "",
    title = title,
    description = "$title description",
    ageRestriction = "P",
    logoUrl = "",
  )

  private fun short(id: String, title: String) = Short(
    id = id,
    videoUrl = "",
    trailerUrl = "",
    thumbnailUrl = "",
    vastUrl = "",
    title = title,
    description = "$title description",
    ageRestriction = "P",
    logoUrl = "",
  )
}
