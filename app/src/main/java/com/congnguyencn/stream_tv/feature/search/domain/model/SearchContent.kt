package com.congnguyencn.stream_tv.feature.search.domain.model

data class SearchContent(
  val id: String,
  val videoUrl: String,
  val thumbnailUrl: String,
  val title: String,
  val description: String,
  val ageRestriction: String?,
  val type: SearchContentType,
)

enum class SearchContentType {
  Video,
  Short,
}

data class SearchSection(val id: String, val type: SearchContentType, val items: List<SearchContent>) {
  init {
    require(items.all { item -> item.type == type }) {
      "Search section $id contains an item incompatible with $type"
    }
  }
}
