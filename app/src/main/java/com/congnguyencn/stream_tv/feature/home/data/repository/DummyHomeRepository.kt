package com.congnguyencn.stream_tv.feature.home.data.repository

import com.congnguyencn.stream_tv.feature.home.data.mapper.toDomain
import com.congnguyencn.stream_tv.feature.home.data.source.HomeDummyDataSource
import com.congnguyencn.stream_tv.feature.home.domain.model.HomeSection
import com.congnguyencn.stream_tv.feature.home.domain.repository.HomeRepository

internal class DummyHomeRepository(private val dataSource: HomeDummyDataSource) : HomeRepository {
  override suspend fun getHomeSections(): List<HomeSection> =
    dataSource.getHomeSections().map { section -> section.toDomain() }
}
