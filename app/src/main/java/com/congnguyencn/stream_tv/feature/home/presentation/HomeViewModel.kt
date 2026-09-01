package com.congnguyencn.stream_tv.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.congnguyencn.stream_tv.feature.home.domain.repository.HomeRepository
import com.congnguyencn.stream_tv.feature.home.presentation.mapper.HomeUiMapper
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
internal class HomeViewModel @Inject constructor(
  private val repository: HomeRepository,
  private val uiMapper: HomeUiMapper,
) : ViewModel() {
  private val _uiState = MutableStateFlow(HomeUiState())
  val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
  private var loadHomeJob: Job? = null

  init {
    loadHome()
  }

  @Suppress("TooGenericExceptionCaught") // This presentation boundary turns repository failures into UI state.
  fun loadHome() {
    loadHomeJob?.cancel()
    loadHomeJob = viewModelScope.launch {
      _uiState.update { state -> state.copy(isLoading = true, errorMessage = null) }
      try {
        val sections = repository.getHomeSections()
        val uiSections = uiMapper.map(sections)
        coroutineContext.ensureActive()
        _uiState.value = HomeUiState(
          isLoading = false,
          sections = uiSections,
        )
      } catch (cancellation: CancellationException) {
        throw cancellation
      } catch (exception: Exception) {
        showLoadError(exception)
      }
    }
  }

  private fun showLoadError(exception: Exception) {
    _uiState.update { state ->
      state.copy(
        isLoading = false,
        errorMessage = exception.message ?: "Unable to load Home content",
      )
    }
  }
}
