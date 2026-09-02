package com.congnguyencn.stream_tv.feature.search.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
data class SearchContentUiItem(
  val id: String,
  val videoUrl: String,
  val thumbnailUrl: String,
  val title: String,
  val description: String,
  val ageRestriction: String?,
  val type: SearchContentTypeUi,
)

enum class SearchContentTypeUi {
  Video,
  Short,
}

@Immutable
data class SearchSectionUiItem(
  val id: String,
  val title: String,
  val type: SearchContentTypeUi,
  val items: List<SearchContentUiItem>,
)
