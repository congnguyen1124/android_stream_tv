package com.congnguyencn.stream_tv.feature.player.presentation.component.section

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerCommentUiItem

@Composable
internal fun PlayerCommentsSection(
  comments: List<PlayerCommentUiItem>,
  totalCommentCount: Long,
  selectedCommentId: Long?,
  isFocusEnabled: Boolean,
  focusRequester: FocusRequester,
  onCommentSelected: (Long) -> Unit,
  onBack: () -> Unit,
  dismissOnLeft: Boolean,
  modifier: Modifier = Modifier,
) {
  val focusCommentId = comments.find { comment -> comment.id == selectedCommentId }?.id
    ?: comments.firstOrNull()?.id

  LaunchedEffect(isFocusEnabled, focusCommentId, comments) {
    if (isFocusEnabled && focusCommentId != null) {
      awaitPlayerSectionFrame()
      focusRequester.requestFocus()
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .handlePlayerSectionExit(onBack = onBack, dismissOnLeft = dismissOnLeft),
  ) {
    PlayerCountHeader(
      title = stringResource(R.string.player_comments),
      count = totalCommentCount,
    )
    Spacer(modifier = Modifier.height(16.dp))
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      items(
        items = comments,
        key = PlayerCommentUiItem::id,
      ) { comment ->
        PlayerCommentCard(
          comment = comment,
          isFocusEnabled = isFocusEnabled,
          onClick = { onCommentSelected(comment.id) },
          modifier = if (comment.id == focusCommentId) {
            Modifier.focusRequester(focusRequester)
          } else {
            Modifier
          },
        )
      }
    }
  }
}

@Composable
internal fun PlayerRepliesSection(
  parentComment: PlayerCommentUiItem?,
  replies: List<PlayerCommentUiItem>,
  selectedReplyId: Long?,
  isFocusEnabled: Boolean,
  focusRequester: FocusRequester,
  onReplySelected: (Long) -> Unit,
  onBack: () -> Unit,
  dismissOnLeft: Boolean,
  modifier: Modifier = Modifier,
) {
  val focusReplyId = replies.find { reply -> reply.id == selectedReplyId }?.id
    ?: replies.firstOrNull()?.id

  LaunchedEffect(isFocusEnabled, focusReplyId, replies) {
    if (isFocusEnabled && focusReplyId != null) {
      awaitPlayerSectionFrame()
      focusRequester.requestFocus()
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .handlePlayerSectionExit(onBack = onBack, dismissOnLeft = dismissOnLeft),
  ) {
    PlayerCountHeader(
      title = stringResource(R.string.player_replies),
      count = replies.size.toLong(),
    )
    Spacer(modifier = Modifier.height(16.dp))
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      parentComment?.let { comment ->
        item(key = "parent-${comment.id}", contentType = "ParentComment") {
          PlayerCommentCard(
            comment = comment,
            isFocusEnabled = false,
            showBackground = false,
            showReplyMetric = false,
            onClick = {},
          )
          Spacer(modifier = Modifier.height(4.dp))
        }
      }

      if (replies.isEmpty()) {
        item(contentType = "NoReplies") {
          Text(
            text = stringResource(R.string.player_no_replies),
            modifier = Modifier.padding(vertical = 32.dp),
            color = StreamTvColors.Neutral20,
            style = StreamTvTheme.typography.bodyLarge,
          )
        }
      } else {
        items(
          items = replies,
          key = PlayerCommentUiItem::id,
        ) { reply ->
          PlayerCommentCard(
            comment = reply,
            isFocusEnabled = isFocusEnabled,
            showReplyMetric = false,
            onClick = { onReplySelected(reply.id) },
            modifier = if (reply.id == focusReplyId) {
              Modifier.focusRequester(focusRequester)
            } else {
              Modifier
            },
          )
        }
      }
    }
  }
}

