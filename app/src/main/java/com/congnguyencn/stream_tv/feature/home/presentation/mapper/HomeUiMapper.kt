package com.congnguyencn.stream_tv.feature.home.presentation.mapper

import com.congnguyencn.stream_tv.feature.home.domain.model.Channel
import com.congnguyencn.stream_tv.feature.home.domain.model.Content
import com.congnguyencn.stream_tv.feature.home.domain.model.HomeSection
import com.congnguyencn.stream_tv.feature.home.domain.model.HomeSectionViewType
import com.congnguyencn.stream_tv.feature.home.domain.model.Series
import com.congnguyencn.stream_tv.feature.home.domain.model.Short
import com.congnguyencn.stream_tv.feature.home.domain.model.Video
import com.congnguyencn.stream_tv.feature.home.presentation.model.ChannelUiItem
import com.congnguyencn.stream_tv.feature.home.presentation.model.HomeContentUiItem
import com.congnguyencn.stream_tv.feature.home.presentation.model.HomeSectionUiItem
import com.congnguyencn.stream_tv.feature.home.presentation.model.HomeSectionViewTypeUi
import com.congnguyencn.stream_tv.feature.home.presentation.model.SeriesUiItem
import com.congnguyencn.stream_tv.feature.home.presentation.model.ShortUiItem
import com.congnguyencn.stream_tv.feature.home.presentation.model.VideoUiItem

internal class HomeUiMapper {
  fun map(sections: List<HomeSection>): List<HomeSectionUiItem> = sections.map(::map)

  private fun map(section: HomeSection): HomeSectionUiItem = HomeSectionUiItem(
    id = section.id,
    title = section.title,
    viewType = section.viewType.toUi(),
    items = section.items.map(Content::toUi),
  )
}

private fun Content.toUi(): HomeContentUiItem = when (this) {
  is Video -> toUi()

  is Series -> SeriesUiItem(
    id = id,
    videoUrl = videoUrl,
    trailerUrl = trailerUrl,
    thumbnailUrl = thumbnailUrl,
    vastUrl = vastUrl,
    title = title,
    description = description,
    ageRestriction = ageRestriction,
    logoUrl = logoUrl,
    episodes = episodes.map(Video::toUi),
  )

  is Channel -> ChannelUiItem(
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

  is Short -> ShortUiItem(
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

private fun Video.toUi(): VideoUiItem = VideoUiItem(
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

private fun HomeSectionViewType.toUi(): HomeSectionViewTypeUi = when (this) {
  HomeSectionViewType.Banner -> HomeSectionViewTypeUi.Banner
  HomeSectionViewType.VerticalBanner -> HomeSectionViewTypeUi.VerticalBanner
  HomeSectionViewType.Videos -> HomeSectionViewTypeUi.Videos
  HomeSectionViewType.ListSeries -> HomeSectionViewTypeUi.ListSeries
  HomeSectionViewType.Channels -> HomeSectionViewTypeUi.Channels
  HomeSectionViewType.Shorts -> HomeSectionViewTypeUi.Shorts
}
