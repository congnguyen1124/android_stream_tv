package com.congnguyencn.stream_tv.feature.player.data.mapper

import com.congnguyencn.stream_tv.feature.player.data.model.PlayerCommentData
import com.congnguyencn.stream_tv.feature.player.data.model.PlayerDetailsData
import com.congnguyencn.stream_tv.feature.player.data.model.PlayerMetadataData
import com.congnguyencn.stream_tv.feature.player.domain.model.PlayerComment
import com.congnguyencn.stream_tv.feature.player.domain.model.PlayerDetails
import com.congnguyencn.stream_tv.feature.player.domain.model.PlayerMetadata
import com.congnguyencn.stream_tv.feature.player.domain.model.PlayerSeekPreview

internal fun PlayerDetailsData.toDomain(): PlayerDetails = PlayerDetails(
  metadata = metadata.toDomain(),
  comments = comments.map(PlayerCommentData::toDomain),
  repliesByCommentId = repliesByCommentId.mapValues { (_, replies) ->
    replies.map(PlayerCommentData::toDomain)
  },
  seekPreview = PlayerSeekPreview(frameUrls = seekPreviewFrameUrls),
)

private fun PlayerMetadataData.toDomain(): PlayerMetadata = PlayerMetadata(
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

private fun PlayerCommentData.toDomain(): PlayerComment = PlayerComment(
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
