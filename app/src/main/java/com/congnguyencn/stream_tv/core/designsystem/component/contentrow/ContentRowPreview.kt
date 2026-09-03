package com.congnguyencn.stream_tv.core.designsystem.component.contentrow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvSurface
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF010810)
@Composable
private fun ContentRowPreview() {
  val focusRequester = remember { FocusRequester() }

  StreamTvTheme {
    StreamTvSurface {
      ContentRow(
        modifier = Modifier.width(900.dp),
        startEdgeFocusRequester = FocusRequester.Cancel,
        selectedItemModifier = Modifier.focusRequester(focusRequester),
      ) {
        items(count = 5) { index ->
          Box(
            modifier = Modifier
              .width(220.dp)
              .height(124.dp)
              .background(
                color = if (index % 2 == 0) StreamTvColors.Primary100 else StreamTvColors.Neutral80,
                shape = RoundedCornerShape(8.dp),
              ),
          )
        }
      }
    }
  }

  LaunchedEffect(Unit) { focusRequester.requestFocus() }
}
