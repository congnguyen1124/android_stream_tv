package com.congnguyencn.stream_tv.feature.player.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class PlayerDetailsUiState(
  val metadata: PlayerMetadataUiState,
  val comments: List<PlayerCommentUiItem>,
  val repliesByCommentId: Map<Long, List<PlayerCommentUiItem>>,
  val seekPreview: PlayerSeekPreviewUiState,
) {
  val totalCommentCount: Long
    get() = comments.size.toLong() + repliesByCommentId.values.sumOf(List<PlayerCommentUiItem>::size)

  fun replies(commentId: Long): List<PlayerCommentUiItem> = repliesByCommentId[commentId].orEmpty()

  fun findComment(commentId: Long): PlayerCommentUiItem? = comments.find { comment -> comment.id == commentId }

  fun findReply(commentId: Long, replyId: Long): PlayerCommentUiItem? =
    replies(commentId).find { reply -> reply.id == replyId }

  fun toggleLike(itemId: Long): PlayerDetailsUiState = copy(
    comments = comments.map { comment -> comment.toggleLikeWhen(itemId) },
    repliesByCommentId = repliesByCommentId.mapValues { (_, replies) ->
      replies.map { reply -> reply.toggleLikeWhen(itemId) }
    },
  )

  private fun PlayerCommentUiItem.toggleLikeWhen(itemId: Long): PlayerCommentUiItem = if (id == itemId) {
    copy(
      isLiked = !isLiked,
      likeCount = (likeCount + if (isLiked) -1 else 1).coerceAtLeast(0),
    )
  } else {
    this
  }

  companion object {
    val Empty = PlayerDetailsUiState(
      metadata = PlayerMetadataUiState.Empty,
      comments = emptyList(),
      repliesByCommentId = emptyMap(),
      seekPreview = PlayerSeekPreviewUiState.Empty,
    )
  }
}

@Immutable
internal data class PlayerMetadataUiState(
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
) {
  companion object {
    val Empty = PlayerMetadataUiState(
      description = "",
      longDescription = "",
      collectionTitle = "",
      seasonTitle = "",
      releaseYear = "",
      genres = "",
      directors = "",
      producers = "",
      writers = "",
      cast = "",
      ageRestriction = "",
    )
  }
}

@Immutable
internal data class PlayerCommentUiItem(
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
