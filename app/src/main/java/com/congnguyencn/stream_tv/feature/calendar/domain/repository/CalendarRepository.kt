package com.congnguyencn.stream_tv.feature.calendar.domain.repository

import com.congnguyencn.stream_tv.feature.calendar.domain.model.CalendarSchedule

fun interface CalendarRepository {
  suspend fun getCalendarSchedule(): CalendarSchedule
}
