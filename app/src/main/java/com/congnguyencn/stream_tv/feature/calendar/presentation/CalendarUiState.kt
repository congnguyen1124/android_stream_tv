package com.congnguyencn.stream_tv.feature.calendar.presentation

import androidx.compose.runtime.Immutable
import com.congnguyencn.stream_tv.feature.calendar.presentation.model.CalendarDayUiModel

@Immutable
data class CalendarUiState(
  val isLoading: Boolean = true,
  val schedule: CalendarDayUiModel? = null,
  val errorMessage: String? = null,
)
