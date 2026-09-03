package com.congnguyencn.stream_tv.feature.setting.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * The feedback pane hands the writing to a phone.
 *
 * Typing a paragraph on a D-pad keyboard is the worst way to collect feedback, so the TV only shows
 * where to send it — the same pattern Profile uses for sign-in.
 */
@Composable
internal fun SettingFeedbackDetail(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = stringResource(R.string.setting_item_send_feedback),
        color = StreamTvColors.NeutralWhite,
        style = StreamTvTheme.typography.titleLarge,
      )

      Spacer(modifier = Modifier.height(SettingUiDefaults.DetailHeadingSpacing))

      SettingDetailMessage(
        text = stringResource(R.string.setting_feedback_message),
        textAlign = TextAlign.Center,
        modifier = Modifier.widthIn(max = SettingUiDefaults.FeedbackMessageWidth),
      )

      Spacer(modifier = Modifier.height(SettingUiDefaults.DetailSectionSpacing))

      StreamTvQrCode(
        content = SettingFeedbackDefaults.FeedbackUrl,
        contentDescription = stringResource(R.string.setting_feedback_qr_description),
        modifier = Modifier
          .size(SettingUiDefaults.FeedbackQrSize)
          .testTag("setting-feedback-qr"),
      )

      Spacer(modifier = Modifier.height(SettingUiDefaults.FeedbackQrToUrlSpacing))

      Text(
        text = stringResource(R.string.setting_feedback_url),
        color = StreamTvColors.Neutral20,
        style = StreamTvTheme.typography.labelMedium,
      )
    }
  }
}

private object SettingFeedbackDefaults {
  /** Placeholder destination: no feedback service is wired up in this build. */
  const val FeedbackUrl = "https://feedback.streamtv.example.com"
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF010810)
@Composable
private fun SettingFeedbackDetailPreview() {
  StreamTvTheme {
    SettingFeedbackDetail()
  }
}
