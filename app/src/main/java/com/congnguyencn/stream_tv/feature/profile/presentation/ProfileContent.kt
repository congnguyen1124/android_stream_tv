package com.congnguyencn.stream_tv.feature.profile.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.core.designsystem.tokens.StreamTvDimensions
import com.congnguyencn.stream_tv.feature.profile.presentation.component.ProfilePairingPanel
import com.congnguyencn.stream_tv.feature.profile.presentation.component.ProfileSignInColumn
import com.congnguyencn.stream_tv.feature.profile.presentation.component.ProfileUiDefaults

/**
 * Profile as a two-column sign-in screen: identity and the phone action on the left, pairing
 * instructions on the right.
 *
 * Both columns are always present. There is no signed-in variant, because pairing is a dummy
 * session that no backend confirms.
 */
@Composable
internal fun ProfileContent(
  uiState: ProfileUiState,
  contentFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
  onPhoneSignInClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .padding(
        start = StreamTvDimensions.ScreenHorizontalPadding,
        top = ProfileUiDefaults.ScreenTopPadding,
        end = StreamTvDimensions.ScreenHorizontalPadding,
        bottom = ProfileUiDefaults.ScreenBottomPadding,
      )
      .testTag("profile-sign-in"),
  ) {
    Row(
      modifier = Modifier.fillMaxSize(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(ProfileUiDefaults.ScreenColumnGap),
    ) {
      ProfileSignInColumn(
        isPhoneSignInSelected = uiState.isPhoneSignInSelected,
        contentFocusRequester = contentFocusRequester,
        topBarFocusRequester = topBarFocusRequester,
        onPhoneSignInClick = onPhoneSignInClick,
        modifier = Modifier.weight(1f),
      )

      ProfilePairingPanel(
        pairingUrl = uiState.pairingUrl,
        pairingCode = uiState.pairingCode,
        pairingValidUntilLabel = uiState.pairingValidUntilLabel,
      )
    }
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF010810)
@Composable
private fun ProfileContentPreview() {
  StreamTvTheme {
    ProfileContent(
      uiState = ProfilePreviewUiState,
      contentFocusRequester = remember { FocusRequester() },
      topBarFocusRequester = remember { FocusRequester() },
      onPhoneSignInClick = {},
    )
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF010810)
@Composable
private fun ProfileContentPhoneSignInSelectedPreview() {
  StreamTvTheme {
    ProfileContent(
      uiState = ProfilePreviewUiState.copy(isPhoneSignInSelected = true),
      contentFocusRequester = remember { FocusRequester() },
      topBarFocusRequester = remember { FocusRequester() },
      onPhoneSignInClick = {},
    )
  }
}

private val ProfilePreviewUiState = ProfileUiState(
  pairingUrl = "https://tv.streamtv.example.com/pair?code=XHSZ-QBKX",
  pairingCode = "XHSZ-QBKX",
  pairingValidUntilLabel = "14:17",
  isPhoneSignInSelected = false,
)
