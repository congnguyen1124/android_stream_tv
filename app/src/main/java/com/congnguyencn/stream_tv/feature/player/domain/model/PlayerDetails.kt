package com.congnguyencn.stream_tv.feature.player.domain.model

data class PlayerDetailsRequest(val title: String, val description: String, val ageRestriction: String?)

data class PlayerDetails(
  val metadata: PlayerMetadata,
  val comments: List<PlayerComment>,
  val repliesByCommentId: Map<Long, List<PlayerComment>>,
  val seekPreview: PlayerSeekPreview,
)

/**
 * Still frames for the seek bar, ordered from the start of the video and evenly spaced across it.
 *
 * The spacing is not stored. Playback duration is only known once the stream is prepared, so a fixed
 * interval baked in here would drift further from the truth the longer the video ran; deriving each
 * frame's position from the count keeps the strip aligned with whatever duration the player reports.
 *
 * An empty list means the frames are not available — no strip has been fetched for this video, or
 * the request is still in flight — and the seek bar simply shows no preview.
 */
data class PlayerSeekPreview(val frameUrls: List<String>)

data class PlayerMetadata(
  val description: String,
  val longDescription: String,
  val collectionTitle: String,
  val seasonTitle: String,
  val releaseYear: String,
  val genres: String,
  val directors: String,
  val producers: String,
  val writers: String,
  val cast: String,
  val ageRestriction: String,
)

data class PlayerComment(
  val id: Long,
  val parentId: Long?,
  val authorName: String,
  val authorAvatarUrl: String?,
  val isAdmin: Boolean,
  val isPinned: Boolean,
  val postedAtLabel: String,
  val content: String,
  val replyCount: Long,
  val likeCount: Long,
  val isLiked: Boolean,
)
