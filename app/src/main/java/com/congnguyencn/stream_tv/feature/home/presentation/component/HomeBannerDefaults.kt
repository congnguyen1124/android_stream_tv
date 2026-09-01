package com.congnguyencn.stream_tv.feature.home.presentation.component

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp

internal object HomeBannerDefaults {
  const val EdgeItemCount = 2
  const val AutoScrollDurationMillis = 5_000L
  const val VerticalGradientWidthRatio = 0.68f
  const val HorizontalGradientHeightRatio = 0.58f

  /**
   * How much of the section below the banner stays on screen while the banner holds focus.
   *
   * A full-bleed hero gives the viewer no hint that anything follows it, so the banner gives up
   * enough room for the next section's header plus the top of its first card. Roughly 70.dp of this
   * is spent on section spacing and the 20.sp header; the remainder is card.
   */
  val NextSectionPeekHeight = 124.dp

  /** Floor: below this the info block — featured badge through dots — stops fitting. */
  val MinHeight = 320.dp

  /** Ceiling: the height the banner artwork was framed at, for viewports tall enough to grant it. */
  val MaxHeight = 600.dp
}

/**
 * The banner height that leaves [HomeBannerDefaults.NextSectionPeekHeight] of the next section
 * visible on a [viewportHeight]-tall screen.
 *
 * Derived rather than fixed because the peek is the point: a hard-coded height silently swallows the
 * next section on any viewport shorter than the one it was measured against, and leaves a gap on a
 * taller one.
 */
internal fun homeBannerHeight(viewportHeight: Dp): Dp =
  (viewportHeight - HomeBannerDefaults.NextSectionPeekHeight).coerceIn(
    minimumValue = HomeBannerDefaults.MinHeight,
    maximumValue = HomeBannerDefaults.MaxHeight,
  )
