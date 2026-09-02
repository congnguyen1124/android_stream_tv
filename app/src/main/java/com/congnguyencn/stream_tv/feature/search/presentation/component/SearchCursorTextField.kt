package com.congnguyencn.stream_tv.feature.search.presentation.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme

/** Read-only query rendering driven entirely by [SearchVirtualKeyboard]. */
@Composable
internal fun SearchCursorTextField(
  text: String,
  cursorPosition: Int,
  modifier: Modifier = Modifier,
  placeholder: String = "Search movies, series, channels and shorts",
  showCaret: Boolean = true,
) {
  val transition = rememberInfiniteTransition(label = "SearchCaret")
  val animatedCaretAlpha by transition.animateFloat(
    initialValue = 1f,
    targetValue = 0.18f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 620, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "SearchCaretAlpha",
  )
  val scrollState = rememberScrollState()
  val safeCursor = cursorPosition.coerceIn(0, text.length)

  LaunchedEffect(text, safeCursor) {
    if (safeCursor >= text.length) {
      scrollState.scrollTo(scrollState.maxValue)
    }
  }

  Row(
    modifier = modifier.padding(horizontal = 18.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = Icons.Default.Search,
      contentDescription = null,
      modifier = Modifier.size(22.dp),
      tint = StreamTvColors.Neutral20,
    )
    Spacer(modifier = Modifier.width(12.dp))
    Box(modifier = Modifier.weight(1f)) {
      if (text.isBlank()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (showCaret) SearchCaret(alpha = animatedCaretAlpha)
          Text(
            text = placeholder,
            color = StreamTvColors.Neutral40,
            style = StreamTvTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      } else {
        Row(
          modifier = Modifier.horizontalScroll(scrollState),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = text.substring(0, safeCursor),
            color = StreamTvColors.NeutralWhite,
            style = StreamTvTheme.typography.bodyLarge,
            maxLines = 1,
            softWrap = false,
          )
          if (showCaret) SearchCaret(alpha = animatedCaretAlpha)
          Text(
            text = text.substring(safeCursor),
            color = StreamTvColors.NeutralWhite,
            style = StreamTvTheme.typography.bodyLarge,
            maxLines = 1,
            softWrap = false,
          )
        }
      }
    }
  }
}

@Composable
private fun SearchCaret(alpha: Float) {
  Box(
    modifier = Modifier
      .padding(horizontal = 1.dp)
      .width(2.dp)
      .height(22.dp)
      .background(StreamTvColors.Primary40.copy(alpha = alpha)),
  )
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF010810)
@Composable
private fun SearchCursorTextFieldPreview() {
  StreamTvTheme {
    SearchCursorTextField(
      text = "Japanese culture",
      cursorPosition = 8,
      modifier = Modifier
        .width(720.dp)
        .height(54.dp)
        .background(Color(0xFF23272E)),
    )
  }
}
