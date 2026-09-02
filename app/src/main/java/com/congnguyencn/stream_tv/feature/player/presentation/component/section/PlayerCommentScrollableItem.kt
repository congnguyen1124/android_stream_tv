package com.congnguyencn.stream_tv.feature.player.presentation.component.section

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerCommentUiItem
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val VisibleCommentLineCount = 4
private const val CommentScrollLineStep = 2
private val CommentItemPadding = 12.dp
private val CommentAvatarSize = 32.dp
private val CommentContentSpacing = 12.dp
private val CommentContentLineHeight = 22.sp
private val CommentScrollbarSpacing = 12.dp
private val CommentScrollbarContainerWidth = 20.dp
private val CommentScrollbarTrackWidth = 4.dp
private val CommentScrollbarThumbUnfocusedWidth = 4.dp
private val CommentScrollbarThumbFocusedWidth = 10.dp

/** A four-line comment viewport with D-pad line scrolling and a focus-aware scrollbar. */
@Composable
@Suppress("CognitiveComplexMethod", "LongMethod")
internal fun PlayerCommentScrollableItem(
  comment: PlayerCommentUiItem,
  isFocusEnabled: Boolean,
  modifier: Modifier = Modifier,
) {
  val textStyle = commentContentTextStyle()
  val textMeasurer = rememberTextMeasurer()
  val density = LocalDensity.current
  var hasFocus by remember { mutableStateOf(false) }
  var scrollLineIndex by remember(comment.content) { mutableIntStateOf(0) }

  BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
    val fullTextWidthPx = with(density) {
      (maxWidth - CommentItemPadding * 2 - CommentAvatarSize - CommentContentSpacing)
        .roundToPx()
        .coerceAtLeast(0)
    }
    val fullTextLayout = remember(comment.content, textStyle, fullTextWidthPx, textMeasurer) {
      textMeasurer.measure(
        text = AnnotatedString(comment.content),
        style = textStyle,
        constraints = Constraints(maxWidth = fullTextWidthPx),
      )
    }
    val needsScrollbar = fullTextLayout.lineCount > VisibleCommentLineCount
    val scrollableTextWidthPx = with(density) {
      (
        maxWidth - CommentItemPadding * 2 - CommentAvatarSize - CommentContentSpacing -
          CommentScrollbarSpacing - CommentScrollbarContainerWidth
        ).roundToPx().coerceAtLeast(0)
    }
    val textLayout = remember(
      comment.content,
      textStyle,
      scrollableTextWidthPx,
      needsScrollbar,
      textMeasurer,
      fullTextLayout,
    ) {
      if (needsScrollbar) {
        textMeasurer.measure(
          text = AnnotatedString(comment.content),
          style = textStyle,
          constraints = Constraints(maxWidth = scrollableTextWidthPx),
        )
      } else {
        fullTextLayout
      }
    }
    val isScrollable = textLayout.lineCount > VisibleCommentLineCount
    val maxScrollLineIndex = if (isScrollable) textLayout.maxScrollLineIndex() else 0
    val currentScrollLineIndex = scrollLineIndex.coerceIn(0, maxScrollLineIndex)
    val visibleTextHeightPx = textLayout.visibleWindowHeightPx()
    val scrollOffsetPx = textLayout.lineTopPx(currentScrollLineIndex)
    val maxScrollOffsetPx = textLayout.lineTopPx(maxScrollLineIndex)

    LaunchedEffect(maxScrollLineIndex, isScrollable) {
      if (!isScrollable || scrollLineIndex > maxScrollLineIndex) {
        scrollLineIndex = maxScrollLineIndex
      }
    }

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .onFocusChanged { hasFocus = it.hasFocus }
        .onPreviewKeyEvent { event ->
          event.handleCommentScrollKeyEvent(
            scrollLineIndex = currentScrollLineIndex,
            maxScrollLineIndex = maxScrollLineIndex,
            onScrollLineIndexChange = { scrollLineIndex = it },
          )
        }
        .focusable(enabled = isScrollable && isFocusEnabled),
    ) {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        colors = SurfaceDefaults.colors(
          containerColor = StreamTvColors.Transparent,
          contentColor = StreamTvColors.NeutralWhite,
        ),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .padding(CommentItemPadding),
          horizontalArrangement = Arrangement.spacedBy(CommentContentSpacing),
          verticalAlignment = Alignment.Top,
        ) {
          PlayerCommentAvatar(comment = comment)
          Column(modifier = Modifier.weight(1f)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = comment.authorName,
                modifier = if (comment.isAdmin) {
                  Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(StreamTvColors.NeutralWhite)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                } else {
                  Modifier
                },
                color = if (comment.isAdmin) StreamTvColors.NeutralBlack else Color.Unspecified,
                style = StreamTvTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "•",
                color = StreamTvColors.Neutral20,
                style = StreamTvTheme.typography.bodyLarge,
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = comment.postedAtLabel,
                color = StreamTvColors.Neutral20,
                style = StreamTvTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            PlayerScrollableCommentContent(
              content = comment.content,
              isScrollable = isScrollable,
              hasFocus = hasFocus,
              textLayout = textLayout,
              scrollOffsetPx = scrollOffsetPx,
              maxScrollOffsetPx = maxScrollOffsetPx,
              visibleTextHeightPx = visibleTextHeightPx,
              modifier = Modifier.fillMaxWidth(),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun PlayerScrollableCommentContent(
  content: String,
  isScrollable: Boolean,
  hasFocus: Boolean,
  textLayout: TextLayoutResult,
  scrollOffsetPx: Int,
  maxScrollOffsetPx: Int,
  visibleTextHeightPx: Int,
  modifier: Modifier = Modifier,
) {
  val density = LocalDensity.current
  val contentTextStyle = commentContentTextStyle().copy(color = StreamTvColors.NeutralWhite)
  val scrollState = rememberScrollState()

  if (!isScrollable) {
    BasicText(
      text = content,
      modifier = modifier,
      style = contentTextStyle,
      maxLines = VisibleCommentLineCount,
      overflow = TextOverflow.Ellipsis,
    )
    return
  }

  val visibleTextHeight = with(density) { visibleTextHeightPx.toDp() }
  val targetScrollOffsetPx = scrollOffsetPx.coerceIn(0, maxScrollOffsetPx)
  val actualScrollOffsetPx = min(targetScrollOffsetPx, scrollState.maxValue)

  LaunchedEffect(targetScrollOffsetPx, scrollState.maxValue) {
    scrollState.scrollTo(actualScrollOffsetPx)
  }

  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(CommentScrollbarSpacing),
    verticalAlignment = Alignment.Top,
  ) {
    Box(
      modifier = Modifier
        .weight(1f)
        .height(visibleTextHeight)
        .clipToBounds()
        .verticalScroll(scrollState, enabled = false),
    ) {
      BasicText(
        text = content,
        modifier = Modifier.fillMaxWidth(),
        style = contentTextStyle,
        overflow = TextOverflow.Clip,
      )
    }
    PlayerCommentScrollbar(
      hasFocus = hasFocus,
      lineCount = textLayout.lineCount,
      scrollOffsetPx = actualScrollOffsetPx,
      maxScrollPx = min(maxScrollOffsetPx, scrollState.maxValue),
      modifier = Modifier
        .width(CommentScrollbarContainerWidth)
        .height(visibleTextHeight),
    )
  }
}

@Composable
private fun commentContentTextStyle() = StreamTvTheme.typography.bodyLarge.copy(
  lineHeight = CommentContentLineHeight,
  lineHeightStyle = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Top,
    trim = LineHeightStyle.Trim.None,
  ),
  platformStyle = PlatformTextStyle(includeFontPadding = false),
)

@Composable
private fun PlayerCommentScrollbar(
  hasFocus: Boolean,
  lineCount: Int,
  scrollOffsetPx: Int,
  maxScrollPx: Int,
  modifier: Modifier = Modifier,
) {
  val density = LocalDensity.current
  val thumbWidth = if (hasFocus) {
    CommentScrollbarThumbFocusedWidth
  } else {
    CommentScrollbarThumbUnfocusedWidth
  }

  BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.TopCenter) {
    val trackHeightPx = with(density) { maxHeight.roundToPx() }
    val thumbWidthPx = with(density) { thumbWidth.roundToPx() }
    val thumbHeightPx = max(
      thumbWidthPx,
      (trackHeightPx * VisibleCommentLineCount.toFloat() / lineCount).roundToInt(),
    ).coerceAtMost(trackHeightPx)
    val thumbOffsetPx = if (maxScrollPx > 0) {
      ((trackHeightPx - thumbHeightPx) * scrollOffsetPx.toFloat() / maxScrollPx).roundToInt()
    } else {
      0
    }

    Box(
      modifier = Modifier
        .width(CommentScrollbarTrackWidth)
        .height(maxHeight)
        .clip(RoundedCornerShape(100.dp))
        .background(StreamTvColors.TransparentWhite20),
    )
    Box(
      modifier = Modifier
        .offset { IntOffset(x = 0, y = thumbOffsetPx) }
        .width(thumbWidth)
        .height(with(density) { thumbHeightPx.toDp() })
        .clip(RoundedCornerShape(100.dp))
        .background(if (hasFocus) StreamTvColors.NeutralWhite else StreamTvColors.Neutral20),
    )
  }
}

