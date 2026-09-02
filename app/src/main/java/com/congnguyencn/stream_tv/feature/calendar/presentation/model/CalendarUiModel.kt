package com.congnguyencn.stream_tv.feature.calendar.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
data class CalendarDayUiModel(val dateLabel: String, val channels: List<CalendarChannelUiModel>)

@Immutable
data class CalendarChannelUiModel(
  val id: String,
  val title: String,
  val logoUrl: String,
  val programs: List<CalendarProgramUiModel>,
)

@Immutable
data class CalendarProgramUiModel(
  val id: String,
  val title: String,
  val description: String,
  val thumbnailUrl: String,
  val startMinute: Int,
  val endMinute: Int,
  val timeLabel: String,
) {
  val durationMinutes: Int
    get() = endMinute - startMinute
}
