package com.congnguyencn.stream_tv.feature.home.data.model

internal sealed interface HomeContentData {
  val id: String
  val videoUrl: String
  val thumbnailUrl: String
  val vastUrl: String
  val title: String
  val description: String
  val ageRestriction: String?
  val logoUrl: String
}

internal data class VideoData(
  override val id: String,
  override val videoUrl: String = "",
  override val thumbnailUrl: String,
  override val vastUrl: String = "",
  override val title: String,
  override val description: String,
  override val ageRestriction: String? = null,
  override val logoUrl: String = "",
) : HomeContentData

internal data class SeriesData(
  override val id: String,
  override val videoUrl: String = "",
  override val thumbnailUrl: String,
  override val vastUrl: String = "",
  override val title: String,
  override val description: String,
  override val ageRestriction: String? = null,
  override val logoUrl: String = "",
  val episodes: List<VideoData>,
) : HomeContentData

internal data class ChannelData(
  override val id: String,
  override val videoUrl: String = "",
  override val thumbnailUrl: String,
  override val vastUrl: String = "",
  override val title: String,
  override val description: String,
  override val ageRestriction: String? = null,
  override val logoUrl: String = "",
) : HomeContentData

internal data class ShortData(
  override val id: String,
  override val videoUrl: String = "",
  override val thumbnailUrl: String,
  override val vastUrl: String = "",
  override val title: String,
  override val description: String,
  override val ageRestriction: String? = null,
  override val logoUrl: String = "",
) : HomeContentData
