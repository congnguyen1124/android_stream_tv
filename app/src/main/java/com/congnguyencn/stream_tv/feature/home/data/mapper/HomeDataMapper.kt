package com.congnguyencn.stream_tv.feature.home.data.mapper

import com.congnguyencn.stream_tv.feature.home.data.model.ChannelData
import com.congnguyencn.stream_tv.feature.home.data.model.HomeContentData
import com.congnguyencn.stream_tv.feature.home.data.model.HomeSectionData
import com.congnguyencn.stream_tv.feature.home.data.model.HomeSectionViewTypeData
import com.congnguyencn.stream_tv.feature.home.data.model.SeriesData
import com.congnguyencn.stream_tv.feature.home.data.model.ShortData
import com.congnguyencn.stream_tv.feature.home.data.model.VideoData
import com.congnguyencn.stream_tv.feature.home.domain.model.Channel
import com.congnguyencn.stream_tv.feature.home.domain.model.Content
import com.congnguyencn.stream_tv.feature.home.domain.model.HomeSection
import com.congnguyencn.stream_tv.feature.home.domain.model.HomeSectionViewType
import com.congnguyencn.stream_tv.feature.home.domain.model.Series
import com.congnguyencn.stream_tv.feature.home.domain.model.Short
import com.congnguyencn.stream_tv.feature.home.domain.model.Video

internal fun HomeSectionData.toDomain(): HomeSection = HomeSection(
  id = id,
  title = title,
  viewType = viewType.toDomain(),
  items = items.map(HomeContentData::toDomain),
)

private fun HomeContentData.toDomain(): Content = when (this) {
  is VideoData -> toDomain()

  is SeriesData -> Series(
    id = id,
    videoUrl = videoUrl,
    trailerUrl = trailerUrl,
    thumbnailUrl = thumbnailUrl,
    vastUrl = vastUrl,
    title = title,
    description = description,
    ageRestriction = ageRestriction,
    logoUrl = logoUrl,
    episodes = episodes.map(VideoData::toDomain),
  )

  is ChannelData -> Channel(
    id = id,
    videoUrl = videoUrl,
    trailerUrl = trailerUrl,
    thumbnailUrl = thumbnailUrl,
    vastUrl = vastUrl,
    title = title,
    description = description,
    ageRestriction = ageRestriction,
    logoUrl = logoUrl,
  )

  is ShortData -> Short(
    id = id,
    videoUrl = videoUrl,
    trailerUrl = trailerUrl,
    thumbnailUrl = thumbnailUrl,
    vastUrl = vastUrl,
    title = title,
    description = description,
    ageRestriction = ageRestriction,
    logoUrl = logoUrl,
  )
}

private fun VideoData.toDomain(): Video = Video(
  id = id,
  videoUrl = videoUrl,
  trailerUrl = trailerUrl,
  thumbnailUrl = thumbnailUrl,
  vastUrl = vastUrl,
  title = title,
  description = description,
  ageRestriction = ageRestriction,
  logoUrl = logoUrl,
)

private fun HomeSectionViewTypeData.toDomain(): HomeSectionViewType = when (this) {
  HomeSectionViewTypeData.Banner -> HomeSectionViewType.Banner
  HomeSectionViewTypeData.VerticalBanner -> HomeSectionViewType.VerticalBanner
  HomeSectionViewTypeData.Videos -> HomeSectionViewType.Videos
  HomeSectionViewTypeData.ListSeries -> HomeSectionViewType.ListSeries
  HomeSectionViewTypeData.Channels -> HomeSectionViewType.Channels
  HomeSectionViewTypeData.Shorts -> HomeSectionViewType.Shorts
}
