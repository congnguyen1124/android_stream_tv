package com.congnguyencn.stream_tv.feature.setting.presentation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingViewModelTest {
    @Test
    fun `openSetting marks setting as ready`() {
        val viewModel = SettingViewModel()

        assertFalse(viewModel.uiState.value.isSettingReady)

        viewModel.openSetting()

        assertTrue(viewModel.uiState.value.isSettingReady)
    }
}
