package com.congnguyencn.stream_tv.feature.search.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.focused
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme

/** Display-only query field. Query editing is owned by [SearchVirtualKeyboard]. */
@Composable
internal fun SearchQuerySurface(
  query: String,
  cursorPosition: Int,
  showCaret: Boolean,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .width(SearchUiDefaults.SearchFieldWidth)
      .height(SearchUiDefaults.SearchFieldHeight)
      .background(StreamTvColors.Neutral90, RoundedCornerShape(percent = 50))
      .semantics { focused = false }
      .testTag("search-query"),
  ) {
    SearchCursorTextField(
      text = query,
      cursorPosition = cursorPosition,
      showCaret = showCaret,
      modifier = Modifier.fillMaxSize(),
    )
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF010810)
@Composable
private fun SearchQuerySurfacePreview() {
  StreamTvTheme {
    SearchQuerySurface(
      query = "Japanese culture",
      cursorPosition = 8,
      showCaret = true,
    )
  }
}