@Composable
internal fun PlayerReplyDetailSection(
  reply: PlayerCommentUiItem,
  isFocusEnabled: Boolean,
  focusRequester: FocusRequester,
  onLikeClick: (Long) -> Unit,
  onBack: () -> Unit,
  dismissOnLeft: Boolean,
  modifier: Modifier = Modifier,
) {
  LaunchedEffect(isFocusEnabled, reply.id, reply.isLiked) {
    if (isFocusEnabled) {
      awaitPlayerSectionFrame()
      focusRequester.requestFocus()
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .handlePlayerSectionExit(onBack = onBack, dismissOnLeft = dismissOnLeft),
  ) {
    PlayerCommentCard(
      comment = reply,
      isFocusEnabled = false,
      showBackground = false,
      showReplyMetric = false,
      onClick = {},
    )
    Spacer(modifier = Modifier.height(16.dp))
    PlayerMetricButton(
      iconResId = if (reply.isLiked) R.drawable.ic_player_like_filled else R.drawable.ic_player_like,
      label = reply.likeCount.toString(),
      onClick = { onLikeClick(reply.id) },
      modifier = Modifier
        .padding(start = 48.dp)
        .focusRequester(focusRequester)
        .focusProperties { canFocus = isFocusEnabled }
        .testTag("player-reply-like"),
    )
  }
}

@Composable
private fun PlayerCountHeader(title: String, count: Long) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text(
      text = title,
      style = StreamTvTheme.typography.titleLarge,
    )
    Text(
      text = count.toString(),
      modifier = Modifier
        .background(StreamTvColors.TransparentWhite10, RoundedCornerShape(100.dp))
        .padding(horizontal = 10.dp, vertical = 4.dp),
      color = StreamTvColors.Neutral20,
      style = StreamTvTheme.typography.labelMedium,
    )
  }
}

@Composable
@Suppress("CognitiveComplexMethod")
private fun PlayerCommentCard(
  comment: PlayerCommentUiItem,
  isFocusEnabled: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  showBackground: Boolean = true,
  showReplyMetric: Boolean = true,
) {
  Surface(
    onClick = onClick,
    modifier = modifier
      .fillMaxWidth()
      .focusProperties { canFocus = isFocusEnabled }
      .testTag("player-comment-${comment.id}"),
    shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(20.dp)),
    scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
    colors = ClickableSurfaceDefaults.colors(
      containerColor = if (showBackground) StreamTvColors.TransparentWhite5 else StreamTvColors.Transparent,
      contentColor = StreamTvColors.Neutral10,
      focusedContainerColor = StreamTvColors.NeutralWhite,
      focusedContentColor = StreamTvColors.NeutralBlack,
      pressedContainerColor = StreamTvColors.Primary60,
      pressedContentColor = StreamTvColors.NeutralWhite,
    ),
  ) {
    Row(
      modifier = Modifier.padding(20.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.Top,
    ) {
      PlayerCommentAvatar(comment = comment)
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = comment.authorName,
            modifier = if (comment.isAdmin) {
              Modifier
                .background(StreamTvColors.Neutral90, RoundedCornerShape(100.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            } else {
              Modifier
            },
            color = if (comment.isAdmin) StreamTvColors.NeutralWhite else LocalContentColor.current,
            style = StreamTvTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          if (comment.isPinned) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = ImageVector.vectorResource(R.drawable.ic_player_pin),
              contentDescription = null,
              modifier = Modifier.size(16.dp),
              tint = LocalContentColor.current,
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "• ${comment.postedAtLabel}",
            color = LocalContentColor.current.copy(alpha = 0.68f),
            style = StreamTvTheme.typography.labelMedium,
          )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = comment.content,
          style = StreamTvTheme.typography.bodyLarge,
          maxLines = 4,
          overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
          PlayerCommentMetric(
            iconResId = if (comment.isLiked) R.drawable.ic_player_like_filled else R.drawable.ic_player_like,
            value = comment.likeCount.toString(),
          )
          if (showReplyMetric) {
            PlayerCommentMetric(
              iconResId = R.drawable.ic_player_comment,
              value = comment.replyCount.toString(),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun PlayerCommentAvatar(comment: PlayerCommentUiItem) {
  Box(
    modifier = Modifier
      .size(32.dp)
      .background(
        color = if (comment.isAdmin) StreamTvColors.Primary60 else StreamTvColors.Neutral70,
        shape = CircleShape,
      ),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = comment.authorName.firstOrNull()?.uppercase().orEmpty(),
      color = StreamTvColors.NeutralWhite,
      style = StreamTvTheme.typography.labelMedium,
    )
  }
}

@Composable
private fun PlayerCommentMetric(iconResId: Int, value: String) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(7.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = ImageVector.vectorResource(iconResId),
      contentDescription = null,
      modifier = Modifier.size(16.dp),
      tint = LocalContentColor.current,
    )
    Text(
      text = value,
      style = StreamTvTheme.typography.labelMedium,
    )
  }
}

@Composable
private fun PlayerMetricButton(iconResId: Int, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
  Surface(
    onClick = onClick,
    modifier = modifier,
    shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(10.dp)),
    scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
    colors = ClickableSurfaceDefaults.colors(
      containerColor = StreamTvColors.TransparentWhite10,
      focusedContainerColor = StreamTvColors.NeutralWhite,
      focusedContentColor = StreamTvColors.NeutralBlack,
    ),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = ImageVector.vectorResource(iconResId),
        contentDescription = null,
        modifier = Modifier.size(18.dp),
      )
      Text(text = label, style = StreamTvTheme.typography.labelMedium)
    }
  }
}
