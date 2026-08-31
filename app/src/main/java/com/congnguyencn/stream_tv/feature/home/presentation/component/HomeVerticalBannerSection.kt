package com.congnguyencn.stream_tv.feature.home.presentation.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvNetworkImage
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.home.presentation.model.ShortUiItem
import kotlin.math.absoluteValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val ShortAspectRatio = 2f / 3f
private const val AutoScrollDelayMillis = 5_000L
private val ShortCardWidth = 184.dp
private val ShortCardShape = RoundedCornerShape(12.dp)

@Composable
internal fun HomeVerticalBannerSection(
    title: String,
    items: List<ShortUiItem>,
    modifier: Modifier = Modifier,
    onItemClick: (ShortUiItem) -> Unit = {},
) {
    if (items.isEmpty()) return

    val initialPage = remember(items.size) { items.size / 2 }
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = items::size,
    )
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scope = rememberCoroutineScope()
    val activeItem = items[pagerState.currentPage]

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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(390.dp)
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
                            onItemClick(activeItem)
                            true
                        }
                        else -> false
                    }
                }
                .focusable(interactionSource = interactionSource)
                .testTag("home-vertical-banner-carousel"),
        ) {
            Crossfade(
                targetState = activeItem,
                modifier = Modifier.fillMaxSize(),
                label = "VerticalBannerBackground",
            ) { item ->
                Box(modifier = Modifier.fillMaxSize()) {
                    StreamTvNetworkImage(
                        imageUrl = item.thumbnailUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.24f),
                        contentScale = ContentScale.Crop,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.9f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.9f),
                                    ),
                                ),
                            )
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.25f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.92f),
                                    ),
                                ),
                            ),
                    )
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .align(Alignment.TopCenter),
            ) {
                val horizontalPadding = ((maxWidth - ShortCardWidth) / 2).coerceAtLeast(0.dp)

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = horizontalPadding),
                    pageSize = PageSize.Fixed(ShortCardWidth),
                    pageSpacing = 24.dp,
                    userScrollEnabled = false,
                    key = { page -> items[page].id },
                ) { page ->
                    VerticalBannerCard(
                        item = items[page],
                        page = page,
                        pagerState = pagerState,
                        showFocusedBorder = isFocused && page == pagerState.currentPage,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 32.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = activeItem.title,
                    color = StreamTvColors.NeutralWhite,
                    style = StreamTvTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = activeItem.description,
                    modifier = Modifier.fillMaxWidth(0.55f),
                    color = StreamTvColors.Neutral20,
                    style = StreamTvTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun VerticalBannerCard(
    item: ShortUiItem,
    page: Int,
    pagerState: PagerState,
    showFocusedBorder: Boolean,
) {
    val borderColor by animateColorAsState(
        targetValue = if (showFocusedBorder) {
            StreamTvColors.NeutralWhite
        } else {
            StreamTvColors.TransparentWhite20
        },
        label = "VerticalBannerCardBorder",
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(ShortCardWidth)
                .aspectRatio(ShortAspectRatio)
                .graphicsLayer {
                    val pageOffset = (
                        pagerState.currentPage - page + pagerState.currentPageOffsetFraction
                    ).absoluteValue
                    val scaleFraction = 1f - pageOffset.coerceIn(0f, 1f)
                    val scale = lerp(start = 0.90f, stop = 1.08f, fraction = scaleFraction)
                    scaleX = scale
                    scaleY = scale
                }
                .clip(ShortCardShape)
                .background(StreamTvColors.NeutralBlack)
                .border(
                    width = if (showFocusedBorder) 3.dp else 1.dp,
                    color = borderColor,
                    shape = ShortCardShape,
                ),
        ) {
            StreamTvNetworkImage(
                imageUrl = item.thumbnailUrl,
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(92.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                        ),
                    ),
            )
            item.ageRestriction?.let { ageRestriction ->
                Text(
                    text = ageRestriction,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .background(StreamTvColors.TransparentBlack60, RoundedCornerShape(5.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                    color = StreamTvColors.Neutral10,
                    style = StreamTvTheme.typography.labelMedium,
                )
            }
        }
    }
}
