package com.congnguyencn.stream_tv.app.di

import com.congnguyencn.stream_tv.feature.calendar.data.mapper.CalendarDataMapper
import com.congnguyencn.stream_tv.feature.calendar.data.repository.DummyCalendarRepository
import com.congnguyencn.stream_tv.feature.calendar.data.source.CalendarDummyDataSource
import com.congnguyencn.stream_tv.feature.calendar.domain.repository.CalendarRepository
import com.congnguyencn.stream_tv.feature.calendar.presentation.mapper.CalendarUiMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object CalendarModule {
  @Provides
  @Singleton
  fun provideCalendarDummyDataSource(): CalendarDummyDataSource = CalendarDummyDataSource()

  @Provides
  @Singleton
  fun provideCalendarDataMapper(): CalendarDataMapper = CalendarDataMapper()

  @Provides
  @Singleton
  fun provideCalendarRepository(
    dataSource: CalendarDummyDataSource,
    mapper: CalendarDataMapper,
  ): CalendarRepository = DummyCalendarRepository(dataSource, mapper)

  @Provides
  @Singleton
  fun provideCalendarUiMapper(): CalendarUiMapper = CalendarUiMapper()
}
