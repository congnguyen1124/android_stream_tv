package com.congnguyencn.stream_tv.feature.home.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvSurface
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.core.designsystem.tokens.StreamTvDimensions
import com.congnguyencn.stream_tv.feature.home.presentation.HomeUiState
import com.congnguyencn.stream_tv.feature.home.presentation.model.ChannelUiItem
import com.congnguyencn.stream_tv.feature.home.presentation.model.HomeContentUiItem
import com.congnguyencn.stream_tv.feature.home.presentation.model.HomeSectionUiItem
import com.congnguyencn.stream_tv.feature.home.presentation.model.HomeSectionViewTypeUi
import com.congnguyencn.stream_tv.feature.home.presentation.model.SeriesUiItem
import com.congnguyencn.stream_tv.feature.home.presentation.model.ShortUiItem
import com.congnguyencn.stream_tv.feature.home.presentation.model.VideoUiItem

private object HomeContentDefaults {
  /** Full-bleed banners run behind the top bar; every other opening section starts below it. */
  const val FirstSectionIndex = 0

  @Stable
  val SectionSpacing: Dp = 34.dp

  @Stable
  val BottomPadding: Dp = 54.dp

  @Stable
  val MessagePadding: Dp = 32.dp
}

/**
 * Home's vertical list of sections.
 *
 * @param isTopBarFocused Whether the shell's top bar currently holds focus. It decides whether Home
 *   may claim focus at all: opening the app and returning from playback both leave focus nowhere, so
 *   the content has to take it, while picking a top bar destination leaves focus on the picked item —
 *   which must keep it rather than being yanked into the rows underneath.
 */
@Composable
internal fun HomeContent(
  uiState: HomeUiState,
  contentFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
  isTopBarFocused: Boolean,
  onItemClick: (HomeContentUiItem) -> Unit,
  onTopBarOverlayVisibilityChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  bannerTrailer: @Composable (item: VideoUiItem, isBannerFocused: Boolean) -> Unit = { _, _ -> },
) {
  val sections = uiState.sections
  val focusState = rememberHomeFocusState()
  val listState = rememberLazyListState()
  val sectionFocusRequesters = remember(sections.map(HomeSectionUiItem::id)) {
    List(sections.size) { FocusRequester() }
  }

  SideEffect {
    focusState.updateSectionCount(sections.size)
  }

  // The opening section fills the screen behind the top bar and paints its own gradients, so the bar
  // only needs a scrim of its own once focus has moved past it onto the rows underneath.
  val isTopBarOverlayVisible = focusState.focusedSectionIndex > HomeContentDefaults.FirstSectionIndex

  RestoreHomeSectionFocusEffect(
    sectionFocusRequesters = sectionFocusRequesters,
    focusState = focusState,
    listState = listState,
    isTopBarFocused = isTopBarFocused,
  )

  LaunchedEffect(isTopBarOverlayVisible) {
    onTopBarOverlayVisibilityChange(isTopBarOverlayVisible)
  }

  // The scrim belongs to the shell, which outlives this screen, so Home has to lower it on the way
  // out. The shell cannot do it off the current route instead: returning from a player composes
  // Home a frame before the route settles, and a route-keyed reset then lands after Home has
  // already reported the section it restored focus to — leaving the scrim down over scrolled rows.
  DisposableEffect(Unit) {
    onDispose { onTopBarOverlayVisibilityChange(false) }
  }

  when {
    uiState.isLoading -> HomeMessage(
      message = "Loading your StreamTV home...",
      modifier = modifier,
    )

    uiState.errorMessage != null -> HomeMessage(
      message = uiState.errorMessage,
      modifier = modifier,
    )

    else -> BoxWithConstraints(modifier = modifier.fillMaxSize()) {
      val bannerHeight = homeBannerHeight(viewportHeight = maxHeight)

      LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
          top = if (sections.firstOrNull()?.viewType.isBanner()) {
            0.dp
          } else {
            StreamTvDimensions.TopBarHeight
          },
          bottom = HomeContentDefaults.BottomPadding,
        ),
        verticalArrangement = Arrangement.spacedBy(HomeContentDefaults.SectionSpacing),
      ) {
        itemsIndexed(
          items = sections,
          key = { _, section -> section.id },
        ) { index, section ->
          HomeSection(
            section = section,
            bannerHeight = bannerHeight,
            contentFocusRequester = contentFocusRequester,
            sectionFocusRequester = sectionFocusRequesters[index],
            topBarFocusRequester = topBarFocusRequester,
            onItemClick = onItemClick,
            modifier = Modifier.onFocusChanged { sectionFocus ->
              if (sectionFocus.hasFocus) {
                focusState.focusSection(index)
              }
            },
            bannerTrailer = bannerTrailer,
          )
        }
      }
    }
  }
}

/**
 * Hands focus back to the section that had it, once the sections are on screen.
 *
 * Scrolls first: sections outside the viewport are never composed, and a `FocusRequester` whose node
 * has not been laid out cannot take focus. A deeper section is offset so its header clears the top
 * bar instead of arriving underneath it.
 */
