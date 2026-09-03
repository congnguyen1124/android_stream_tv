package com.congnguyencn.stream_tv.feature.setting.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvButton
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme

/**
 * The pane for entries that only mean something on an account: subscription, payments, gift codes.
 *
 * Its action leaves Settings for the sign-in screen rather than opening a second sign-in flow here,
 * so pairing lives in exactly one place.
 */
@Composable
internal fun SettingSignInRequiredDetail(
  actionFocusRequester: FocusRequester,
  menuFocusRequester: FocusRequester,
  onGetStarted: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Image(
        painter = painterResource(R.drawable.img_logo_app),
        contentDescription = stringResource(R.string.app_name),
        modifier = Modifier
          .width(SettingUiDefaults.GateLogoWidth)
          .height(SettingUiDefaults.GateLogoHeight),
      )

      Spacer(modifier = Modifier.height(SettingUiDefaults.GateLogoToMessageSpacing))

      SettingDetailMessage(
        text = stringResource(R.string.setting_sign_in_required_message),
        textAlign = TextAlign.Center,
        modifier = Modifier.widthIn(max = SettingUiDefaults.GateMessageWidth),
      )

      Spacer(modifier = Modifier.height(SettingUiDefaults.GateMessageToActionSpacing))

      StreamTvButton(
        text = stringResource(R.string.setting_sign_in_required_action),
        onClick = onGetStarted,
        modifier = Modifier
          .testTag("setting-detail-action")
          .focusRequester(actionFocusRequester)
          .focusProperties { left = menuFocusRequester },
      )
    }
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF010810)
@Composable
private fun SettingSignInRequiredDetailPreview() {
  StreamTvTheme {
    SettingSignInRequiredDetail(
      actionFocusRequester = remember { FocusRequester() },
      menuFocusRequester = remember { FocusRequester() },
      onGetStarted = {},
    )
  }
}
