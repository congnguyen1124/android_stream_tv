package com.congnguyencn.stream_tv.feature.search.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme

/** Compact suggestion and keyboard workspace that keeps the first result row in view. */
@Composable
internal fun SearchWorkspace(
  query: String,
  cursorPosition: Int,
  showCaret: Boolean,
  suggestions: List<String>,
  keyboardVisibilityState: MutableTransitionState<Boolean>,
  contentFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
  firstKeyboardKeyFocusRequester: FocusRequester,
  searchKeyFocusRequester: FocusRequester,
  onKey: (String) -> Unit,
  onBackspace: () -> Unit,
  onClear: () -> Unit,
  onCursorLeft: () -> Unit,
  onCursorRight: () -> Unit,
  onSearch: () -> Unit,
  onSuggestionClick: (String) -> Unit,
  onMoveToResults: () -> Boolean,
  modifier: Modifier = Modifier,
) {
  val keyboardEntryRequester = if (suggestions.isEmpty()) {
    contentFocusRequester
  } else {
    firstKeyboardKeyFocusRequester
  }

  Row(
    modifier = modifier
      .fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(SearchUiDefaults.SearchWorkspaceGap),
  ) {
    Column(modifier = Modifier.width(SearchUiDefaults.SearchFieldWidth)) {
      SearchQuerySurface(
        query = query,
        cursorPosition = cursorPosition,
        showCaret = showCaret,
      )

      AnimatedVisibility(
        visible = keyboardVisibilityState.targetState,
        enter = searchWorkspaceEnterTransition(),
        exit = searchWorkspaceExitTransition(),
      ) {
        SearchSuggestions(
          suggestions = suggestions,
          firstFocusRequester = contentFocusRequester,
          topBarFocusRequester = topBarFocusRequester,
          keyboardFocusRequester = firstKeyboardKeyFocusRequester,
          onSuggestionClick = onSuggestionClick,
          onMoveToResults = onMoveToResults,
          modifier = Modifier
            .padding(top = SearchUiDefaults.SearchWorkspaceTopPadding)
            .width(SearchUiDefaults.SuggestionColumnWidth),
        )
      }
    }

    AnimatedVisibility(
      visibleState = keyboardVisibilityState,
      enter = searchWorkspaceEnterTransition(),
      exit = searchWorkspaceExitTransition(),
    ) {
      SearchVirtualKeyboard(
        onKey = onKey,
        onBackspace = onBackspace,
        onClear = onClear,
        onCursorLeft = onCursorLeft,
        onCursorRight = onCursorRight,
        onSearch = onSearch,
        firstKeyFocusRequester = keyboardEntryRequester,
        searchKeyFocusRequester = searchKeyFocusRequester,
        topBarFocusRequester = topBarFocusRequester,
        leftExitFocusRequester = if (suggestions.isEmpty()) FocusRequester.Default else contentFocusRequester,
        onMoveToResults = onMoveToResults,
        modifier = Modifier
          .width(SearchUiDefaults.KeyboardWidth)
          .height(SearchUiDefaults.SearchWorkspaceHeight),
      )
    }
  }
}

private fun searchWorkspaceEnterTransition() =
  fadeIn(tween(SearchUiDefaults.KeyboardAnimationDurationMillis)) +
    expandVertically(
      animationSpec = tween(SearchUiDefaults.KeyboardAnimationDurationMillis),
      expandFrom = Alignment.Top,
    )

private fun searchWorkspaceExitTransition() =
  fadeOut(tween(SearchUiDefaults.KeyboardAnimationDurationMillis)) +
    shrinkVertically(
      animationSpec = tween(SearchUiDefaults.KeyboardAnimationDurationMillis),
      shrinkTowards = Alignment.Top,
    )

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF010810)
@Composable
private fun SearchWorkspacePreview() {
  StreamTvTheme {
    SearchWorkspace(
      query = "Japanese culture",
      cursorPosition = 8,
      showCaret = true,
      suggestions = listOf(
        "Wildlife documentaries",
        "Live sports",
        "Japanese culture",
        "Chinese festivals",
        "Football highlights",
        "Basketball stories",
      ),
      keyboardVisibilityState = remember { MutableTransitionState(true) },
      contentFocusRequester = remember { FocusRequester() },
      topBarFocusRequester = remember { FocusRequester() },
      firstKeyboardKeyFocusRequester = remember { FocusRequester() },
      searchKeyFocusRequester = remember { FocusRequester() },
      onKey = {},
      onBackspace = {},
      onClear = {},
      onCursorLeft = {},
      onCursorRight = {},
      onSearch = {},
      onSuggestionClick = {},
      onMoveToResults = { true },
      modifier = Modifier
        .background(StreamTvColors.NeutralBlack)
        .padding(horizontal = 24.dp),
    )
  }
}
