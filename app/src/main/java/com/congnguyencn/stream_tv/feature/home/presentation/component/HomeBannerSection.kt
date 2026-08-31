package com.congnguyencn.stream_tv.feature.home.presentation.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvNetworkImage
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.home.presentation.model.VideoUiItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val BannerAspectRatio = 16f / 9f
private const val AutoScrollDelayMillis = 6_000L
private val BannerShape = RoundedCornerShape(14.dp)

@Composable
internal fun HomeBannerSection(
    title: String,
    items: List<VideoUiItem>,
    contentFocusRequester: FocusRequester,
    topBarFocusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    onItemClick: (VideoUiItem) -> Unit = {},
) {
    if (items.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = items::size)
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(items.size, isFocused) {
        if (items.size < 2 || isFocused) return@LaunchedEffect
        while (true) {
            delay(AutoScrollDelayMillis)
            pagerState.animateScrollToPage((pagerState.currentPage + 1) % items.size)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        HomeSectionHeader(title = title)

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
        ) {
            val bannerWidth = maxWidth.coerceAtMost(1_000.dp)
            val focusBorderColor by animateColorAsState(
                targetValue = if (isFocused) StreamTvColors.NeutralWhite else StreamTvColors.TransparentWhite10,
                label = "HomeBannerBorderColor",
            )
            val focusScale by animateFloatAsState(
                targetValue = if (isFocused) 1.015f else 1f,
                label = "HomeBannerFocusScale",
            )

            Box(
                modifier = Modifier
                    .width(bannerWidth)
                    .aspectRatio(BannerAspectRatio)
                    .graphicsLayer {
                        scaleX = focusScale
                        scaleY = focusScale
                    }
                    .clip(BannerShape)
                    .border(
                        width = if (isFocused) 3.dp else 1.dp,
                        color = focusBorderColor,
                        shape = BannerShape,
                    )
                    .focusRequester(contentFocusRequester)
                    .focusProperties { up = topBarFocusRequester }
                    .onPreviewKeyEvent { event ->
                        if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                        when (event.key) {
                            Key.DirectionLeft -> {
                                scope.launch {
                                    pagerState.animateScrollToPage((pagerState.currentPage - 1).coerceAtLeast(0))
                                }
                                true
                            }
                            Key.DirectionRight -> {
                                scope.launch {
                                    pagerState.animateScrollToPage(
                                        (pagerState.currentPage + 1).coerceAtMost(items.lastIndex),
                                    )
                                }
                                true
                            }
                            Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
                                onItemClick(items[pagerState.currentPage])
                                true
                            }
                            else -> false
                        }
                    }
                    .focusable(interactionSource = interactionSource)
                    .testTag("home-banner-carousel"),
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = false,
                    key = { page -> items[page].id },
                ) { page ->
                    BannerPage(item = items[page])
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(22.dp)
                        .background(StreamTvColors.TransparentBlack60, CircleShape)
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    repeat(items.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index == pagerState.currentPage) 9.dp else 7.dp)
                                .background(
                                    color = if (index == pagerState.currentPage) {
                                        StreamTvColors.NeutralWhite
                                    } else {
                                        StreamTvColors.TransparentWhite40
                                    },
                                    shape = CircleShape,
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BannerPage(item: VideoUiItem) {
    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(
            targetState = item.thumbnailUrl,
            label = "HomeBannerImage",
        ) { imageUrl ->
            StreamTvNetworkImage(
                imageUrl = imageUrl,
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .fillMaxWidth(0.72f)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.90f),
                            Color.Black.copy(alpha = 0.55f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(170.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.78f)),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 34.dp, end = 160.dp, bottom = 30.dp),
        ) {
            item.ageRestriction?.let { ageRestriction ->
                Text(
                    text = ageRestriction,
                    modifier = Modifier
                        .border(1.dp, StreamTvColors.TransparentWhite40, RoundedCornerShape(5.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    color = StreamTvColors.Neutral10,
                    style = StreamTvTheme.typography.labelMedium,
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Text(
                text = item.title,
                color = StreamTvColors.NeutralWhite,
                style = StreamTvTheme.typography.headlineLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.description,
                modifier = Modifier.fillMaxWidth(0.7f),
                color = StreamTvColors.Neutral20,
                style = StreamTvTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
