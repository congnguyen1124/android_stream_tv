package com.congnguyencn.stream_tv.feature.home.domain.model

data class HomeSection(
  val id: String,
  val title: String,
  val viewType: HomeSectionViewType,
  val items: List<Content>,
) {
  init {
    require(items.isNotEmpty()) { "Home section $id must not be empty" }
    require(items.all(viewType::accepts)) {
      "Home section $id contains an item incompatible with $viewType"
    }
  }
}

enum class HomeSectionViewType {
  Banner,
  VerticalBanner,
  Videos,
  ListSeries,
  Channels,
  Shorts,
  ;

  fun accepts(content: Content): Boolean = when (this) {
    Banner, Videos -> content is Video
    VerticalBanner, Shorts -> content is Short
    ListSeries -> content is Series
    Channels -> content is Channel
  }
}
