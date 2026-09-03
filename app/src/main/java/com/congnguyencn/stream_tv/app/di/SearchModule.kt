package com.congnguyencn.stream_tv.app.di

import com.congnguyencn.stream_tv.feature.home.domain.repository.HomeRepository
import com.congnguyencn.stream_tv.feature.search.data.repository.DummySearchRepository
import com.congnguyencn.stream_tv.feature.search.domain.repository.SearchRepository
import com.congnguyencn.stream_tv.feature.search.presentation.mapper.SearchUiMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object SearchModule {
  @Provides
  @Singleton
  fun provideSearchRepository(homeRepository: HomeRepository): SearchRepository = DummySearchRepository(homeRepository)

  @Provides
  @Singleton
  fun provideSearchUiMapper(): SearchUiMapper = SearchUiMapper()
}
