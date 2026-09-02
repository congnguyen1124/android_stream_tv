package com.congnguyencn.stream_tv.core.designsystem.component.lazyfocusedstack

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors

object LazyFocusedStackDefaults {
  val ColumnWidth: Dp = 214.dp
  val ColumnSpacing: Dp = 8.dp
  val HeaderHeight: Dp = 64.dp
  val TimeRulerWidth: Dp = 76.dp
  val HourHeight: Dp = 76.dp
  val ItemVerticalSpacing: Dp = 3.dp
  val SelectedItemPadding: Dp = 2.dp
  val SelectedItemShape: Shape = RoundedCornerShape(7.dp)

  internal const val ScrollDurationMillis = 190
  internal const val BeyondBoundsColumnCount = 1
  internal const val BeyondBoundsMinuteCount = 120

  @Composable
  fun SelectedItem(
    isFocused: Boolean,
    modifier: Modifier = Modifier,
    shape: Shape = SelectedItemShape,
  ) {
    val borderColor by animateColorAsState(
      targetValue = if (isFocused) StreamTvColors.NeutralWhite else StreamTvColors.TransparentWhite20,
      label = "LazyFocusedStackSelectedItemBorder",
    )

    Box(
      modifier = modifier
        .fillMaxSize()
        .border(
          width = if (isFocused) 3.dp else 1.dp,
          color = borderColor,
          shape = shape,
        ),
    )
  }
}
