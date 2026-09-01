package com.congnguyencn.stream_tv.feature.player.domain.model

data class PlayerDetailsRequest(val title: String, val description: String, val ageRestriction: String?)

data class PlayerDetails(
  val metadata: PlayerMetadata,
  val comments: List<PlayerComment>,
  val repliesByCommentId: Map<Long, List<PlayerComment>>,
)

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
