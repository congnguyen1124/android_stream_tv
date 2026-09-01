package com.congnguyencn.stream_tv.feature.search.presentation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchViewModelTest {
  @Test
  fun `openSearch marks search as ready`() {
    val viewModel = SearchViewModel()

    assertFalse(viewModel.uiState.value.isSearchReady)

    viewModel.openSearch()

    assertTrue(viewModel.uiState.value.isSearchReady)
  }
}
