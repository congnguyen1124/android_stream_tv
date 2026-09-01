package com.congnguyencn.stream_tv.feature.home.presentation.component

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvNetworkImage
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.core.designsystem.tokens.StreamTvDimensions
import com.congnguyencn.stream_tv.feature.home.presentation.model.VideoUiItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

private object HomeBannerDefaults {
  const val EdgeItemCount = 2
  const val AutoScrollDurationMillis = 5_000L
  const val VerticalGradientWidthRatio = 0.68f
  const val HorizontalGradientHeightRatio = 0.58f
  val BannerHeight = 600.dp
}

/**
 * @param bannerTrailer The trailer layer drawn over the active item's thumbnail, supplied by the
 *   route. Empty by default, which is the whole banner minus playback — what previews and Compose
 *   tests want, since neither has a player.
 */
@Suppress("LongMethod")
@Composable
internal fun HomeBannerSection(
  items: List<VideoUiItem>,
  contentFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
  modifier: Modifier = Modifier,
  autoPlay: Boolean = true,
  autoScrollDurationMillis: Long = HomeBannerDefaults.AutoScrollDurationMillis,
  onItemClick: (VideoUiItem) -> Unit = {},
  bannerTrailer: @Composable (item: VideoUiItem, isBannerFocused: Boolean) -> Unit = { _, _ -> },
) {
  if (items.isEmpty()) return

  val hasLoopingEdges = items.size > HomeBannerDefaults.EdgeItemCount
  val pagerItems = remember(items, hasLoopingEdges) {
    if (hasLoopingEdges) items.toLoopingBannerItems() else items
  }
  val initialPage = if (hasLoopingEdges) HomeBannerDefaults.EdgeItemCount else 0
  val pagerState = rememberPagerState(
    initialPage = initialPage,
    pageCount = pagerItems::size,
  )
  val scope = rememberCoroutineScope()
  val interactionSource = remember { MutableInteractionSource() }
  val isFocused by interactionSource.collectIsFocusedAsState()
  var isAutoPlay by remember { mutableStateOf(autoPlay) }

  DisposableEffect(isFocused, autoPlay) {
    isAutoPlay = autoPlay && !isFocused
    onDispose { }
  }

  if (isAutoPlay && pagerItems.size > 1) {
    BannerAutoScrollEffect(
      pagerState = pagerState,
      hasLoopingEdges = hasLoopingEdges,
      autoScrollDurationMillis = autoScrollDurationMillis,
    )
  }

  val currentPage = pagerState.currentPage
  val activeIndex = currentPage.toBannerRealIndex(
    realItemCount = items.size,
    hasLoopingEdges = hasLoopingEdges,
  )
  val activeItem = items[activeIndex]

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .height(HomeBannerDefaults.BannerHeight)
      .testTag("home-banner-container"),
  ) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
      val verticalGradientWidth = maxWidth * HomeBannerDefaults.VerticalGradientWidthRatio
      val horizontalGradientHeight = maxHeight * HomeBannerDefaults.HorizontalGradientHeightRatio

      Box(
        modifier = Modifier
          .align(Alignment.Center)
          .fillMaxWidth()
          .height(8.dp)
          .focusRequester(contentFocusRequester)
          .focusProperties { up = topBarFocusRequester }
          .onPreviewKeyEvent { event ->
            handleBannerKeyEvent(
              event = event,
              pagerState = pagerState,
              currentPage = currentPage,
              realItemCount = items.size,
              hasLoopingEdges = hasLoopingEdges,
              isFocused = isFocused,
              scope = scope,
              onSelect = { onItemClick(activeItem) },
            )
          }
          .focusable(interactionSource = interactionSource)
          .testTag("home-banner-carousel"),
      )

      Crossfade(
        targetState = activeItem,
        modifier = Modifier.fillMaxSize(),
        label = "HomeBannerImage",
      ) { item ->
        StreamTvNetworkImage(
          imageUrl = item.thumbnailUrl,
          contentDescription = item.title,
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop,
        )
      }

      // Over the thumbnail, under the gradients and the info block: a trailer is the hero image
      // moving, not a layer that gets to cover the title.
      bannerTrailer(activeItem, isFocused)

      HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        flingBehavior = PagerDefaults.flingBehavior(pagerState),
        userScrollEnabled = true,
        key = { page -> page },
      ) {
        Spacer(modifier = Modifier.fillMaxSize())
      }

      BannerVerticalGradient(
        verticalGradientWidth = verticalGradientWidth,
        modifier = Modifier.align(Alignment.CenterStart),
      )
      BannerHorizontalGradient(
        horizontalGradientHeight = horizontalGradientHeight,
        modifier = Modifier.align(Alignment.BottomCenter),
      )

      HomeBannerInfo(
        item = activeItem,
        isFocused = isFocused,
        itemCount = items.size,
        activeIndex = activeIndex,
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(
            start = StreamTvDimensions.ScreenHorizontalPadding,
            bottom = 30.dp,
          ),
      )
    }
  }
}

