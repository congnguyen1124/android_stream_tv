package com.congnguyencn.stream_tv.feature.profile.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme

/**
 * A miniature of the mobile app's bottom navigation, with Account ringed.
 *
 * It shows the viewer where to tap on their phone without shipping a screenshot that would go stale
 * with the next mobile release, so it is drawn from the same icons the shell already uses.
 */
@Composable
internal fun ProfileMobileAppArt(modifier: Modifier = Modifier) {
  Row(
    modifier = modifier
      .height(ProfileUiDefaults.AppArtHeight)
      .clip(ProfileUiDefaults.AppArtShape)
      .background(StreamTvColors.Neutral100)
      .padding(horizontal = ProfileUiDefaults.AppArtHorizontalPadding),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(ProfileUiDefaults.AppArtItemSpacing),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(ProfileUiDefaults.AppArtHomeLabelSpacing),
    ) {
      Icon(
        painter = painterResource(R.drawable.ic_home),
        contentDescription = null,
        tint = StreamTvColors.Primary60,
        modifier = Modifier.size(ProfileUiDefaults.AppArtIconSize),
      )
      Text(
        text = stringResource(R.string.profile_mobile_app_home_label),
        color = StreamTvColors.Neutral10,
        style = StreamTvTheme.typography.labelMedium.copy(
          fontSize = ProfileUiDefaults.AppArtHomeLabelFontSize,
        ),
      )
    }

    ProfileMobileAppArtIcons.forEach { iconResId ->
      Icon(
        painter = painterResource(iconResId),
        contentDescription = null,
        tint = StreamTvColors.Neutral50,
        modifier = Modifier.size(ProfileUiDefaults.AppArtIconSize),
      )
    }

    Box(
      modifier = Modifier
        .size(ProfileUiDefaults.AppArtProfileRingSize)
        .border(
          width = ProfileUiDefaults.AppArtProfileRingWidth,
          color = StreamTvColors.Primary60,
          shape = CircleShape,
        ),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        painter = painterResource(R.drawable.ic_profile),
        contentDescription = null,
        tint = StreamTvColors.NeutralWhite,
        modifier = Modifier.size(ProfileUiDefaults.AppArtProfileIconSize),
      )
    }
  }
}

private val ProfileMobileAppArtIcons = listOf(
  R.drawable.ic_search,
  R.drawable.ic_live_tv,
  R.drawable.ic_bookmark_outline,
)

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF010810)
@Composable
private fun ProfileMobileAppArtPreview() {
  StreamTvTheme {
    ProfileMobileAppArt()
  }
}
