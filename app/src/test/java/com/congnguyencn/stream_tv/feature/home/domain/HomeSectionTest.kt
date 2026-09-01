package com.congnguyencn.stream_tv.feature.home.domain

import com.congnguyencn.stream_tv.feature.home.domain.model.HomeSection
import com.congnguyencn.stream_tv.feature.home.domain.model.HomeSectionViewType
import com.congnguyencn.stream_tv.feature.home.domain.model.Video
import org.junit.Assert.assertThrows
import org.junit.Test

class HomeSectionTest {
  @Test
  fun `channel section rejects a video item`() {
    assertThrows(IllegalArgumentException::class.java) {
      HomeSection(
        id = "invalid",
        title = "Invalid",
        viewType = HomeSectionViewType.Channels,
        items = listOf(testVideo()),
      )
    }
  }

  private fun testVideo() = Video(
    id = "video-1",
    videoUrl = "",
    thumbnailUrl = "https://example.com/video.jpg",
    vastUrl = "",
    title = "Video",
    description = "Description",
    ageRestriction = null,
    logoUrl = "",
  )
}
