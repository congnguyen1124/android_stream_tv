package com.congnguyencn.stream_tv.feature.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.congnguyencn.stream_tv.feature.search.domain.repository.SearchRepository
import com.congnguyencn.stream_tv.feature.search.presentation.mapper.SearchUiMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
internal class SearchViewModel @Inject constructor(
  private val repository: SearchRepository,
  private val uiMapper: SearchUiMapper,
) : ViewModel() {
  private val mutableUiState = MutableStateFlow(SearchUiState())
  val uiState: StateFlow<SearchUiState> = mutableUiState.asStateFlow()

  private var loadJob: Job? = null
  private var suggestionJob: Job? = null

  init {
    loadRecommendations()
  }

  fun onKeyInput(input: String) {
    mutableUiState.update { state ->
      val at = state.cursorPosition.coerceIn(0, state.query.length)
      state.copy(
        query = state.query.substring(0, at) + input + state.query.substring(at),
        cursorPosition = at + input.length,
        errorMessage = null,
      )
    }
    refreshSuggestions()
  }

  fun onBackspace() {
    mutableUiState.update { state ->
      val at = state.cursorPosition.coerceIn(0, state.query.length)
      if (at == 0) {
        state
      } else {
        state.copy(
          query = state.query.removeRange(at - 1, at),
          cursorPosition = at - 1,
          errorMessage = null,
        )
      }
    }
    refreshSuggestions()
  }

  fun onClearInput() {
    mutableUiState.update {
      it.copy(
        query = "",
        cursorPosition = 0,
        errorMessage = null,
      )
    }
    refreshSuggestions()
  }

  fun onCursorLeft() {
    mutableUiState.update { state ->
      state.copy(cursorPosition = (state.cursorPosition - 1).coerceAtLeast(0))
    }
  }

  fun onCursorRight() {
    mutableUiState.update { state ->
      state.copy(cursorPosition = (state.cursorPosition + 1).coerceAtMost(state.query.length))
    }
  }

  fun onSuggestionClick(suggestion: String) {
    mutableUiState.update {
      it.copy(
        query = suggestion,
        cursorPosition = suggestion.length,
        suggestions = emptyList(),
      )
    }
    submitSearch()
  }

  fun showKeyboard() {
    mutableUiState.update { it.copy(isKeyboardVisible = true) }
    refreshSuggestions()
  }

  fun hideKeyboard() {
    mutableUiState.update { it.copy(isKeyboardVisible = false) }
  }

  fun submitSearch() {
    val query = uiState.value.query.trim()
    if (query.isBlank() || uiState.value.isSearching) return

    suggestionJob?.cancel()
    loadJob?.cancel()
    mutableUiState.update {
      it.copy(
        query = query,
        cursorPosition = query.length,
        isKeyboardVisible = false,
        isSearching = true,
        errorMessage = null,
      )
    }
    loadJob = viewModelScope.launch {
      runCatching { repository.search(query) }
        .onSuccess { sections ->
          mutableUiState.update {
            it.copy(
              isLoading = false,
              isSearching = false,
              submittedQuery = query,
              sections = uiMapper.map(sections),
              suggestions = emptyList(),
            )
          }
        }
        .onFailure { error ->
          mutableUiState.update {
            it.copy(
              isLoading = false,
              isSearching = false,
              errorMessage = error.message ?: "Search is temporarily unavailable",
            )
          }
        }
    }
  }

  private fun loadRecommendations() {
    loadJob?.cancel()
    loadJob = viewModelScope.launch {
      runCatching {
        repository.getRecommendations() to repository.getSuggestions("")
      }.onSuccess { (sections, suggestions) ->
        mutableUiState.update {
          it.copy(
            isLoading = false,
            sections = uiMapper.map(sections),
            suggestions = suggestions,
            errorMessage = null,
          )
        }
      }.onFailure { error ->
        mutableUiState.update {
          it.copy(
            isLoading = false,
            errorMessage = error.message ?: "Search is temporarily unavailable",
          )
        }
      }
    }
  }

  private fun refreshSuggestions() {
    suggestionJob?.cancel()
    val query = uiState.value.query
    suggestionJob = viewModelScope.launch {
      runCatching { repository.getSuggestions(query) }
        .onSuccess { suggestions ->
          mutableUiState.update { it.copy(suggestions = suggestions) }
        }
    }
  }
}
