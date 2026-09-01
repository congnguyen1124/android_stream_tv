package com.congnguyencn.stream_tv.feature.player.data.source

import com.congnguyencn.stream_tv.feature.player.data.model.PlayerCommentData
import com.congnguyencn.stream_tv.feature.player.data.model.PlayerDetailsData
import com.congnguyencn.stream_tv.feature.player.data.model.PlayerMetadataData
import com.congnguyencn.stream_tv.feature.player.domain.model.PlayerDetailsRequest

internal class PlayerDummyDataSource {
  fun getDetails(request: PlayerDetailsRequest): PlayerDetailsData = PlayerDetailsData(
    metadata = request.toMetadata(),
    comments = comments,
    repliesByCommentId = repliesByCommentId,
    seekPreviewFrameUrls = seekPreviewFrameUrls,
  )

  private fun PlayerDetailsRequest.toMetadata(): PlayerMetadataData {
    val subject = when {
      title.contains("Tokyo", ignoreCase = true) || title.contains("Japanese", ignoreCase = true) ->
        SubjectMetadata(
          genres = "Culture, Travel, Documentary",
          directors = "Kenji Mori",
          cast = "Aiko Tanaka, Ren Ito",
          longDescription =
            "A cinematic portrait of living traditions, quiet rituals, and the people carrying them into modern life.",
        )

      title.contains("China", ignoreCase = true) || title.contains("festival", ignoreCase = true) ->
        SubjectMetadata(
          genres = "Culture, History, Documentary",
          directors = MayaChen,
          cast = "Lin Wei, Zhao Min",
          longDescription =
            "Color, music, and family memory come together in a celebration shaped by generations of shared tradition.",
        )

      title.contains("tiger", ignoreCase = true) || title.contains("wild", ignoreCase = true) ->
        SubjectMetadata(
          genres = "Nature, Wildlife, Documentary",
          directors = "Amelia Brooks",
          cast = "Narrated by Daniel Hart",
          longDescription =
            "Patient field photography reveals how one remarkable predator reads the forest and survives in a changing habitat.",
        )

      else -> SubjectMetadata(
        genres = "Sport, Documentary",
        directors = "Jordan Miles",
        cast = "StreamTV Sports Unit",
        longDescription =
          "Go beyond the result to see the preparation, instinct, and human decisions behind a defining performance.",
      )
    }

    return PlayerMetadataData(
      description = description.ifBlank { "A StreamTV original story." },
      longDescription = subject.longDescription,
      collectionTitle = "StreamTV Originals",
      seasonTitle = "Featured Stories",
      releaseYear = "2026",
      genres = subject.genres,
      directors = subject.directors,
      producers = "Olivia Reed, Noah Williams",
      writers = "Emma Clark",
      cast = subject.cast,
      ageRestriction = ageRestriction.orEmpty().ifBlank { "P" },
    )
  }

  private data class SubjectMetadata(
    val genres: String,
    val directors: String,
    val cast: String,
    val longDescription: String,
  )

  private companion object {
    const val MayaChen = "Maya Chen"

    /**
     * Stand-in frame strip until a real thumbnail track exists.
     *
     * Requested at 320px because these are drawn into a card barely wider than that — pulling full
     * size stills would move megabytes to render a postage stamp while the viewer is scrubbing.
     */
    val seekPreviewFrameUrls = listOf(
      "11023865/pexels-photo-11023865.jpeg",
      "12167844/pexels-photo-12167844.jpeg",
      "12343886/pexels-photo-12343886.jpeg",
      "3651820/pexels-photo-3651820.jpeg",
      "2531709/pexels-photo-2531709.jpeg",
      "3800539/pexels-photo-3800539.jpeg",
      "2404959/pexels-photo-2404959.jpeg",
      "1181298/pexels-photo-1181298.jpeg",
      "1571442/pexels-photo-1571442.jpeg",
      "1105666/pexels-photo-1105666.jpeg",
      "3771069/pexels-photo-3771069.jpeg",
      "2422915/pexels-photo-2422915.jpeg",
    ).map { photo -> "https://images.pexels.com/photos/$photo?auto=compress&cs=tinysrgb&w=320" }

    val comments = listOf(
      PlayerCommentData(
        id = 1,
        parentId = null,
        authorName = "StreamTV",
        authorAvatarUrl = null,
        isAdmin = true,
        isPinned = true,
        postedAtLabel = "1 day ago",
        content = "What detail stayed with you after watching this story?",
        replyCount = 3,
        likeCount = 124,
      ),
      PlayerCommentData(
        id = 2,
        parentId = null,
        authorName = MayaChen,
        authorAvatarUrl = null,
        isAdmin = false,
        isPinned = false,
        postedAtLabel = "3 hours ago",
        content = "The photography feels patient and intentional. Every frame has room to breathe.",
        replyCount = 2,
        likeCount = 48,
      ),
      PlayerCommentData(
        id = 3,
        parentId = null,
        authorName = "Kenji Sato",
        authorAvatarUrl = null,
        isAdmin = false,
        isPinned = false,
        postedAtLabel = "2 hours ago",
        content = "I appreciated the context around the traditions instead of only showing beautiful images.",
        replyCount = 1,
        likeCount = 31,
      ),
      PlayerCommentData(
        id = 4,
        parentId = null,
        authorName = "Oliver Grant",
        authorAvatarUrl = null,
        isAdmin = false,
        isPinned = false,
        postedAtLabel = "45 minutes ago",
        content =
          "Excellent pacing and sound design. This is exactly why documentaries work so well on a large screen.",
        replyCount = 0,
        likeCount = 19,
      ),
    )

    val repliesByCommentId = mapOf(
      1L to listOf(
        reply(
          id = 101,
          parentId = 1,
          author = "Anna Lee",
          content = "The final wide shot was unforgettable.",
        ),
        reply(
          id = 102,
          parentId = 1,
          author = "David Kim",
          content = "For me it was the quiet moment before the music returned.",
        ),
        reply(
          id = 103,
          parentId = 1,
          author = "StreamTV",
          content = "We love both of those moments too.",
          isAdmin = true,
        ),
      ),
      2L to listOf(
        reply(
          id = 201,
          parentId = 2,
          author = "Sofia Martin",
          content = "The natural light made it feel very honest.",
        ),
        reply(
          id = 202,
          parentId = 2,
          author = MayaChen,
          content = "Exactly. Nothing looked overly staged.",
        ),
      ),
      3L to listOf(
        reply(
          id = 301,
          parentId = 3,
          author = "Leo Park",
          content = "That context made the smaller gestures much more meaningful.",
        ),
      ),
    )

    fun reply(id: Long, parentId: Long, author: String, content: String, isAdmin: Boolean = false) = PlayerCommentData(
      id = id,
      parentId = parentId,
      authorName = author,
      authorAvatarUrl = null,
      isAdmin = isAdmin,
      isPinned = false,
      postedAtLabel = "Recently",
      content = content,
      replyCount = 0,
      likeCount = (id % 17) + 3,
    )
  }
}
