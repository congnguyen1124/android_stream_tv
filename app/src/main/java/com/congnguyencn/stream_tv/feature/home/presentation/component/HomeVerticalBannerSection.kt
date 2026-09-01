package com.congnguyencn.stream_tv.feature.home.presentation.component

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import androidx.core.graphics.scale
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.palette.graphics.Palette
import androidx.tv.material3.MaterialTheme
import coil3.BitmapImage
import coil3.Image
import coil3.toBitmap
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvNetworkImage
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.tokens.StreamTvDimensions
import com.congnguyencn.stream_tv.feature.home.presentation.model.ShortUiItem
import kotlin.math.absoluteValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private object HomeVerticalBannerDefaults {
    const val VisibleItemCount = 5
    const val ItemRatio = 2f / 3f
    const val FocusedItemScale = 1.1f
    const val UnfocusedItemScale = 0.94f
    const val AutoScrollDurationMillis = 5_000L
    const val BackgroundOverlayAlpha = 0.72f
    const val PaletteBitmapWidth = 64
    const val PaletteBitmapHeight = 96
    const val PaletteMaxColorCount = 12

    val TopContentPadding = StreamTvDimensions.TopBarHeight
    val PagerHeight = 272.dp
    val BannerHeight = TopContentPadding + PagerHeight + 20.dp
    val ItemWidth = 164.dp
    val ItemSpacing = 22.dp
    val ItemShape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    val FocusedBorderWidth = 3.dp
    val UnfocusedBorderWidth = 1.dp
}

