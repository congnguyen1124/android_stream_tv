package com.congnguyencn.stream_tv.feature.profile.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvButton
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme

/**
 * The identity half of Profile: brand mark, invitation, and the only focusable control on the
 * screen.
 *
 * Deliberately does not request focus on appearance. Profile is reachable only from the top bar,
 * which keeps focus on its own item until the viewer presses Down — that key lands on
 * [contentFocusRequester].
 */
@Composable
internal fun ProfileSignInColumn(
  isPhoneSignInSelected: Boolean,
  contentFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
  onPhoneSignInClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.widthIn(max = ProfileUiDefaults.SignInColumnMaxWidth),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Image(
      painter = painterResource(R.drawable.img_logo_app),
      contentDescription = stringResource(R.string.app_name),
      modifier = Modifier
        .width(ProfileUiDefaults.LogoWidth)
        .height(ProfileUiDefaults.LogoHeight),
    )

    Spacer(modifier = Modifier.height(ProfileUiDefaults.LogoToTitleSpacing))

    Text(
      text = stringResource(R.string.profile_sign_in_title),
      color = StreamTvColors.NeutralWhite,
      textAlign = TextAlign.Center,
      style = StreamTvTheme.typography.headlineLarge,
    )

    Spacer(modifier = Modifier.height(ProfileUiDefaults.TitleToDescriptionSpacing))

    Text(
      text = stringResource(
        if (isPhoneSignInSelected) {
          R.string.profile_sign_in_phone_hint
        } else {
          R.string.profile_sign_in_description
        },
      ),
      color = StreamTvColors.Neutral20,
      textAlign = TextAlign.Center,
      // Reserving the lines keeps the action button still when the copy swaps.
      minLines = ProfileUiDefaults.DescriptionLineCount,
      maxLines = ProfileUiDefaults.DescriptionLineCount,
      style = StreamTvTheme.typography.bodyLarge,
      modifier = Modifier.testTag("profile-sign-in-description"),
    )

    Spacer(modifier = Modifier.height(ProfileUiDefaults.DescriptionToActionSpacing))

    StreamTvButton(
      text = stringResource(R.string.profile_sign_in_phone_action),
      onClick = onPhoneSignInClick,
      modifier = Modifier
        .testTag("profile-phone-sign-in")
        .focusRequester(contentFocusRequester)
        .focusProperties { up = topBarFocusRequester },
    )
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF010810)
@Composable
private fun ProfileSignInColumnPreview() {
  StreamTvTheme {
    ProfileSignInColumn(
      isPhoneSignInSelected = false,
      contentFocusRequester = remember { FocusRequester() },
      topBarFocusRequester = remember { FocusRequester() },
      onPhoneSignInClick = {},
    )
  }
}
