package com.congnguyencn.stream_tv.feature.calendar.presentation.mapper

import com.congnguyencn.stream_tv.feature.calendar.domain.model.CalendarSchedule
import com.congnguyencn.stream_tv.feature.calendar.presentation.model.CalendarChannelUiModel
import com.congnguyencn.stream_tv.feature.calendar.presentation.model.CalendarDayUiModel
import com.congnguyencn.stream_tv.feature.calendar.presentation.model.CalendarProgramUiModel
import java.time.format.DateTimeFormatter
import java.util.Locale

internal class CalendarUiMapper {
  fun map(schedule: CalendarSchedule): CalendarDayUiModel = CalendarDayUiModel(
    dateLabel = schedule.date.format(DateLabelFormatter).uppercase(Locale.ENGLISH),
    channels = schedule.channels.map { channel ->
      CalendarChannelUiModel(
        id = channel.id,
        title = channel.title,
        logoUrl = channel.logoUrl,
        programs = channel.programs.map { program ->
          CalendarProgramUiModel(
            id = program.id,
            title = program.title,
            description = program.description,
            thumbnailUrl = program.thumbnailUrl,
            startMinute = program.startMinute,
            endMinute = program.endMinute,
            timeLabel = "${program.startMinute.toClock()} – ${program.endMinute.toClock()}",
          )
        },
      )
    },
  )

  private fun Int.toClock(): String {
    val boundedMinute = coerceIn(0, DayEndMinute)
    if (boundedMinute == DayEndMinute) return "24:00"
    return String.format(Locale.ENGLISH, "%02d:%02d", boundedMinute / 60, boundedMinute % 60)
  }

  private companion object {
    const val DayEndMinute = 24 * 60
    val DateLabelFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM", Locale.ENGLISH)
  }
}