@Suppress("LongMethod", "CognitiveComplexMethod")
@Composable
internal fun HomeVerticalBannerSection(
    items: List<ShortUiItem>,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true,
    autoScrollDurationMillis: Long = HomeVerticalBannerDefaults.AutoScrollDurationMillis,
    onItemClick: (ShortUiItem) -> Unit = {},
) {
    if (items.isEmpty()) return

    val isLoopingEnabled = items.size >= HomeVerticalBannerDefaults.VisibleItemCount
    val pagerItems = remember(items, isLoopingEnabled) {
        if (isLoopingEnabled) items.toLoopingVerticalBannerItems() else items
    }
    val initialRealIndex = remember(items) { items.size / 2 }
    val initialPage = remember(isLoopingEnabled, initialRealIndex) {
        if (isLoopingEnabled) {
            initialRealIndex + HomeVerticalBannerDefaults.VisibleItemCount
        } else {
            initialRealIndex
        }
    }
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = pagerItems::size,
    )
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val itemBackgroundColors = remember(items) { mutableStateMapOf<String, Color>() }
    var isAutoPlay by remember { mutableStateOf(autoPlay && isLoopingEnabled) }

    DisposableEffect(isFocused, autoPlay, isLoopingEnabled) {
        isAutoPlay = autoPlay && isLoopingEnabled && !isFocused
        onDispose { }
    }

    if (isLoopingEnabled) {
        VerticalBannerLoopEdgeEffect(
            pagerState = pagerState,
            realItemCount = items.size,
        )
    }
    if (isAutoPlay) {
        VerticalBannerAutoScrollEffect(
            pagerState = pagerState,
            autoScrollDurationMillis = autoScrollDurationMillis,
        )
    }

    val activeIndex = pagerState.currentPage.toVerticalBannerRealIndex(
        realItemCount = items.size,
        isLoopingEnabled = isLoopingEnabled,
    )
    val activeItem = items[activeIndex]
    val activeBackgroundColor = itemBackgroundColors[activeItem.id] ?: StreamTvColors.TransparentBlack60

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(HomeVerticalBannerDefaults.BannerHeight),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(1.dp)
                .onPreviewKeyEvent { event ->
                    when (event.key) {
                        Key.DirectionLeft -> {
                            if (
                                event.type == KeyEventType.KeyDown &&
                                (isLoopingEnabled || pagerState.currentPage > 0)
                            ) {
                                scope.launch {
                                    scrollVerticalBannerPrevious(
                                        pagerState = pagerState,
                                        isLoopingEnabled = isLoopingEnabled,
                                        realItemCount = items.size,
                                    )
                                }
                            }
                            true
                        }

                        Key.DirectionRight -> {
                            if (
                                event.type == KeyEventType.KeyDown &&
                                (isLoopingEnabled || pagerState.currentPage < pagerItems.lastIndex)
                            ) {
                                scope.launch {
                                    scrollVerticalBannerNext(
                                        pagerState = pagerState,
                                        isLoopingEnabled = isLoopingEnabled,
                                    )
                                }
                            }
                            true
                        }

                        Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
                            if (event.type == KeyEventType.KeyDown) onItemClick(activeItem)
                            true
                        }

                        else -> false
                    }
                }
                .focusable(interactionSource = interactionSource)
                .testTag("home-vertical-banner-carousel"),
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .align(Alignment.TopCenter)
                .zIndex(-1f),
        ) {
            val verticalBannerOffset = (maxHeight - HomeVerticalBannerDefaults.BannerHeight) / 2
            val verticalGradientWidth = maxWidth / 3
            val horizontalGradientHeight = maxHeight / 1.5f

            VerticalBannerBackground(
                backgroundColor = activeBackgroundColor,
                verticalGradientWidth = verticalGradientWidth,
                horizontalGradientHeight = horizontalGradientHeight,
                modifier = Modifier
                    .fillMaxSize()
                    .absoluteOffset(y = verticalBannerOffset),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = HomeVerticalBannerDefaults.TopContentPadding),
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HomeVerticalBannerDefaults.PagerHeight),
            ) {
                val itemSpacing = HomeVerticalBannerDefaults.ItemSpacing
                val maxVisibleItemWidth = (
                    (maxWidth - itemSpacing * (HomeVerticalBannerDefaults.VisibleItemCount - 1)) /
                        HomeVerticalBannerDefaults.VisibleItemCount
                    ).coerceAtLeast(1.dp)
                val itemWidth = HomeVerticalBannerDefaults.ItemWidth.coerceAtMost(maxVisibleItemWidth)
                val horizontalPadding = ((maxWidth - itemWidth) / 2).coerceAtLeast(0.dp)

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = horizontalPadding),
                    pageSpacing = itemSpacing,
                    pageSize = PageSize.Fixed(itemWidth),
                    flingBehavior = PagerDefaults.flingBehavior(pagerState),
                    userScrollEnabled = true,
                    key = { page ->
                        val realIndex = page.toVerticalBannerRealIndex(
                            realItemCount = items.size,
                            isLoopingEnabled = isLoopingEnabled,
                        )
                        "${pagerItems[page].id}_${page}_$realIndex"
                    },
                ) { page ->
                    VerticalBannerItem(
                        item = pagerItems[page],
                        itemWidth = itemWidth,
                        pagerState = pagerState,
                        page = page,
                        isFocused = isFocused && page == pagerState.currentPage,
                        onBackgroundColorExtract = { itemId, backgroundColor ->
                            if (itemBackgroundColors[itemId] != backgroundColor) {
                                itemBackgroundColors[itemId] = backgroundColor
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(if (page == pagerState.currentPage) 1f else 0f),
                    )
                }
            }
        }
    }
}

@Composable
private fun VerticalBannerItem(
    item: ShortUiItem,
    itemWidth: Dp,
    pagerState: PagerState,
    page: Int,
    isFocused: Boolean,
    onBackgroundColorExtract: (itemId: String, backgroundColor: Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    var loadedImage by remember(item.id) { mutableStateOf<Image?>(null) }
    val pageOffset = (pagerState.currentPage - page + pagerState.currentPageOffsetFraction).absoluteValue
    val scaleFraction = 1f - pageOffset.coerceIn(0f, 1f)
    val scale = lerp(
        start = HomeVerticalBannerDefaults.UnfocusedItemScale,
        stop = HomeVerticalBannerDefaults.FocusedItemScale,
        fraction = scaleFraction,
    )

    LaunchedEffect(item.id, loadedImage) {
        val image = loadedImage ?: return@LaunchedEffect
        val backgroundColor = withContext(Dispatchers.Default) {
            image.extractVerticalBannerBackgroundColor()
        }
        onBackgroundColorExtract(item.id, backgroundColor)
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .width(itemWidth)
                .aspectRatio(HomeVerticalBannerDefaults.ItemRatio)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .clip(HomeVerticalBannerDefaults.ItemShape)
                .background(Color.Black)
                .border(
                    border = BorderStroke(
                        width = if (isFocused) {
                            HomeVerticalBannerDefaults.FocusedBorderWidth
                        } else {
                            HomeVerticalBannerDefaults.UnfocusedBorderWidth
                        },
                        color = if (isFocused) {
                            StreamTvColors.NeutralWhite
                        } else {
                            StreamTvColors.TransparentWhite10
                        },
                    ),
                    shape = HomeVerticalBannerDefaults.ItemShape,
                ),
        ) {
            StreamTvNetworkImage(
                imageUrl = item.thumbnailUrl,
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onSuccess = { state -> loadedImage = state.result.image },
            )
        }
    }
}

