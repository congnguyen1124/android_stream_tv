package com.congnguyencn.stream_tv.feature.search.domain.repository

import com.congnguyencn.stream_tv.feature.search.domain.model.SearchSection

interface SearchRepository {
  suspend fun getRecommendations(): List<SearchSection>

  suspend fun search(query: String): List<SearchSection>

  suspend fun getSuggestions(query: String): List<String>
}
