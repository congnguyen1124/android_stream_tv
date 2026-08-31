package com.congnguyencn.stream_tv.feature.home.data.model

internal data class HomeSectionData(
    val id: String,
    val title: String,
    val viewType: HomeSectionViewTypeData,
    val items: List<HomeContentData>,
)

internal enum class HomeSectionViewTypeData {
    Banner,
    VerticalBanner,
    Videos,
    ListSeries,
    Channels,
    Shorts,
}