@Composable
private fun VerticalBannerBackground(
    backgroundColor: Color,
    verticalGradientWidth: Dp,
    horizontalGradientHeight: Dp,
    modifier: Modifier = Modifier,
) {
    val animatedBackgroundColor by animateColorAsState(
        targetValue = backgroundColor,
        label = "VerticalBannerBackgroundColor",
    )

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(animatedBackgroundColor),
        )
        VerticalBannerVerticalGradient(
            verticalGradientWidth = verticalGradientWidth,
            modifier = Modifier.align(Alignment.CenterStart),
        )
        VerticalBannerHorizontalGradient(
            horizontalGradientHeight = horizontalGradientHeight,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(verticalGradientWidth)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(StreamTvColors.Transparent, MaterialTheme.colorScheme.surface),
                    ),
                ),
        )
    }
}

@Composable
private fun VerticalBannerHorizontalGradient(
    horizontalGradientHeight: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(horizontalGradientHeight)
            .background(
                Brush.verticalGradient(
                    colors = listOf(StreamTvColors.Transparent, MaterialTheme.colorScheme.surface),
                ),
            ),
    )
}

@Composable
private fun VerticalBannerVerticalGradient(
    verticalGradientWidth: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(verticalGradientWidth)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(MaterialTheme.colorScheme.surface, StreamTvColors.Transparent),
                ),
            ),
    )
}

