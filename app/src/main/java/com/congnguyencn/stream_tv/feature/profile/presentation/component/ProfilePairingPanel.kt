package com.congnguyencn.stream_tv.feature.profile.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvQrCode
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme

/**
 * The pairing half of Profile: how to sign in from the mobile app, by QR symbol or typed code.
 *
 * Nothing here is focusable. The panel is a reading surface, so it must fit the viewport at its
 * declared size rather than rely on scrolling.
 */
@Composable
internal fun ProfilePairingPanel(
  pairingUrl: String,
  pairingCode: String,
  pairingValidUntilLabel: String,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .width(ProfileUiDefaults.PanelWidth)
      .clip(ProfileUiDefaults.PanelShape)
      .background(
        Brush.verticalGradient(
          listOf(
            StreamTvColors.Primary100.copy(alpha = ProfileUiDefaults.PanelGradientEndAlpha),
            StreamTvColors.TransparentWhite5,
          ),
        ),
      )
      .border(
        width = ProfileUiDefaults.PanelBorderWidth,
        color = StreamTvColors.TransparentWhite10,
        shape = ProfileUiDefaults.PanelShape,
      )
      .padding(
        horizontal = ProfileUiDefaults.PanelHorizontalPadding,
        vertical = ProfileUiDefaults.PanelVerticalPadding,
      )
      .testTag("profile-pairing-panel"),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = stringResource(R.string.profile_pairing_title),
      color = StreamTvColors.NeutralWhite,
      textAlign = TextAlign.Center,
      style = StreamTvTheme.typography.titleLarge,
    )

    Spacer(modifier = Modifier.height(ProfileUiDefaults.PanelTitleToStepsSpacing))

    ProfilePairingStep(
      number = 1,
      text = stringResource(R.string.profile_pairing_step_account),
      illustration = { ProfileMobileAppArt() },
      modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(ProfileUiDefaults.PanelStepSpacing))

    ProfilePairingStep(
      number = 2,
      text = stringResource(R.string.profile_pairing_step_scan),
      modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(ProfileUiDefaults.PanelStepsToQrSpacing))

    StreamTvQrCode(
      content = pairingUrl,
      contentDescription = stringResource(R.string.profile_pairing_qr_description),
      modifier = Modifier
        .size(ProfileUiDefaults.PanelQrSize)
        .testTag("profile-pairing-qr"),
    )

    Spacer(modifier = Modifier.height(ProfileUiDefaults.PanelQrToDividerSpacing))

    ProfilePairingDivider(modifier = Modifier.fillMaxWidth())

    Spacer(modifier = Modifier.height(ProfileUiDefaults.PanelDividerToCodeSpacing))

    Text(
      text = stringResource(R.string.profile_pairing_code_label),
      color = StreamTvColors.Neutral20,
      style = StreamTvTheme.typography.labelMedium,
    )

    Spacer(modifier = Modifier.height(ProfileUiDefaults.PanelCodeLabelSpacing))

    Text(
      text = pairingCode,
      color = StreamTvColors.NeutralWhite,
      style = StreamTvTheme.typography.headlineLarge.copy(
        fontSize = ProfileUiDefaults.CodeFontSize,
        letterSpacing = ProfileUiDefaults.CodeLetterSpacing,
      ),
      modifier = Modifier.testTag("profile-pairing-code"),
    )

    Spacer(modifier = Modifier.height(ProfileUiDefaults.PanelCodeToValiditySpacing))

    Text(
      text = stringResource(R.string.profile_pairing_valid_until, pairingValidUntilLabel),
      color = StreamTvColors.Neutral30,
      style = StreamTvTheme.typography.labelMedium.copy(
        fontSize = ProfileUiDefaults.ValidityFontSize,
      ),
    )
  }
}

@Composable
private fun ProfilePairingDivider(modifier: Modifier = Modifier) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(ProfileUiDefaults.DividerLabelSpacing),
  ) {
    ProfilePairingDividerRule(modifier = Modifier.weight(1f))
    Text(
      text = stringResource(R.string.profile_pairing_divider),
      color = StreamTvColors.Neutral30,
      style = StreamTvTheme.typography.labelMedium.copy(
        fontSize = ProfileUiDefaults.DividerLabelFontSize,
        letterSpacing = ProfileUiDefaults.DividerLabelLetterSpacing,
      ),
    )
    ProfilePairingDividerRule(modifier = Modifier.weight(1f))
  }
}

@Composable
private fun ProfilePairingDividerRule(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .height(ProfileUiDefaults.DividerThickness)
      .background(StreamTvColors.Neutral80),
  )
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF010810)
@Composable
private fun ProfilePairingPanelPreview() {
  StreamTvTheme {
    ProfilePairingPanel(
      pairingUrl = "https://tv.streamtv.example.com/pair?code=XHSZ-QBKX",
      pairingCode = "XHSZ-QBKX",
      pairingValidUntilLabel = "14:17",
    )
  }
}
