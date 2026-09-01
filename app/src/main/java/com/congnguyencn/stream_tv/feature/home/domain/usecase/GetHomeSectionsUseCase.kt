package com.congnguyencn.stream_tv.feature.home.domain.usecase

import com.congnguyencn.stream_tv.feature.home.domain.model.HomeSection
import com.congnguyencn.stream_tv.feature.home.domain.repository.HomeRepository

class GetHomeSectionsUseCase(private val repository: HomeRepository) {
  operator fun invoke(): List<HomeSection> = repository.getHomeSections()
}
