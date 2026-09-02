package com.congnguyencn.stream_tv.feature.calendar.data.mapper

import com.congnguyencn.stream_tv.feature.calendar.data.model.CalendarChannelData
import com.congnguyencn.stream_tv.feature.calendar.data.model.CalendarDayData
import com.congnguyencn.stream_tv.feature.calendar.data.model.CalendarProgramData
import com.congnguyencn.stream_tv.feature.calendar.domain.model.CalendarChannel
import com.congnguyencn.stream_tv.feature.calendar.domain.model.CalendarProgram
import com.congnguyencn.stream_tv.feature.calendar.domain.model.CalendarSchedule
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

internal class CalendarDataMapper {
  fun map(source: CalendarDayData): CalendarSchedule {
    val scheduleDate = LocalDate.parse(source.date)
    return CalendarSchedule(
      date = scheduleDate,
      channels = source.channels.map { channel -> channel.toDomain(scheduleDate) },
    )
  }

  private fun CalendarChannelData.toDomain(scheduleDate: LocalDate): CalendarChannel = CalendarChannel(
    id = channelId,
    title = title,
    logoUrl = logoUrl,
    programs = programs.mapNotNull { program -> program.toDomain(scheduleDate) }
      .sortedBy(CalendarProgram::startMinute),
  )

  private fun CalendarProgramData.toDomain(scheduleDate: LocalDate): CalendarProgram? {
    val dayStart = scheduleDate.atStartOfDay()
    val start = LocalDateTime.parse(startTime, ScheduleDateTimeFormatter)
    val stop = LocalDateTime.parse(stopTime, ScheduleDateTimeFormatter)
    val startMinute = Duration.between(dayStart, start).toMinutes().toInt().coerceIn(DayStartMinute, DayEndMinute)
    val endMinute = Duration.between(dayStart, stop).toMinutes().toInt().coerceIn(DayStartMinute, DayEndMinute)
    if (endMinute <= startMinute) return null

    return CalendarProgram(
      id = programId,
      title = title,
      description = description,
      thumbnailUrl = thumbnailUrl,
      startMinute = startMinute,
      endMinute = endMinute,
    )
  }

  private companion object {
    const val DayStartMinute = 0
    const val DayEndMinute = 24 * 60
    val ScheduleDateTimeFormatter: DateTimeFormatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS", Locale.ENGLISH)
  }
}
