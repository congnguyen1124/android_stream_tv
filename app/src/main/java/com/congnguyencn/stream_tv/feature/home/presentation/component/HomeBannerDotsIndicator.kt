package com.congnguyencn.stream_tv.feature.home.presentation.component

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme

private object HomeBannerDotsDefaults {
  @Stable
  val ActiveWidth: Dp = 24.dp

  @Stable
  val InactiveWidth: Dp = 12.dp

  @Stable
  val Height: Dp = 4.dp

  @Stable
  val Spacing: Dp = 6.dp

  @Stable
  val Shape: Shape = RoundedCornerShape(2.dp)

  const val TransitionDurationMillis = 260
}

/**
 * The banner's page indicator: one pill per item, the active one twice as wide.
 *
 * @param count Number of real banner items — the duplicated looping edges are not represented.
 * @param activeIndex Index of the item currently shown, in `0 until count`.
 */
@Composable
internal fun HomeBannerDotsIndicator(count: Int, activeIndex: Int, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(HomeBannerDotsDefaults.Spacing),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    repeat(count) { index ->
      HomeBannerDot(isActive = index == activeIndex)
    }
  }
}

/**
 * One pill, animating between its two sizes.
 *
 * Both dots involved in a page change animate over the same window, so the outgoing pill is seen
 * shrinking while the incoming one grows rather than the pair swapping widths on a single frame.
 */
@Composable
private fun HomeBannerDot(isActive: Boolean, modifier: Modifier = Modifier) {
  val transition = updateTransition(targetState = isActive, label = "HomeBannerDot")
  val width by transition.animateDp(
    transitionSpec = { homeBannerDotAnimationSpec() },
    label = "HomeBannerDotWidth",
  ) { active ->
    if (active) HomeBannerDotsDefaults.ActiveWidth else HomeBannerDotsDefaults.InactiveWidth
  }
  val color by transition.animateColor(
    transitionSpec = { homeBannerDotAnimationSpec() },
    label = "HomeBannerDotColor",
  ) { active ->
    if (active) StreamTvColors.NeutralWhite else StreamTvColors.TransparentWhite40
  }

  Box(
    modifier = modifier
      .width(width)
      .height(HomeBannerDotsDefaults.Height)
      .background(color = color, shape = HomeBannerDotsDefaults.Shape),
  )
}

private fun <T> homeBannerDotAnimationSpec(): FiniteAnimationSpec<T> =
  tween(durationMillis = HomeBannerDotsDefaults.TransitionDurationMillis)

@Preview(device = Devices.TV_720p, showBackground = true, backgroundColor = 0xFF0B0B0F)
@Composable
private fun HomeBannerDotsIndicatorPreview() {
  StreamTvTheme {
    Box(
      modifier = Modifier
        .background(Color.Black)
        .padding(24.dp),
    ) {
      HomeBannerDotsIndicator(count = 5, activeIndex = 2)
    }
  }
}

@Preview(device = Devices.TV_720p, showBackground = true, backgroundColor = 0xFF0B0B0F)
@Composable
private fun HomeBannerDotsIndicatorFirstPagePreview() {
  StreamTvTheme {
    Box(
      modifier = Modifier
        .background(Color.Black)
        .padding(24.dp),
    ) {
      HomeBannerDotsIndicator(count = 5, activeIndex = 0)
    }
  }
}
