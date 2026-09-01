package com.congnguyencn.stream_tv.feature.home.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.home.presentation.model.VideoUiItem

private object HomeBannerInfoDefaults {
  @Stable
  val BlockWidth: Dp = 470.dp

  @Stable
  val DescriptionWidth: Dp = 440.dp

  @Stable
  val BadgeShape: Shape = RoundedCornerShape(4.dp)

  @Stable
  val PlayButtonShape: Shape = RoundedCornerShape(8.dp)

  @Stable
  val PlayIconSize: Dp = 20.dp

  const val TitleMaxLines = 2
  const val DescriptionMaxLines = 2
}

/**
 * The banner's text block: featured label, age rating, title, synopsis, play affordance and dots.
 *
 * @param isFocused Whether the banner carousel holds focus. The play affordance is drawn, not
 *   focusable — the carousel itself is the single focus target, so this is how the block reflects it.
 */
@Composable
internal fun HomeBannerInfo(
  item: VideoUiItem,
  isFocused: Boolean,
  itemCount: Int,
  activeIndex: Int,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.width(HomeBannerInfoDefaults.BlockWidth),
    horizontalAlignment = Alignment.Start,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = stringResource(R.string.home_banner_featured_label),
        modifier = Modifier
          .background(StreamTvColors.TransparentBlack60, HomeBannerInfoDefaults.BadgeShape)
          .padding(horizontal = 8.dp, vertical = 4.dp),
        color = StreamTvColors.Primary30,
        style = StreamTvTheme.typography.labelMedium,
      )
      item.ageRestriction?.takeIf(String::isNotBlank)?.let { ageRestriction ->
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = ageRestriction,
          modifier = Modifier
            .border(1.dp, StreamTvColors.TransparentWhite40, HomeBannerInfoDefaults.BadgeShape)
            .padding(horizontal = 7.dp, vertical = 3.dp),
          color = StreamTvColors.Neutral10,
          style = StreamTvTheme.typography.labelMedium,
        )
      }
    }
    Spacer(modifier = Modifier.height(12.dp))
    Text(
      text = item.title,
      color = StreamTvColors.NeutralWhite,
      style = StreamTvTheme.typography.headlineLarge,
      maxLines = HomeBannerInfoDefaults.TitleMaxLines,
      overflow = TextOverflow.Ellipsis,
    )
    item.description.takeIf(String::isNotBlank)?.let { description ->
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = description,
        modifier = Modifier.width(HomeBannerInfoDefaults.DescriptionWidth),
        color = StreamTvColors.Neutral10,
        style = StreamTvTheme.typography.bodyLarge,
        maxLines = HomeBannerInfoDefaults.DescriptionMaxLines,
        overflow = TextOverflow.Ellipsis,
      )
    }
    Spacer(modifier = Modifier.height(18.dp))
    HomeBannerPlayButton(isFocused = isFocused)
    Spacer(modifier = Modifier.height(18.dp))
    HomeBannerDotsIndicator(
      count = itemCount,
      activeIndex = activeIndex,
    )
  }
}

@Composable
private fun HomeBannerPlayButton(isFocused: Boolean, modifier: Modifier = Modifier) {
  val contentColor = if (isFocused) StreamTvColors.Neutral80 else StreamTvColors.Neutral10

  Row(
    modifier = modifier
      .background(
        color = if (isFocused) StreamTvColors.NeutralWhite else StreamTvColors.TransparentWhite10,
        shape = HomeBannerInfoDefaults.PlayButtonShape,
      )
      .padding(horizontal = 20.dp, vertical = 11.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = ImageVector.vectorResource(R.drawable.ic_play),
      contentDescription = null,
      modifier = Modifier.size(HomeBannerInfoDefaults.PlayIconSize),
      tint = contentColor,
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = stringResource(R.string.play),
      color = contentColor,
      style = StreamTvTheme.typography.labelMedium,
    )
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF0B0B0F)
@Composable
private fun HomeBannerInfoFocusedPreview() {
  StreamTvTheme {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
        .padding(48.dp),
      contentAlignment = Alignment.BottomStart,
    ) {
      HomeBannerInfo(
        item = HomePreviewData.Videos.first(),
        isFocused = true,
        itemCount = HomePreviewData.Videos.size,
        activeIndex = 0,
      )
    }
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF0B0B0F)
@Composable
private fun HomeBannerInfoUnfocusedPreview() {
  StreamTvTheme {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
        .padding(48.dp),
      contentAlignment = Alignment.BottomStart,
    ) {
      HomeBannerInfo(
        item = HomePreviewData.Videos[2],
        isFocused = false,
        itemCount = HomePreviewData.Videos.size,
        activeIndex = 2,
      )
    }
  }
}
