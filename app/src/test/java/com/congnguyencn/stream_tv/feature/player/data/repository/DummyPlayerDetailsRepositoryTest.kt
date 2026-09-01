package com.congnguyencn.stream_tv.feature.player.data.repository

import com.congnguyencn.stream_tv.feature.player.data.source.PlayerDummyDataSource
import com.congnguyencn.stream_tv.feature.player.domain.model.PlayerDetailsRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DummyPlayerDetailsRepositoryTest {
  @Test
  fun `repository keeps launch metadata and supplies navigable comment replies`() {
    val repository = DummyPlayerDetailsRepository(PlayerDummyDataSource())

    val details = repository.getDetails(
      PlayerDetailsRequest(
        title = "Tokyo: Tradition in motion",
        description = "A journey through Asakusa.",
        ageRestriction = "P",
      ),
    )

    assertEquals("A journey through Asakusa.", details.metadata.description)
    assertEquals("P", details.metadata.ageRestriction)
    assertTrue(details.metadata.genres.contains("Culture"))
    assertTrue(details.comments.isNotEmpty())
    assertEquals(details.comments.first().replyCount.toInt(), details.repliesByCommentId.getValue(1).size)
  }
}
