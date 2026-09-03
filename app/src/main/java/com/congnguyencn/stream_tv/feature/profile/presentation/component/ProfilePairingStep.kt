package com.congnguyencn.stream_tv.feature.profile.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme

/** One numbered instruction inside the pairing panel, with optional illustration beneath it. */
@Composable
internal fun ProfilePairingStep(
  number: Int,
  text: String,
  modifier: Modifier = Modifier,
  illustration: @Composable (() -> Unit)? = null,
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(ProfileUiDefaults.StepBadgeToTextSpacing),
  ) {
    Box(
      modifier = Modifier
        .size(ProfileUiDefaults.StepBadgeSize)
        .background(color = StreamTvColors.Primary90, shape = CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = number.toString(),
        color = StreamTvColors.Primary20,
        style = StreamTvTheme.typography.labelMedium.copy(
          fontSize = ProfileUiDefaults.StepBadgeFontSize,
        ),
      )
    }

    Column(
      verticalArrangement = Arrangement.spacedBy(ProfileUiDefaults.StepTextToArtSpacing),
    ) {
      Text(
        text = text,
        color = StreamTvColors.Neutral10,
        maxLines = ProfileUiDefaults.StepTextLineCount,
        overflow = TextOverflow.Ellipsis,
        style = StreamTvTheme.typography.labelMedium.copy(
          fontSize = ProfileUiDefaults.StepFontSize,
        ),
      )
      illustration?.invoke()
    }
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF010810)
@Composable
private fun ProfilePairingStepPreview() {
  StreamTvTheme {
    ProfilePairingStep(
      number = 1,
      text = "Open “Account” in the StreamTV mobile app",
      illustration = { ProfileMobileAppArt() },
    )
  }
}
