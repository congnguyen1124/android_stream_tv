package com.congnguyencn.stream_tv.feature.home.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/**
 * Which Home section owns focus.
 *
 * Saveable because Home is rebuilt from scratch whenever it is left: the players are siblings of the
 * browsing shell in the outer graph, so opening one disposes Home's whole composition and returning
 * composes a new one. Without this, every return re-armed the opening section — the banner — and the
 * row the viewer had been on lost focus even though its own selected item was restored.
 */
@Stable
internal class HomeFocusState internal constructor(initialFocusedSectionIndex: Int) {
  var focusedSectionIndex by mutableIntStateOf(initialFocusedSectionIndex.coerceAtLeast(0))
    private set

  fun focusSection(index: Int) {
    if (index < 0 || index == focusedSectionIndex) return
    focusedSectionIndex = index
  }

  /**
   * Clamps the remembered index into a section list of [sectionCount].
   *
   * The catalogue can come back shorter than it was when focus was recorded, and an index past its
   * end would restore focus to nothing at all.
   */
  internal fun updateSectionCount(sectionCount: Int) {
    require(sectionCount >= 0) { "Home section count must be non-negative" }
    if (sectionCount == 0) return

    val lastIndex = sectionCount - 1
    if (focusedSectionIndex > lastIndex) {
      focusedSectionIndex = lastIndex
    }
  }

  companion object {
    val Saver: Saver<HomeFocusState, Int> = Saver(
      save = { state -> state.focusedSectionIndex },
      restore = ::HomeFocusState,
    )
  }
}

@Composable
internal fun rememberHomeFocusState(initialFocusedSectionIndex: Int = 0): HomeFocusState =
  rememberSaveable(saver = HomeFocusState.Saver) {
    HomeFocusState(initialFocusedSectionIndex)
  }
