package com.congnguyencn.stream_tv.feature.home.presentation.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
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

internal enum class HomeContentRowStyle(
    val cardWidth: Dp,
    val aspectRatio: Float,
    val detailHeight: Dp,
    val descriptionMaxLines: Int,
) {
    Video(
        cardWidth = 272.dp,
        aspectRatio = 16f / 9f,
        detailHeight = 52.dp,
        descriptionMaxLines = 1,
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
    ;

    val thumbnailHeight: Dp
        get() = cardWidth / aspectRatio
}

@Composable
internal fun HomeContentRowSection(
    sectionId: String,
    title: String,
    items: List<HomeContentUiItem>,
    style: HomeContentRowStyle,
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
            itemSpacing = 18.dp,
            selectedItemModifier = Modifier.testTag("home-content-row-$sectionId-selected-item"),
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
                    modifier = Modifier.testTag("home-content-${item.id}"),
                )
            }
        }
    }
}

@Composable
private fun HomeContentFocusFrame(
    style: HomeContentRowStyle,
    isFocused: Boolean,
    modifier: Modifier = Modifier,
) {
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
    modifier: Modifier = Modifier,
) {
    val itemAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.66f,
        label = "HomeContentCardAlpha",
    )

    Column(
        modifier = modifier
            .width(style.cardWidth)
            .graphicsLayer { alpha = itemAlpha },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(style.aspectRatio)
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(style.detailHeight)
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = item.title,
                color = if (isSelected) StreamTvColors.NeutralWhite else StreamTvColors.Neutral30,
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
private fun ContentBadge(
    item: HomeContentUiItem,
    modifier: Modifier = Modifier,
) {
    val label = when (item) {
        is ChannelUiItem -> "LIVE"
        is SeriesUiItem -> "${item.episodes.size} EPISODES"
        is ShortUiItem -> "SHORT"
        else -> "VIDEO"
    }
    val backgroundColor = if (item is ChannelUiItem) {
        Color(0xFFD71920)
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