private fun KeyEvent.handleCommentScrollKeyEvent(
  scrollLineIndex: Int,
  maxScrollLineIndex: Int,
  onScrollLineIndexChange: (Int) -> Unit,
): Boolean {
  if (type != KeyEventType.KeyDown) return false
  return when (key) {
    Key.DirectionLeft,
    Key.DirectionRight,
    -> true

    Key.DirectionDown -> if (scrollLineIndex >= maxScrollLineIndex) {
      false
    } else {
      onScrollLineIndexChange(
        scrollLineIndex + min(CommentScrollLineStep, maxScrollLineIndex - scrollLineIndex),
      )
      true
    }

    Key.DirectionUp -> if (scrollLineIndex <= 0) {
      false
    } else {
      onScrollLineIndexChange(max(scrollLineIndex - CommentScrollLineStep, 0))
      true
    }

    else -> false
  }
}

private fun TextLayoutResult.maxScrollLineIndex(): Int = (lineCount - VisibleCommentLineCount).coerceAtLeast(0)

private fun TextLayoutResult.lineTopPx(lineIndex: Int): Int =
  getLineTop(lineIndex.coerceIn(0, lineCount - 1)).roundToInt()

private fun TextLayoutResult.visibleWindowHeightPx(): Int {
  val lastVisibleLineIndex = (VisibleCommentLineCount - 1).coerceAtMost(lineCount - 1)
  return getLineBottom(lastVisibleLineIndex).roundToInt()
}

@Preview(device = Devices.TV_720p, showBackground = true, backgroundColor = 0xFF171717)
@Composable
private fun PlayerCommentScrollableItemPreview() {
  StreamTvTheme {
    PlayerCommentScrollableItem(
      comment = previewScrollableComment(),
      isFocusEnabled = true,
      modifier = Modifier.width(420.dp),
    )
  }
}

private fun previewScrollableComment() = PlayerCommentUiItem(
  id = 11L,
  parentId = 1L,
  authorName = "StreamTV",
  authorAvatarUrl = null,
  isAdmin = true,
  isPinned = false,
  postedAtLabel = "2 hours ago",
  content = "The final scene rewards a patient viewer with a graceful shift in perspective. " +
    "The camera stays close to the wildlife before opening onto the entire valley, revealing " +
    "how every small movement belongs to a much larger story. This longer preview also makes " +
    "the four-line viewport and its focus-aware scrollbar visible.",
  replyCount = 0,
  likeCount = 42,
  isLiked = false,
)