@Suppress("LongParameterList")
private fun handleBannerKeyEvent(
  event: KeyEvent,
  pagerState: PagerState,
  currentPage: Int,
  realItemCount: Int,
  hasLoopingEdges: Boolean,
  isFocused: Boolean,
  scope: CoroutineScope,
  onSelect: () -> Unit,
): Boolean {
  val isKeyDown = event.type == KeyEventType.KeyDown

  return when (event.key) {
    Key.DirectionLeft -> {
      if (isKeyDown && canScrollBannerLeft(currentPage, hasLoopingEdges)) {
        scope.launch { scrollBannerPrevious(pagerState, hasLoopingEdges) }
      }
      true
    }

    Key.DirectionRight -> {
      if (isKeyDown && isFocused) {
        scope.launch {
          scrollBannerNext(
            pagerState = pagerState,
            realItemCount = realItemCount,
            hasLoopingEdges = hasLoopingEdges,
          )
        }
      }
      true
    }

    Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
      if (isKeyDown) onSelect()
      true
    }

    else -> false
  }
}

@Composable
@OptIn(ExperimentalCoroutinesApi::class)
private fun BannerAutoScrollEffect(pagerState: PagerState, hasLoopingEdges: Boolean, autoScrollDurationMillis: Long) {
  val scope = rememberCoroutineScope()

  LifecycleResumeEffect(
    pagerState,
    scope,
    hasLoopingEdges,
    autoScrollDurationMillis,
  ) {
    val autoSlideJob = scope.launch {
      snapshotFlow { pagerState.isScrollInProgress }
        .flatMapLatest { isScrollInProgress ->
          if (isScrollInProgress) emptyFlow() else intervalFlow(autoScrollDurationMillis)
        }
        .collectLatest {
          val currentPage = pagerState.currentPage
          val canAutoScroll = canBannerAutoScroll(
            currentPage = currentPage,
            lastIndex = pagerState.pageCount - 1,
            hasLoopingEdges = hasLoopingEdges,
          )
          if (canAutoScroll && !pagerState.isScrollInProgress) {
            pagerState.animateScrollToPage(currentPage + 1)
          }
        }
    }

    val loopEdgeJob = scope.launch {
      snapshotFlow { pagerState.settledPage }
        .mapNotNull { settledPage ->
          bannerLoopTargetPage(
            settledPage = settledPage,
            lastIndex = pagerState.pageCount - 1,
            hasLoopingEdges = hasLoopingEdges,
          )
        }
        .collectLatest(pagerState::scrollToPage)
    }

    onPauseOrDispose {
      autoSlideJob.cancel()
      loopEdgeJob.cancel()
    }
  }
}

private fun canBannerAutoScroll(currentPage: Int, lastIndex: Int, hasLoopingEdges: Boolean): Boolean =
  if (hasLoopingEdges) {
    currentPage != 1 && currentPage != lastIndex - 1 && currentPage in 0..<lastIndex
  } else {
    currentPage < lastIndex
  }

/** Page to jump to when the pager settles on a duplicated edge item, or null while inside the real range. */
private fun bannerLoopTargetPage(settledPage: Int, lastIndex: Int, hasLoopingEdges: Boolean): Int? = when {
  !hasLoopingEdges -> null
  settledPage <= 1 -> lastIndex - HomeBannerDefaults.EdgeItemCount
  settledPage >= lastIndex - 1 -> HomeBannerDefaults.EdgeItemCount
  else -> null
}

private suspend fun scrollBannerNext(pagerState: PagerState, realItemCount: Int, hasLoopingEdges: Boolean) {
  val lastRealPage = if (hasLoopingEdges) {
    HomeBannerDefaults.EdgeItemCount + realItemCount - 1
  } else {
    pagerState.pageCount - 1
  }
  if (pagerState.currentPage >= lastRealPage) return
  pagerState.animateScrollToPage(pagerState.currentPage + 1)
}

private suspend fun scrollBannerPrevious(pagerState: PagerState, hasLoopingEdges: Boolean) {
  val firstRealPage = if (hasLoopingEdges) HomeBannerDefaults.EdgeItemCount else 0
  if (pagerState.currentPage <= firstRealPage) return
  pagerState.animateScrollToPage(pagerState.currentPage - 1)
}

