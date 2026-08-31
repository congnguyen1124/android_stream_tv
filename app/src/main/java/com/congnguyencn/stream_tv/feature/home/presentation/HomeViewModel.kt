package com.congnguyencn.stream_tv.feature.home.presentation

import androidx.lifecycle.ViewModel
import com.congnguyencn.stream_tv.feature.home.domain.usecase.GetHomeSectionsUseCase
import com.congnguyencn.stream_tv.feature.home.presentation.mapper.HomeUiMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
internal class HomeViewModel @Inject constructor(
    private val getHomeSections: GetHomeSectionsUseCase,
    private val uiMapper: HomeUiMapper,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = mutableUiState.asStateFlow()

    init {
        loadHome()
    }

    fun loadHome() {
        mutableUiState.value = HomeUiState(isLoading = true)
        mutableUiState.value = runCatching {
            HomeUiState(
                isLoading = false,
                sections = uiMapper.map(getHomeSections()),
            )
        }.getOrElse { throwable ->
            HomeUiState(
                isLoading = false,
                errorMessage = throwable.message ?: "Unable to load Home content",
            )
        }
    }
}
