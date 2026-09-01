package com.congnguyencn.stream_tv.feature.home.presentation.component

import com.congnguyencn.stream_tv.feature.home.presentation.HomeUiState
import com.congnguyencn.stream_tv.feature.home.presentation.model.HomeSectionUiItem
import com.congnguyencn.stream_tv.feature.home.presentation.model.HomeSectionViewTypeUi
import com.congnguyencn.stream_tv.feature.home.presentation.model.VideoUiItem

/**
 * Fixtures for the Home previews.
 *
 * Deliberately offline: the URLs are never fetched in a preview, so every field is a literal and no
 * preview here needs a repository, a player or a network image to render the same way twice.
 */
internal object HomePreviewData {
  val Videos: List<VideoUiItem> = listOf(
    video(
      id = "video-1",
      title = "Pulse of the Court",
      description = "A season inside the league's fastest offence, told by the players who ran it.",
    ),
    video(
      id = "video-2",
      title = "Second Wind",
      description = "Four climbers, one unfinished route and a weather window closing by the hour.",
    ),
    video(
      id = "video-3",
      title = "Northern Lines",
      description = "The night train that keeps a coastline connected through the darkest months.",
    ),
    video(
      id = "video-4",
      title = "Salt and Iron",
      description = "How a shipyard town rebuilt itself around the boats it refused to stop making.",
    ),
    video(
      id = "video-5",
      title = "Quiet Hours",
      description = "A city orchestra rehearses the piece nobody expected them to attempt.",
    ),
  )

  val BannerSection: HomeSectionUiItem = HomeSectionUiItem(
    id = "featured",
    title = "Featured today",
    viewType = HomeSectionViewTypeUi.Banner,
    items = Videos,
  )

  val VideosSection: HomeSectionUiItem = HomeSectionUiItem(
    id = "trending",
    title = "Trending now",
    viewType = HomeSectionViewTypeUi.Videos,
    items = Videos,
  )

  val ContinueWatchingSection: HomeSectionUiItem = HomeSectionUiItem(
    id = "continue-watching",
    title = "Continue watching",
    viewType = HomeSectionViewTypeUi.Videos,
    items = Videos.asReversed(),
  )

  val LoadedUiState: HomeUiState = HomeUiState(
    isLoading = false,
    sections = listOf(BannerSection, VideosSection, ContinueWatchingSection),
  )

  private fun video(id: String, title: String, description: String) = VideoUiItem(
    id = id,
    videoUrl = "",
    trailerUrl = "",
    thumbnailUrl = "",
    vastUrl = "",
    title = title,
    description = description,
    ageRestriction = "P",
    logoUrl = "",
  )
}
