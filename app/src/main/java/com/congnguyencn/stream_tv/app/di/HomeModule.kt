package com.congnguyencn.stream_tv.app.di

import com.congnguyencn.stream_tv.feature.home.data.repository.DummyHomeRepository
import com.congnguyencn.stream_tv.feature.home.data.source.HomeDummyDataSource
import com.congnguyencn.stream_tv.feature.home.domain.repository.HomeRepository
import com.congnguyencn.stream_tv.feature.home.presentation.mapper.HomeUiMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object HomeModule {
  @Provides
  @Singleton
  fun provideHomeDummyDataSource(): HomeDummyDataSource = HomeDummyDataSource()

  @Provides
  @Singleton
  fun provideHomeRepository(dataSource: HomeDummyDataSource): HomeRepository = DummyHomeRepository(dataSource)

  @Provides
  @Singleton
  fun provideHomeUiMapper(): HomeUiMapper = HomeUiMapper()
}
