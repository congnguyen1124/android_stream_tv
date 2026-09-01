package com.congnguyencn.stream_tv.feature.player.presentation.mapper

import com.congnguyencn.stream_tv.feature.player.domain.model.PlayerComment
import com.congnguyencn.stream_tv.feature.player.domain.model.PlayerDetails
import com.congnguyencn.stream_tv.feature.player.domain.model.PlayerMetadata
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerCommentUiItem
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerDetailsUiState
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerMetadataUiState

internal fun PlayerDetails.toUiState(): PlayerDetailsUiState = PlayerDetailsUiState(
  metadata = metadata.toUiState(),
  comments = comments.map(PlayerComment::toUiItem),
  repliesByCommentId = repliesByCommentId.mapValues { (_, replies) ->
    replies.map(PlayerComment::toUiItem)
  },
)

private fun PlayerMetadata.toUiState(): PlayerMetadataUiState = PlayerMetadataUiState(
  description = description,
  longDescription = longDescription,
  collectionTitle = collectionTitle,
  seasonTitle = seasonTitle,
  releaseYear = releaseYear,
  genres = genres,
  directors = directors,
  producers = producers,
  writers = writers,
  cast = cast,
  ageRestriction = ageRestriction,
)

private fun PlayerComment.toUiItem(): PlayerCommentUiItem = PlayerCommentUiItem(
  id = id,
  parentId = parentId,
  authorName = authorName,
  authorAvatarUrl = authorAvatarUrl,
  isAdmin = isAdmin,
  isPinned = isPinned,
  postedAtLabel = postedAtLabel,
  content = content,
  replyCount = replyCount,
  likeCount = likeCount,
  isLiked = isLiked,
)
