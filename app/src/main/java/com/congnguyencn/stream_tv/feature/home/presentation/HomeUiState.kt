package com.congnguyencn.stream_tv.feature.home.presentation

import com.congnguyencn.stream_tv.feature.home.presentation.model.HomeSectionUiItem

data class HomeUiState(
    val isLoading: Boolean = true,
    val sections: List<HomeSectionUiItem> = emptyList(),
    val errorMessage: String? = null,
)
