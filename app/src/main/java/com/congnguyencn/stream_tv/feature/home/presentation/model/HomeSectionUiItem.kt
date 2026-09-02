package com.congnguyencn.stream_tv.feature.home.presentation.model

data class HomeSectionUiItem(
  val id: String,
  val title: String,
  val viewType: HomeSectionViewTypeUi,
  val items: List<HomeContentUiItem>,
) {
  init {
    require(items.all(viewType::accepts)) {
      "UI section $id contains an item incompatible with $viewType"
    }
  }
}

enum class HomeSectionViewTypeUi {
  Banner,
  VerticalBanner,
  Videos,
  VideosPopular,
  ListSeries,
  Channels,
  Shorts,
  ShortPopular,
  ;

  internal fun accepts(item: HomeContentUiItem): Boolean = when (this) {
    Banner, Videos, VideosPopular -> item is VideoUiItem
    VerticalBanner, Shorts, ShortPopular -> item is ShortUiItem
    ListSeries -> item is SeriesUiItem
    Channels -> item is ChannelUiItem
  }
}