@Composable
private fun RestoreHomeSectionFocusEffect(
  sectionFocusRequesters: List<FocusRequester>,
  focusState: HomeFocusState,
  listState: LazyListState,
  isTopBarFocused: Boolean,
) {
  val density = LocalDensity.current
  val headerClearance = remember(density) {
    with(density) { -StreamTvDimensions.TopBarHeight.roundToPx() }
  }

  LaunchedEffect(sectionFocusRequesters) {
    val targetIndex = focusState.focusedSectionIndex
    if (targetIndex !in sectionFocusRequesters.indices || isTopBarFocused) return@LaunchedEffect

    listState.scrollToItem(
      index = targetIndex,
      scrollOffset = if (targetIndex == HomeContentDefaults.FirstSectionIndex) 0 else headerClearance,
    )
    sectionFocusRequesters[targetIndex].requestFocus()
  }
}

@Suppress("LongParameterList")
@Composable
private fun HomeSection(
  section: HomeSectionUiItem,
  bannerHeight: Dp,
  contentFocusRequester: FocusRequester,
  sectionFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
  onItemClick: (HomeContentUiItem) -> Unit,
  modifier: Modifier = Modifier,
  bannerTrailer: @Composable (item: VideoUiItem, isBannerFocused: Boolean) -> Unit = { _, _ -> },
) {
  when (section.viewType) {
    HomeSectionViewTypeUi.Banner -> HomeBannerSection(
      items = section.items.requireItemsOfType<VideoUiItem>(),
      height = bannerHeight,
      contentFocusRequester = contentFocusRequester,
      sectionFocusRequester = sectionFocusRequester,
      topBarFocusRequester = topBarFocusRequester,
      modifier = modifier,
      onItemClick = onItemClick,
      bannerTrailer = bannerTrailer,
    )

    HomeSectionViewTypeUi.VerticalBanner -> HomeVerticalBannerSection(
      items = section.items.requireItemsOfType<ShortUiItem>(),
      sectionFocusRequester = sectionFocusRequester,
      modifier = modifier,
      onItemClick = onItemClick,
    )

    HomeSectionViewTypeUi.Videos -> HomeContentRowSection(
      sectionId = section.id,
      title = section.title,
      items = section.items.requireItemsOfType<VideoUiItem>(),
      style = HomeContentRowStyle.Video,
      sectionFocusRequester = sectionFocusRequester,
      modifier = modifier,
      onItemClick = onItemClick,
    )

    HomeSectionViewTypeUi.ListSeries -> HomeContentRowSection(
      sectionId = section.id,
      title = section.title,
      items = section.items.requireItemsOfType<SeriesUiItem>(),
      style = HomeContentRowStyle.Series,
      sectionFocusRequester = sectionFocusRequester,
      modifier = modifier,
      onItemClick = onItemClick,
    )

    HomeSectionViewTypeUi.Channels -> HomeContentRowSection(
      sectionId = section.id,
      title = section.title,
      items = section.items.requireItemsOfType<ChannelUiItem>(),
      style = HomeContentRowStyle.Channel,
      sectionFocusRequester = sectionFocusRequester,
      modifier = modifier,
      onItemClick = onItemClick,
    )

    HomeSectionViewTypeUi.Shorts -> HomeContentRowSection(
      sectionId = section.id,
      title = section.title,
      items = section.items.requireItemsOfType<ShortUiItem>(),
      style = HomeContentRowStyle.Short,
      sectionFocusRequester = sectionFocusRequester,
      modifier = modifier,
      onItemClick = onItemClick,
    )
  }
}

@Composable
private fun HomeMessage(message: String, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = message,
      modifier = Modifier.padding(HomeContentDefaults.MessagePadding),
      color = StreamTvColors.Neutral20,
      style = StreamTvTheme.typography.titleLarge,
    )
  }
}

private inline fun <reified T : HomeContentUiItem> List<HomeContentUiItem>.requireItemsOfType(): List<T> = map { item ->
  requireNotNull(item as? T) {
    "Expected ${T::class.simpleName}, but received ${item::class.simpleName}"
  }
}

private fun HomeSectionViewTypeUi?.isBanner(): Boolean =
  this == HomeSectionViewTypeUi.Banner || this == HomeSectionViewTypeUi.VerticalBanner

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF0B0B0F)
@Composable
private fun HomeContentPreview() {
  StreamTvTheme {
    StreamTvSurface {
      HomeContent(
        uiState = HomePreviewData.LoadedUiState,
        contentFocusRequester = remember { FocusRequester() },
        topBarFocusRequester = remember { FocusRequester() },
        // A preview holds no focus, so the restore effect must not try to claim it.
        isTopBarFocused = true,
        onItemClick = {},
        onTopBarOverlayVisibilityChange = {},
      )
    }
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF0B0B0F)
@Composable
private fun HomeContentLoadingPreview() {
  StreamTvTheme {
    StreamTvSurface {
      HomeContent(
        uiState = HomeUiState(isLoading = true),
        contentFocusRequester = remember { FocusRequester() },
        topBarFocusRequester = remember { FocusRequester() },
        isTopBarFocused = true,
        onItemClick = {},
        onTopBarOverlayVisibilityChange = {},
      )
    }
  }
}
