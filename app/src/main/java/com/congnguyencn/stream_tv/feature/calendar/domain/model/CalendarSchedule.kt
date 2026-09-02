package com.congnguyencn.stream_tv.feature.calendar.domain.model

import java.time.LocalDate

data class CalendarSchedule(val date: LocalDate, val channels: List<CalendarChannel>)

data class CalendarChannel(val id: String, val title: String, val logoUrl: String, val programs: List<CalendarProgram>)

data class CalendarProgram(
  val id: String,
  val title: String,
  val description: String,
  val thumbnailUrl: String,
  val startMinute: Int,
  val endMinute: Int,
) {
  val durationMinutes: Int
    get() = endMinute - startMinute
}
