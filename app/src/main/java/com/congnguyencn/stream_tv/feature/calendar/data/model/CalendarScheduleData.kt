package com.congnguyencn.stream_tv.feature.calendar.data.model

internal data class CalendarDayData(val date: String, val channels: List<CalendarChannelData>)

internal data class CalendarChannelData(
  val channelId: String,
  val title: String,
  val logoUrl: String,
  val programs: List<CalendarProgramData>,
)

/** Mirrors the attached schedule JSON while carrying the presentation metadata dummy data needs. */
internal data class CalendarProgramData(
  val programId: String,
  val startTime: String,
  val stopTime: String,
  val title: String,
  val description: String,
  val thumbnailUrl: String,
)
