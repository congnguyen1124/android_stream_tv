package com.congnguyencn.stream_tv.feature.calendar.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.congnguyencn.stream_tv.feature.calendar.domain.repository.CalendarRepository
import com.congnguyencn.stream_tv.feature.calendar.presentation.mapper.CalendarUiMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
internal class CalendarViewModel @Inject constructor(
  private val repository: CalendarRepository,
  private val uiMapper: CalendarUiMapper,
) : ViewModel() {
  private val mutableUiState = MutableStateFlow(CalendarUiState())
  val uiState: StateFlow<CalendarUiState> = mutableUiState.asStateFlow()
  private var loadJob: Job? = null

  init {
    loadSchedule()
  }

  @Suppress("TooGenericExceptionCaught")
  fun loadSchedule() {
    loadJob?.cancel()
    loadJob = viewModelScope.launch {
      mutableUiState.update { state -> state.copy(isLoading = true, errorMessage = null) }
      try {
        val schedule = uiMapper.map(repository.getCalendarSchedule())
        coroutineContext.ensureActive()
        mutableUiState.value = CalendarUiState(
          isLoading = false,
          schedule = schedule,
        )
      } catch (cancellation: CancellationException) {
        throw cancellation
      } catch (exception: Exception) {
        mutableUiState.update { state ->
          state.copy(
            isLoading = false,
            errorMessage = exception.message ?: "Unable to load the program guide",
          )
        }
      }
    }
  }
}
