package com.congnguyencn.stream_tv.feature.search.presentation.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme

@Composable
internal fun SearchSuggestions(
  suggestions: List<String>,
  firstFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
  keyboardFocusRequester: FocusRequester,
  onSuggestionClick: (String) -> Unit,
  onMoveToResults: () -> Boolean,
  modifier: Modifier = Modifier,
) {
  val visibleSuggestions = suggestions.take(SearchUiDefaults.MaxSuggestionCount)

  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(SearchUiDefaults.SuggestionSpacing),
  ) {
    visibleSuggestions.forEachIndexed { index, suggestion ->
      SearchSuggestionItem(
        suggestion = suggestion,
        onClick = { onSuggestionClick(suggestion) },
        modifier = Modifier
          .fillMaxWidth()
          .height(SearchUiDefaults.SuggestionHeight)
          .then(if (index == 0) Modifier.focusRequester(firstFocusRequester) else Modifier)
          .focusProperties {
            if (index == 0) up = topBarFocusRequester
            right = keyboardFocusRequester
          }
          .then(
            if (index == visibleSuggestions.lastIndex) {
              Modifier.moveFocusOnDpad(
                key = Key.DirectionDown,
                onMove = onMoveToResults,
              )
            } else {
              Modifier
            },
          )
          .testTag("search-suggestion-$index"),
      )
    }
  }
}

@Composable
private fun SearchSuggestionItem(suggestion: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
  var isFocused by remember { mutableStateOf(false) }
  val indicatorColor by animateColorAsState(
    targetValue = if (isFocused) StreamTvColors.Primary40 else StreamTvColors.Transparent,
    label = "SearchSuggestionIndicator",
  )
  val iconBackgroundColor by animateColorAsState(
    targetValue = if (isFocused) StreamTvColors.Primary30 else StreamTvColors.TransparentWhite5,
    label = "SearchSuggestionIconBackground",
  )

  Surface(
    onClick = onClick,
    modifier = modifier.onFocusChanged { isFocused = it.isFocused },
    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
    scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
    colors = ClickableSurfaceDefaults.colors(
      containerColor = StreamTvColors.Transparent,
      contentColor = StreamTvColors.Neutral30,
      focusedContainerColor = StreamTvColors.Transparent,
      focusedContentColor = StreamTvColors.NeutralWhite,
      pressedContainerColor = StreamTvColors.TransparentWhite5,
      pressedContentColor = StreamTvColors.NeutralWhite,
    ),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(end = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier
          .width(2.dp)
          .fillMaxHeight()
          .background(indicatorColor, RoundedCornerShape(percent = 50)),
      )
      Spacer(modifier = Modifier.width(8.dp))
      Box(
        modifier = Modifier
          .size(22.dp)
          .background(iconBackgroundColor, CircleShape),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.Refresh,
          contentDescription = null,
          modifier = Modifier.size(15.dp),
          tint = if (isFocused) StreamTvColors.NeutralBlack else StreamTvColors.Neutral30,
        )
      }
      Spacer(modifier = Modifier.width(9.dp))
      Text(
        text = suggestion,
        style = StreamTvTheme.typography.labelMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

private fun Modifier.moveFocusOnDpad(key: Key, onMove: () -> Boolean): Modifier = onPreviewKeyEvent { event ->
  if (event.key != key) return@onPreviewKeyEvent false
  if (event.type == KeyEventType.KeyDown) onMove()
  true
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF010810)
@Composable
private fun SearchSuggestionsPreview() {
  val firstFocusRequester = remember { FocusRequester() }

  StreamTvTheme {
    SearchSuggestions(
      suggestions = listOf(
        "Wildlife documentaries",
        "Live sports",
        "Japanese culture",
        "Chinese festivals",
      ),
      firstFocusRequester = firstFocusRequester,
      topBarFocusRequester = remember { FocusRequester() },
      keyboardFocusRequester = remember { FocusRequester() },
      onSuggestionClick = {},
      onMoveToResults = { true },
      modifier = Modifier
        .width(SearchUiDefaults.SuggestionColumnWidth)
        .padding(8.dp),
    )
  }

  LaunchedEffect(Unit) { firstFocusRequester.requestFocus() }
}
