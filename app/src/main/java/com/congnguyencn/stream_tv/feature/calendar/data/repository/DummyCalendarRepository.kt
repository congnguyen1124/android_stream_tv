package com.congnguyencn.stream_tv.feature.calendar.data.repository

import com.congnguyencn.stream_tv.feature.calendar.data.mapper.CalendarDataMapper
import com.congnguyencn.stream_tv.feature.calendar.data.source.CalendarDummyDataSource
import com.congnguyencn.stream_tv.feature.calendar.domain.model.CalendarSchedule
import com.congnguyencn.stream_tv.feature.calendar.domain.repository.CalendarRepository

internal class DummyCalendarRepository(
  private val dataSource: CalendarDummyDataSource,
  private val mapper: CalendarDataMapper,
) : CalendarRepository {
  override suspend fun getCalendarSchedule(): CalendarSchedule = mapper.map(dataSource.getSchedule())
}
