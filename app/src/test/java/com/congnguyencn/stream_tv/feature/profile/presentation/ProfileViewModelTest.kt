package com.congnguyencn.stream_tv.feature.profile.presentation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileViewModelTest {
    @Test
    fun `openProfile marks profile as ready`() {
        val viewModel = ProfileViewModel()

        assertFalse(viewModel.uiState.value.isProfileReady)

        viewModel.openProfile()

        assertTrue(viewModel.uiState.value.isProfileReady)
    }
}
