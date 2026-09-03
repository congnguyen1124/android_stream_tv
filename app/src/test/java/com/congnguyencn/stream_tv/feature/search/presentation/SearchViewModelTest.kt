package com.congnguyencn.stream_tv.feature.search.presentation

import com.congnguyencn.stream_tv.core.testing.MainDispatcherRule
import com.congnguyencn.stream_tv.feature.search.domain.model.SearchContent
import com.congnguyencn.stream_tv.feature.search.domain.model.SearchContentType
import com.congnguyencn.stream_tv.feature.search.domain.model.SearchSection
import com.congnguyencn.stream_tv.feature.search.domain.repository.SearchRepository
import com.congnguyencn.stream_tv.feature.search.presentation.mapper.SearchUiMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
  @get:Rule
  val mainDispatcherRule = MainDispatcherRule()

  @Test
  fun `keyboard editing inserts and deletes at the caret`() = runTest(mainDispatcherRule.testDispatcher) {
    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.onKeyInput("c")
    viewModel.onKeyInput("a")
    viewModel.onKeyInput("t")
    viewModel.onCursorLeft()
    viewModel.onKeyInput("r")
    viewModel.onBackspace()

    assertEquals("cat", viewModel.uiState.value.query)
    assertEquals(2, viewModel.uiState.value.cursorPosition)
  }

  @Test
  fun `submitting a query closes keyboard and publishes mapped results`() = runTest(mainDispatcherRule.testDispatcher) {
    val repository = FakeSearchRepository()
    val viewModel = SearchViewModel(repository, SearchUiMapper())
    advanceUntilIdle()

    "tiger".forEach { character -> viewModel.onKeyInput(character.toString()) }
    viewModel.submitSearch()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isKeyboardVisible)
    assertFalse(state.isSearching)
    assertEquals("tiger", state.submittedQuery)
    assertEquals("Videos", state.sections.single().title)
    assertEquals("Realm of the tiger", state.sections.single().items.single().title)
    assertEquals("tiger", repository.lastQuery)
  }

  @Test
  fun `blank query does not dismiss the keyboard`() = runTest(mainDispatcherRule.testDispatcher) {
    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.submitSearch()

    assertTrue(viewModel.uiState.value.isKeyboardVisible)
    assertEquals(null, viewModel.uiState.value.submittedQuery)
  }

  private fun createViewModel() = SearchViewModel(
    repository = FakeSearchRepository(),
    uiMapper = SearchUiMapper(),
  )
}

private class FakeSearchRepository : SearchRepository {
  var lastQuery: String? = null

  override suspend fun getRecommendations(): List<SearchSection> = testSections()

  override suspend fun search(query: String): List<SearchSection> {
    lastQuery = query
    return testSections()
  }

  override suspend fun getSuggestions(query: String): List<String> = listOf("Wildlife documentaries")

  private fun testSections() = listOf(
    SearchSection(
      id = "videos",
      type = SearchContentType.Video,
      items = listOf(
        SearchContent(
          id = "video-tiger",
          videoUrl = "https://example.com/tiger.m3u8",
          thumbnailUrl = "https://example.com/tiger.jpg",
          title = "Realm of the tiger",
          description = "A journey through the wild.",
          ageRestriction = "T13",
          type = SearchContentType.Video,
        ),
      ),
    ),
  )
}
