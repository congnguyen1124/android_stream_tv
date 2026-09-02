package com.congnguyencn.stream_tv.feature.home.presentation.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvNetworkImage
import com.congnguyencn.stream_tv.core.designsystem.component.contentrow.ContentRow
import com.congnguyencn.stream_tv.core.designsystem.component.contentrow.itemsIndexed
import com.congnguyencn.stream_tv.core.designsystem.component.contentrow.rememberContentRowState
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.home.presentation.model.ChannelUiItem
import com.congnguyencn.stream_tv.feature.home.presentation.model.HomeContentUiItem
import com.congnguyencn.stream_tv.feature.home.presentation.model.SeriesUiItem
import com.congnguyencn.stream_tv.feature.home.presentation.model.ShortUiItem

private val ContentThumbnailShape = RoundedCornerShape(8.dp)
private val FocusFramePadding = 2.dp
private val RankedItemSpacing = 32.dp
private val RankArtworkHeight = 103.dp
private val RankArtworkHorizontalOffset = (-30).dp
private val RankArtworkVerticalOffset = (-16).dp

internal enum class HomeContentRowStyle(
  val cardWidth: Dp,
  val aspectRatio: Float,
  val detailHeight: Dp,
  val descriptionMaxLines: Int,
  val isRanked: Boolean = false,
) {
  Video(
    cardWidth = 272.dp,
    aspectRatio = 16f / 9f,
    detailHeight = 52.dp,
    descriptionMaxLines = 1,
  ),
  PopularVideo(
    cardWidth = 272.dp,
    aspectRatio = 16f / 9f,
    detailHeight = 52.dp,
    descriptionMaxLines = 1,
    isRanked = true,
  ),
  Series(
    cardWidth = 272.dp,
    aspectRatio = 16f / 9f,
    detailHeight = 52.dp,
    descriptionMaxLines = 1,
  ),
  Channel(
    cardWidth = 272.dp,
    aspectRatio = 16f / 9f,
    detailHeight = 52.dp,
    descriptionMaxLines = 1,
  ),
  Short(
    cardWidth = 152.dp,
    aspectRatio = 2f / 3f,
    detailHeight = 70.dp,
    descriptionMaxLines = 2,
  ),
  PopularShort(
    cardWidth = 152.dp,
    aspectRatio = 2f / 3f,
    detailHeight = 70.dp,
    descriptionMaxLines = 2,
    isRanked = true,
  ),
  ;

  val thumbnailHeight: Dp
    get() = cardWidth / aspectRatio
}

/**
 * One titled row of Home content.
 *
 * @param sectionFocusRequester Attached to the row's single focus target — the fixed selection
 *   overlay — so Home can hand focus straight back to this row after a return from playback.
 */
@Suppress("LongParameterList")
@Composable
internal fun HomeContentRowSection(
  sectionId: String,
  title: String,
  items: List<HomeContentUiItem>,
  style: HomeContentRowStyle,
  sectionFocusRequester: FocusRequester,
  modifier: Modifier = Modifier,
  onItemClick: (HomeContentUiItem) -> Unit = {},
) {
  if (items.isEmpty()) return

  val state = rememberContentRowState()

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    HomeSectionHeader(title = title)

    ContentRow(
      state = state,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("home-content-row-$sectionId"),
      itemSpacing = if (style.isRanked) RankedItemSpacing else 18.dp,
      loopingEnabled = !style.isRanked,
      selectedItemModifier = Modifier
        .focusRequester(sectionFocusRequester)
        .testTag("home-content-row-$sectionId-selected-item"),
      selectedItem = { isFocused ->
        HomeContentFocusFrame(
          style = style,
          isFocused = isFocused,
        )
      },
      onSelectedItemClick = { selectedIndex ->
        items.getOrNull(selectedIndex)?.let(onItemClick)
      },
    ) {
      itemsIndexed(
        items = items,
        key = { _, item -> item.id },
        contentType = { _, item -> item::class.simpleName },
      ) { index, item ->
        HomeContentCard(
          item = item,
          style = style,
          isSelected = index == state.selectedIndex,
          rank = (index + 1).takeIf { style.isRanked },
          rankTestTag = "home-content-row-$sectionId-rank-${index + 1}",
          modifier = Modifier.testTag("home-content-${item.id}"),
        )
      }
    }
  }
}

@Composable
private fun HomeContentFocusFrame(style: HomeContentRowStyle, isFocused: Boolean, modifier: Modifier = Modifier) {
  val borderColor by animateColorAsState(
    targetValue = if (isFocused) StreamTvColors.NeutralWhite else Color.Transparent,
    label = "HomeContentFocusFrameColor",
  )

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(style.thumbnailHeight + FocusFramePadding * 2)
      .border(
        width = 3.dp,
        color = borderColor,
        shape = RoundedCornerShape(10.dp),
      ),
  )
}

