package com.congnguyencn.stream_tv.feature.home.presentation.model

sealed interface HomeContentUiItem {
    val id: String
    val videoUrl: String
    val thumbnailUrl: String
    val vastUrl: String
    val title: String
    val description: String
    val ageRestriction: String?
    val logoUrl: String
}

data class VideoUiItem(
    override val id: String,
    override val videoUrl: String,
    override val thumbnailUrl: String,
    override val vastUrl: String,
    override val title: String,
    override val description: String,
    override val ageRestriction: String?,
    override val logoUrl: String,
) : HomeContentUiItem

data class SeriesUiItem(
    override val id: String,
    override val videoUrl: String,
    override val thumbnailUrl: String,
    override val vastUrl: String,
    override val title: String,
    override val description: String,
    override val ageRestriction: String?,
    override val logoUrl: String,
    val episodes: List<VideoUiItem>,
) : HomeContentUiItem

data class ChannelUiItem(
    override val id: String,
    override val videoUrl: String,
    override val thumbnailUrl: String,
    override val vastUrl: String,
    override val title: String,
    override val description: String,
    override val ageRestriction: String?,
    override val logoUrl: String,
) : HomeContentUiItem

data class ShortUiItem(
    override val id: String,
    override val videoUrl: String,
    override val thumbnailUrl: String,
    override val vastUrl: String,
    override val title: String,
    override val description: String,
    override val ageRestriction: String?,
    override val logoUrl: String,
) : HomeContentUiItem