private fun canScrollBannerLeft(currentPage: Int, hasLoopingEdges: Boolean): Boolean =
  currentPage > if (hasLoopingEdges) HomeBannerDefaults.EdgeItemCount else 0

internal fun Int.toBannerRealIndex(realItemCount: Int, hasLoopingEdges: Boolean): Int {
  if (realItemCount <= 0) return 0
  if (!hasLoopingEdges) return coerceIn(0, realItemCount - 1)
  val shiftedIndex = this - HomeBannerDefaults.EdgeItemCount
  return (shiftedIndex % realItemCount + realItemCount) % realItemCount
}

internal fun <T> List<T>.toLoopingBannerItems(): List<T> = when {
  size <= HomeBannerDefaults.EdgeItemCount -> this

  else -> buildList(size + HomeBannerDefaults.EdgeItemCount * 2) {
    add(this@toLoopingBannerItems[this@toLoopingBannerItems.lastIndex - 1])
    add(this@toLoopingBannerItems.last())
    addAll(this@toLoopingBannerItems)
    add(this@toLoopingBannerItems.first())
    add(this@toLoopingBannerItems[1])
  }
}

private fun intervalFlow(durationMillis: Long) = flow {
  while (true) {
    delay(durationMillis)
    emit(Unit)
  }
}

@Composable
private fun HomeBannerInfo(
  item: VideoUiItem,
  isFocused: Boolean,
  itemCount: Int,
  activeIndex: Int,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.width(470.dp),
    horizontalAlignment = Alignment.Start,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = "STREAMTV FEATURED",
        modifier = Modifier
          .background(StreamTvColors.TransparentBlack60, RoundedCornerShape(4.dp))
          .padding(horizontal = 8.dp, vertical = 4.dp),
        color = StreamTvColors.Primary30,
        style = StreamTvTheme.typography.labelMedium,
      )
      item.ageRestriction?.takeIf(String::isNotBlank)?.let { ageRestriction ->
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = ageRestriction,
          modifier = Modifier
            .border(1.dp, StreamTvColors.TransparentWhite40, RoundedCornerShape(4.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
          color = StreamTvColors.Neutral10,
          style = StreamTvTheme.typography.labelMedium,
        )
      }
    }
    Spacer(modifier = Modifier.height(12.dp))
    Text(
      text = item.title,
      color = StreamTvColors.NeutralWhite,
      style = StreamTvTheme.typography.headlineLarge,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
    )
    item.description.takeIf(String::isNotBlank)?.let { description ->
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = description,
        modifier = Modifier.width(440.dp),
        color = StreamTvColors.Neutral10,
        style = StreamTvTheme.typography.bodyLarge,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
    }
    Spacer(modifier = Modifier.height(18.dp))
    BannerPlayButton(isFocused = isFocused)
    Spacer(modifier = Modifier.height(18.dp))
    BannerDotsIndicator(
      count = itemCount,
      activeIndex = activeIndex,
    )
  }
}

@Composable
private fun BannerPlayButton(isFocused: Boolean, modifier: Modifier = Modifier) {
  val contentColor = if (isFocused) StreamTvColors.Neutral80 else StreamTvColors.Neutral10
  Row(
    modifier = modifier
      .background(
        color = if (isFocused) StreamTvColors.NeutralWhite else StreamTvColors.TransparentWhite10,
        shape = RoundedCornerShape(8.dp),
      )
      .padding(horizontal = 20.dp, vertical = 11.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = ImageVector.vectorResource(R.drawable.ic_play),
      contentDescription = null,
      modifier = Modifier.size(20.dp),
      tint = contentColor,
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = stringResource(R.string.play),
      color = contentColor,
      style = StreamTvTheme.typography.labelMedium,
    )
  }
}

@Composable
private fun BannerDotsIndicator(count: Int, activeIndex: Int, modifier: Modifier = Modifier) {
  Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
    repeat(count) { index ->
      val isActive = index == activeIndex
      Box(
        modifier = Modifier
          .padding(end = 6.dp)
          .width(if (isActive) 24.dp else 12.dp)
          .height(4.dp)
          .background(
            color = if (isActive) {
              StreamTvColors.NeutralWhite
            } else {
              StreamTvColors.TransparentWhite40
            },
            shape = RoundedCornerShape(2.dp),
          ),
      )
    }
  }
}

@Composable
private fun BannerHorizontalGradient(horizontalGradientHeight: Dp, modifier: Modifier = Modifier) {
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
private fun BannerVerticalGradient(verticalGradientWidth: Dp, modifier: Modifier = Modifier) {
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
