package com.congnguyencn.stream_tv.feature.setting.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
internal class SettingViewModel @Inject constructor() : ViewModel() {
    private val mutableUiState = MutableStateFlow(SettingUiState())
    val uiState: StateFlow<SettingUiState> = mutableUiState.asStateFlow()

    fun openSetting() {
        mutableUiState.update { it.copy(isSettingReady = true) }
    }
}
