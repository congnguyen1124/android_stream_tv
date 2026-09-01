package com.congnguyencn.stream_tv.feature.home.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
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
  val SectionSpacing = 34.dp
  val BottomPadding = 54.dp
  val MessagePadding = 32.dp
}

@Composable
internal fun HomeContent(
  uiState: HomeUiState,
  contentFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
  onItemClick: (HomeContentUiItem) -> Unit,
  onTopBarOverlayVisibilityChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  bannerTrailer: @Composable (item: VideoUiItem, isBannerFocused: Boolean) -> Unit = { _, _ -> },
) {
  var focusedSectionIndex by remember { mutableIntStateOf(HomeContentDefaults.FirstSectionIndex) }

  // The opening section fills the screen behind the top bar and paints its own gradients, so the bar
  // only needs a scrim of its own once focus has moved past it onto the rows underneath.
  val isTopBarOverlayVisible = focusedSectionIndex > HomeContentDefaults.FirstSectionIndex

  LaunchedEffect(uiState.sections) {
    if (uiState.sections.any { it.viewType == HomeSectionViewTypeUi.Banner }) {
      contentFocusRequester.requestFocus()
    }
  }

  LaunchedEffect(isTopBarOverlayVisible) {
    onTopBarOverlayVisibilityChange(isTopBarOverlayVisible)
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

    else -> LazyColumn(
      modifier = modifier.fillMaxSize(),
      contentPadding = PaddingValues(
        top = if (uiState.sections.firstOrNull()?.viewType.isBanner()) {
          0.dp
        } else {
          StreamTvDimensions.TopBarHeight
        },
        bottom = HomeContentDefaults.BottomPadding,
      ),
      verticalArrangement = Arrangement.spacedBy(HomeContentDefaults.SectionSpacing),
    ) {
      itemsIndexed(
        items = uiState.sections,
        key = { _, section -> section.id },
      ) { index, section ->
        HomeSection(
          section = section,
          contentFocusRequester = contentFocusRequester,
          topBarFocusRequester = topBarFocusRequester,
          onItemClick = onItemClick,
          modifier = Modifier.onFocusChanged { focusState ->
            if (focusState.hasFocus) {
              focusedSectionIndex = index
            }
          },
          bannerTrailer = bannerTrailer,
        )
      }
    }
  }
}

@Composable
private fun HomeSection(
  section: HomeSectionUiItem,
  contentFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
  onItemClick: (HomeContentUiItem) -> Unit,
  modifier: Modifier = Modifier,
  bannerTrailer: @Composable (item: VideoUiItem, isBannerFocused: Boolean) -> Unit = { _, _ -> },
) {
  when (section.viewType) {
    HomeSectionViewTypeUi.Banner -> HomeBannerSection(
      items = section.items.requireItemsOfType<VideoUiItem>(),
      contentFocusRequester = contentFocusRequester,
      topBarFocusRequester = topBarFocusRequester,
      modifier = modifier,
      onItemClick = onItemClick,
      bannerTrailer = bannerTrailer,
    )

    HomeSectionViewTypeUi.VerticalBanner -> HomeVerticalBannerSection(
      items = section.items.requireItemsOfType<ShortUiItem>(),
      modifier = modifier,
      onItemClick = onItemClick,
    )

    HomeSectionViewTypeUi.Videos -> HomeContentRowSection(
      sectionId = section.id,
      title = section.title,
      items = section.items.requireItemsOfType<VideoUiItem>(),
      style = HomeContentRowStyle.Video,
      modifier = modifier,
      onItemClick = onItemClick,
    )

    HomeSectionViewTypeUi.ListSeries -> HomeContentRowSection(
      sectionId = section.id,
      title = section.title,
      items = section.items.requireItemsOfType<SeriesUiItem>(),
      style = HomeContentRowStyle.Series,
      modifier = modifier,
      onItemClick = onItemClick,
    )

    HomeSectionViewTypeUi.Channels -> HomeContentRowSection(
      sectionId = section.id,
      title = section.title,
      items = section.items.requireItemsOfType<ChannelUiItem>(),
      style = HomeContentRowStyle.Channel,
      modifier = modifier,
      onItemClick = onItemClick,
    )

    HomeSectionViewTypeUi.Shorts -> HomeContentRowSection(
      sectionId = section.id,
      title = section.title,
      items = section.items.requireItemsOfType<ShortUiItem>(),
      style = HomeContentRowStyle.Short,
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
