package com.congnguyencn.stream_tv.feature.search.presentation.mapper

import com.congnguyencn.stream_tv.feature.search.domain.model.SearchContent
import com.congnguyencn.stream_tv.feature.search.domain.model.SearchContentType
import com.congnguyencn.stream_tv.feature.search.domain.model.SearchSection
import com.congnguyencn.stream_tv.feature.search.presentation.model.SearchContentTypeUi
import com.congnguyencn.stream_tv.feature.search.presentation.model.SearchContentUiItem
import com.congnguyencn.stream_tv.feature.search.presentation.model.SearchSectionUiItem

internal class SearchUiMapper {
  fun map(sections: List<SearchSection>): List<SearchSectionUiItem> = sections.map { section ->
    SearchSectionUiItem(
      id = section.id,
      title = when (section.type) {
        SearchContentType.Video -> "Videos"
        SearchContentType.Short -> "Shorts"
      },
      type = section.type.toUi(),
      items = section.items.map(SearchContent::toUi),
    )
  }
}

private fun SearchContent.toUi() = SearchContentUiItem(
  id = id,
  videoUrl = videoUrl,
  thumbnailUrl = thumbnailUrl,
  title = title,
  description = description,
  ageRestriction = ageRestriction,
  type = type.toUi(),
)

private fun SearchContentType.toUi(): SearchContentTypeUi = when (this) {
  SearchContentType.Video -> SearchContentTypeUi.Video
  SearchContentType.Short -> SearchContentTypeUi.Short
}
