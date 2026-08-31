package com.congnguyencn.stream_tv.feature.setting.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class SettingViewModel : ViewModel() {
    private val mutableUiState = MutableStateFlow(SettingUiState())
    val uiState: StateFlow<SettingUiState> = mutableUiState.asStateFlow()

    fun openSetting() {
        mutableUiState.update { it.copy(isSettingReady = true) }
    }
}
