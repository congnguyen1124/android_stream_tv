package com.congnguyencn.stream_tv.core.designsystem.tokens

import androidx.compose.ui.unit.dp

object StreamTvDimensions {
  val AppBarHeight = 82.dp
  val TopBarHeight = 80.dp
  val TopBarGradientHeight = 80.dp

  /**
   * Deliberately taller than the bar itself: the scrim has to fade out well below the last row of
   * items, otherwise the gradient ends on a visible edge instead of dissolving into the content.
   */
  val TopBarOverlayHeight = 168.dp
  val ScreenHorizontalPadding = 48.dp
  val NavigationItemSpacing = 4.dp
}
