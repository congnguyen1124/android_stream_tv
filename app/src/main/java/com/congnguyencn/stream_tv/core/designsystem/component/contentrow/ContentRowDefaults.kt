package com.congnguyencn.stream_tv.core.designsystem.component.contentrow

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors

object ContentRowDefaults {
  val ItemSpacing: Dp = 18.dp
  val ContentPadding: PaddingValues = PaddingValues(horizontal = 48.dp)
  val SelectedItemContentPadding: Dp = 2.dp
  val SelectedItemShape: Shape = RoundedCornerShape(8.dp)

  internal const val ScrollDurationMillis = 190
  internal const val LoopingItemCountThreshold = 5

  /** The fixed, transparent focus target drawn above the moving lazy items. */
  @Composable
  fun SelectedItem(isFocused: Boolean, modifier: Modifier = Modifier, shape: Shape = SelectedItemShape) {
    val borderColor by animateColorAsState(
      targetValue = if (isFocused) {
        StreamTvColors.NeutralWhite
      } else {
        StreamTvColors.TransparentWhite20
      },
      label = "ContentRowSelectedItemBorder",
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