@Composable
private fun HomeContentCard(
  item: HomeContentUiItem,
  style: HomeContentRowStyle,
  isSelected: Boolean,
  rank: Int?,
  rankTestTag: String,
  modifier: Modifier = Modifier,
) {
  val titleColor by animateColorAsState(
    targetValue = if (isSelected) StreamTvColors.NeutralWhite else StreamTvColors.Neutral20,
    label = "HomeContentCardTitleColor",
  )

  Column(
    modifier = modifier.width(style.cardWidth),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(style.aspectRatio),
    ) {
      Box(
        modifier = Modifier
          .matchParentSize()
          .clip(ContentThumbnailShape)
          .background(StreamTvColors.Neutral90),
      ) {
        StreamTvNetworkImage(
          imageUrl = item.thumbnailUrl,
          contentDescription = item.title,
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop,
        )

        ContentBadge(
          item = item,
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(8.dp),
        )

        item.ageRestriction?.takeIf(String::isNotBlank)?.let { ageRestriction ->
          Text(
            text = ageRestriction,
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(8.dp)
              .background(StreamTvColors.TransparentBlack60, RoundedCornerShape(4.dp))
              .padding(horizontal = 6.dp, vertical = 3.dp),
            color = StreamTvColors.Neutral10,
            style = StreamTvTheme.typography.labelMedium,
          )
        }
      }

      rank?.let { position ->
        HomePopularRank(
          rank = position,
          modifier = Modifier
            .align(Alignment.TopStart)
            .offset(
              x = RankArtworkHorizontalOffset,
              y = RankArtworkVerticalOffset,
            )
            .testTag(rankTestTag),
        )
      }
    }

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .height(style.detailHeight)
        .padding(top = 8.dp),
      verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
      Text(
        text = item.title,
        color = titleColor,
        style = StreamTvTheme.typography.bodyLarge,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = item.description,
        color = if (isSelected) StreamTvColors.Neutral20 else StreamTvColors.Neutral50,
        style = StreamTvTheme.typography.labelMedium,
        maxLines = style.descriptionMaxLines,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun HomePopularRank(rank: Int, modifier: Modifier = Modifier) {
  val drawableResId = rankedDrawableRes(rank) ?: return

  Image(
    painter = painterResource(drawableResId),
    contentDescription = "Rank $rank",
    modifier = modifier.height(RankArtworkHeight),
    contentScale = ContentScale.FillHeight,
  )
}

private fun rankedDrawableRes(rank: Int): Int? = when (rank) {
  1 -> R.drawable.img_ranked_1
  2 -> R.drawable.img_ranked_2
  3 -> R.drawable.img_ranked_3
  4 -> R.drawable.img_ranked_4
  5 -> R.drawable.img_ranked_5
  6 -> R.drawable.img_ranked_6
  7 -> R.drawable.img_ranked_7
  8 -> R.drawable.img_ranked_8
  9 -> R.drawable.img_ranked_9
  else -> null
}

@Composable
private fun ContentBadge(item: HomeContentUiItem, modifier: Modifier = Modifier) {
  val label = when (item) {
    is ChannelUiItem -> "LIVE"
    is SeriesUiItem -> "${item.episodes.size} EPISODES"
    is ShortUiItem -> "SHORT"
    else -> "VIDEO"
  }
  val backgroundColor = if (item is ChannelUiItem) {
    StreamTvColors.LiveBadge
  } else {
    StreamTvColors.TransparentBlack80
  }

  Row(
    modifier = modifier
      .background(backgroundColor, RoundedCornerShape(4.dp))
      .padding(horizontal = 7.dp, vertical = 3.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label,
      color = StreamTvColors.NeutralWhite,
      style = StreamTvTheme.typography.labelMedium,
    )
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF0B0B0F)
@Composable
private fun HomeContentRowSectionPreview() {
  StreamTvTheme {
    Box(modifier = Modifier.background(StreamTvColors.NeutralBlack)) {
      HomeContentRowSection(
        sectionId = "trending",
        title = "Trending now",
        items = HomePreviewData.Videos,
        style = HomeContentRowStyle.Video,
        sectionFocusRequester = remember { FocusRequester() },
        modifier = Modifier.padding(vertical = 24.dp),
      )
    }
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF0B0B0F)
@Composable
private fun HomePopularVideoRowSectionPreview() {
  StreamTvTheme {
    Box(modifier = Modifier.background(StreamTvColors.NeutralBlack)) {
      HomeContentRowSection(
        sectionId = "popular-videos",
        title = "Popular videos",
        items = HomePreviewData.Videos,
        style = HomeContentRowStyle.PopularVideo,
        sectionFocusRequester = remember { FocusRequester() },
        modifier = Modifier.padding(vertical = 24.dp),
      )
    }
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF0B0B0F)
@Composable
private fun HomePopularShortRowSectionPreview() {
  StreamTvTheme {
    Box(modifier = Modifier.background(StreamTvColors.NeutralBlack)) {
      HomeContentRowSection(
        sectionId = "popular-shorts",
        title = "Popular shorts",
        items = HomePreviewData.Shorts,
        style = HomeContentRowStyle.PopularShort,
        sectionFocusRequester = remember { FocusRequester() },
        modifier = Modifier.padding(vertical = 24.dp),
      )
    }
  }
}
