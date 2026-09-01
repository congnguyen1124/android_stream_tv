package com.congnguyencn.stream_tv.feature.player.data.model

internal data class PlayerDetailsData(
  val metadata: PlayerMetadataData,
  val comments: List<PlayerCommentData>,
  val repliesByCommentId: Map<Long, List<PlayerCommentData>>,
)

internal data class PlayerMetadataData(
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

internal data class PlayerCommentData(
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
  val isLiked: Boolean = false,
)