@Composable
@OptIn(ExperimentalCoroutinesApi::class)
private fun VerticalBannerAutoScrollEffect(
    pagerState: PagerState,
    autoScrollDurationMillis: Long,
) {
    val scope = rememberCoroutineScope()
    LifecycleResumeEffect(pagerState, scope, autoScrollDurationMillis) {
        val autoSlideJob = scope.launch {
            snapshotFlow { pagerState.isScrollInProgress }
                .flatMapLatest { isScrollInProgress ->
                    if (isScrollInProgress) emptyFlow() else verticalBannerIntervalFlow(autoScrollDurationMillis)
                }
                .collectLatest {
                    if (!pagerState.isScrollInProgress && pagerState.currentPage < pagerState.pageCount - 1) {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
        }
        onPauseOrDispose { autoSlideJob.cancel() }
    }
}

@Composable
private fun VerticalBannerLoopEdgeEffect(pagerState: PagerState, realItemCount: Int) {
    val scope = rememberCoroutineScope()
    LifecycleResumeEffect(pagerState, scope, realItemCount) {
        val loopEdgeJob = scope.launch {
            snapshotFlow { pagerState.settledPage }
                .mapNotNull { page -> page.toLoopedVerticalBannerPageOrNull(realItemCount) }
                .collectLatest(pagerState::scrollToPage)
        }
        onPauseOrDispose { loopEdgeJob.cancel() }
    }
}

private suspend fun scrollVerticalBannerPrevious(
    pagerState: PagerState,
    isLoopingEnabled: Boolean,
    realItemCount: Int,
) {
    if (isLoopingEnabled && pagerState.currentPage <= 0) {
        pagerState.scrollToPage(HomeVerticalBannerDefaults.VisibleItemCount + realItemCount - 1)
    }
    pagerState.animateScrollToPage((pagerState.currentPage - 1).coerceAtLeast(0))
}

private suspend fun scrollVerticalBannerNext(pagerState: PagerState, isLoopingEnabled: Boolean) {
    val lastPage = pagerState.pageCount - 1
    if (isLoopingEnabled && pagerState.currentPage >= lastPage) {
        pagerState.scrollToPage(HomeVerticalBannerDefaults.VisibleItemCount)
    }
    pagerState.animateScrollToPage((pagerState.currentPage + 1).coerceAtMost(lastPage))
}

internal fun Int.toVerticalBannerRealIndex(realItemCount: Int, isLoopingEnabled: Boolean): Int {
    if (!isLoopingEnabled) return coerceIn(0, realItemCount - 1)
    val shiftedIndex = this - HomeVerticalBannerDefaults.VisibleItemCount
    return (shiftedIndex % realItemCount + realItemCount) % realItemCount
}

private fun Int.toLoopedVerticalBannerPageOrNull(realItemCount: Int): Int? {
    val firstRealPage = HomeVerticalBannerDefaults.VisibleItemCount
    val lastRealPage = firstRealPage + realItemCount - 1
    return when {
        this < firstRealPage -> this + realItemCount
        this > lastRealPage -> this - realItemCount
        else -> null
    }
}

internal fun <T> List<T>.toLoopingVerticalBannerItems(): List<T> = when {
    size < HomeVerticalBannerDefaults.VisibleItemCount -> this
    else -> buildList(size + HomeVerticalBannerDefaults.VisibleItemCount * 2) {
        addAll(this@toLoopingVerticalBannerItems.takeLast(HomeVerticalBannerDefaults.VisibleItemCount))
        addAll(this@toLoopingVerticalBannerItems)
        addAll(this@toLoopingVerticalBannerItems.take(HomeVerticalBannerDefaults.VisibleItemCount))
    }
}

private fun verticalBannerIntervalFlow(durationMillis: Long) = flow {
    while (true) {
        delay(durationMillis)
        emit(Unit)
    }
}

private fun Image.extractVerticalBannerBackgroundColor(): Color = runCatching {
    val sourceBitmap = when (this) {
        is BitmapImage -> bitmap
        else -> toBitmap(
            width = HomeVerticalBannerDefaults.PaletteBitmapWidth,
            height = HomeVerticalBannerDefaults.PaletteBitmapHeight,
        )
    }
    val softwareBitmap = if (sourceBitmap.config == Bitmap.Config.HARDWARE) {
        sourceBitmap.copy(Bitmap.Config.ARGB_8888, false)
    } else {
        sourceBitmap
    }
    val paletteBitmap = if (
        softwareBitmap.width == HomeVerticalBannerDefaults.PaletteBitmapWidth &&
        softwareBitmap.height == HomeVerticalBannerDefaults.PaletteBitmapHeight
    ) {
        softwareBitmap
    } else {
        softwareBitmap.scale(
            HomeVerticalBannerDefaults.PaletteBitmapWidth,
            HomeVerticalBannerDefaults.PaletteBitmapHeight,
            false,
        )
    }
    val palette = Palette.from(paletteBitmap)
        .maximumColorCount(HomeVerticalBannerDefaults.PaletteMaxColorCount)
        .generate()
    val swatchColor = palette.lightVibrantSwatch?.rgb
        ?: palette.vibrantSwatch?.rgb
        ?: palette.lightMutedSwatch?.rgb
        ?: palette.mutedSwatch?.rgb
        ?: palette.dominantSwatch?.rgb
        ?: palette.darkVibrantSwatch?.rgb
        ?: palette.darkMutedSwatch?.rgb

    swatchColor?.toVerticalBannerOverlayColor() ?: StreamTvColors.TransparentBlack60
}.getOrDefault(StreamTvColors.TransparentBlack60)

private fun Int.toVerticalBannerOverlayColor(): Color = Color(this)
    .copy(alpha = 0.4f)
    .compositeOver(Color.Black)
    .copy(alpha = HomeVerticalBannerDefaults.BackgroundOverlayAlpha)
