package com.congnguyencn.stream_tv.feature.home.presentation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeViewModelTest {
    @Test
    fun `startExperience marks home as ready`() {
        val viewModel = HomeViewModel()

        assertFalse(viewModel.uiState.value.isReady)

        viewModel.startExperience()

        assertTrue(viewModel.uiState.value.isReady)
    }
}
