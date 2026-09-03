package com.congnguyencn.stream_tv.feature.setting.presentation.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme

/**
 * A read-only document pane — terms, policy.
 *
 * The copy is written to fit the pane at 720p. Nothing here is focusable, so a document that
 * outgrew the viewport could not be scrolled into view; shortening the copy is the fix, not adding
 * a scroll container the D-pad cannot reach.
 */
@Composable
internal fun SettingDocumentDetail(
  @StringRes titleResId: Int,
  @StringRes bodyResId: Int,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxSize()) {
    Text(
      text = stringResource(titleResId),
      color = StreamTvColors.NeutralWhite,
      style = StreamTvTheme.typography.titleLarge,
    )

    Spacer(modifier = Modifier.height(SettingUiDefaults.DetailHeadingSpacing))

    SettingDetailMessage(
      text = stringResource(bodyResId),
      textAlign = TextAlign.Start,
      modifier = Modifier
        .widthIn(max = SettingDocumentDefaults.BodyWidth)
        .testTag("setting-document-body"),
    )
  }
}

private object SettingDocumentDefaults {
  val BodyWidth = 520.dp
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF010810)
@Composable
private fun SettingDocumentDetailPreview() {
  StreamTvTheme {
    SettingDocumentDetail(
      titleResId = R.string.setting_item_terms_of_service,
      bodyResId = R.string.setting_terms_body,
    )
  }
}
