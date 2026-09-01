package com.congnguyencn.stream_tv.feature.home.presentation.component

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Surface
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvNetworkImage
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.core.designsystem.tokens.StreamTvDimensions
import com.congnguyencn.stream_tv.feature.home.presentation.model.VideoUiItem

private object HomeBannerSectionDefaults {
  /** The invisible focus target laid over the artwork — the banner's single focusable node. */
  @Stable
  val FocusTargetHeight: Dp = 8.dp

  @Stable
  val InfoBottomPadding: Dp = 30.dp

  /** Viewport the previews frame the banner against — a 1080p TV reports 540.dp of height. */
  @Stable
  val PreviewViewportHeight: Dp = 540.dp
}

/**
 * Home's hero carousel: one focus target, artwork that cross-fades, and an info block over it.
 *
 * @param height Set by the caller from the viewport so a slice of the section below stays visible —
 *   see [homeBannerHeight]. The banner cannot work this out itself: inside a `LazyColumn` its own
 *   height constraint is unbounded.
 * @param sectionFocusRequester How Home hands focus back to this section after a return from
 *   playback. Distinct from [contentFocusRequester], which is where the top bar's Down key lands.
 * @param bannerTrailer The trailer layer drawn over the active item's thumbnail, supplied by the
 *   route. Empty by default, which is the whole banner minus playback — what previews and Compose
 *   tests want, since neither has a player.
 */
@Suppress("LongMethod", "LongParameterList")
@Composable
internal fun HomeBannerSection(
  items: List<VideoUiItem>,
  height: Dp,
  contentFocusRequester: FocusRequester,
  sectionFocusRequester: FocusRequester,
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
      .height(height)
      .testTag("home-banner-container"),
  ) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
      val verticalGradientWidth = maxWidth * HomeBannerDefaults.VerticalGradientWidthRatio
      val horizontalGradientHeight = maxHeight * HomeBannerDefaults.HorizontalGradientHeightRatio

      Box(
        modifier = Modifier
          .align(Alignment.Center)
          .fillMaxWidth()
          .height(HomeBannerSectionDefaults.FocusTargetHeight)
          .focusRequester(contentFocusRequester)
          .focusRequester(sectionFocusRequester)
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

      HomeBannerVerticalGradient(
        verticalGradientWidth = verticalGradientWidth,
        modifier = Modifier.align(Alignment.CenterStart),
      )
      HomeBannerHorizontalGradient(
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
            bottom = HomeBannerSectionDefaults.InfoBottomPadding,
          ),
      )
    }
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF0B0B0F)
@Composable
private fun HomeBannerSectionPreview() {
  StreamTvTheme {
    HomeBannerSection(
      items = HomePreviewData.Videos,
      height = homeBannerHeight(viewportHeight = HomeBannerSectionDefaults.PreviewViewportHeight),
      contentFocusRequester = remember { FocusRequester() },
      sectionFocusRequester = remember { FocusRequester() },
      topBarFocusRequester = remember { FocusRequester() },
      // A preview has no lifecycle owner driving the pager, so auto-scroll is off to keep the frame
      // identical on every render.
      autoPlay = false,
    )
  }
}
