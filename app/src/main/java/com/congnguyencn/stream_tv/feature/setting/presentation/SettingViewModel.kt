package com.congnguyencn.stream_tv.feature.setting.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.congnguyencn.stream_tv.feature.setting.domain.repository.SettingRepository
import com.congnguyencn.stream_tv.feature.setting.presentation.model.SettingItemUi
import com.congnguyencn.stream_tv.feature.setting.presentation.model.toUiModel
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
internal class SettingViewModel @Inject constructor(private val repository: SettingRepository) : ViewModel() {
  private val mutableUiState = MutableStateFlow(SettingUiState())
  val uiState: StateFlow<SettingUiState> = mutableUiState.asStateFlow()
  private var loadJob: Job? = null

  init {
    loadSystemInfo()
  }

  @Suppress("TooGenericExceptionCaught") // This presentation boundary turns repository failures into UI state.
  fun loadSystemInfo() {
    loadJob?.cancel()
    loadJob = viewModelScope.launch {
      mutableUiState.update { state ->
        state.copy(isLoadingSystemInfo = true, systemInfoErrorMessage = null)
      }
      try {
        val systemInfo = repository.getSystemInfo().toUiModel()
        coroutineContext.ensureActive()
        mutableUiState.update { state ->
          state.copy(
            isLoadingSystemInfo = false,
            systemInfo = systemInfo,
            systemInfoErrorMessage = null,
          )
        }
      } catch (cancellation: CancellationException) {
        throw cancellation
      } catch (exception: Exception) {
        mutableUiState.update { state ->
          state.copy(
            isLoadingSystemInfo = false,
            systemInfoErrorMessage = exception.message ?: DefaultSystemInfoErrorMessage,
          )
        }
      }
    }
  }

  fun selectItem(item: SettingItemUi) {
    mutableUiState.update { state -> state.copy(selectedItem = item) }
  }

  fun clearSearchHistory() {
    mutableUiState.update { state -> state.copy(isSearchHistoryCleared = true) }
  }

  fun clearWatchHistory() {
    mutableUiState.update { state -> state.copy(isWatchHistoryCleared = true) }
  }

  private companion object {
    const val DefaultSystemInfoErrorMessage = "Unable to read device information"
  }
}
