package com.congnguyencn.stream_tv.feature.setting.presentation.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
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
 * A privacy pane: what the entry removes, and the control that removes it.
 *
 * Once cleared, the action goes away rather than staying as a button that would do nothing — which
 * also removes the Right target, so the menu stops pointing at it. Pressing it hands focus back to
 * the menu before the state changes, because focus cannot survive on a node that is being removed.
 */
@Composable
internal fun SettingHistoryDetail(
  @StringRes titleResId: Int,
  @StringRes messageResId: Int,
  @StringRes clearedMessageResId: Int,
  isCleared: Boolean,
  actionFocusRequester: FocusRequester,
  menuFocusRequester: FocusRequester,
  onClear: () -> Unit,
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
      text = stringResource(if (isCleared) clearedMessageResId else messageResId),
      textAlign = TextAlign.Start,
      modifier = Modifier
        .widthIn(max = SettingUiDefaults.HistoryMessageWidth)
        .testTag("setting-history-message"),
    )

    if (!isCleared) {
      Spacer(modifier = Modifier.height(SettingUiDefaults.HistoryMessageToActionSpacing))

      StreamTvButton(
        text = stringResource(R.string.setting_clear_action),
        onClick = {
          // Clearing removes this button, and focus cannot be left on a node that is about to go
          // away: park it on the selected menu entry first, then change the state.
          menuFocusRequester.requestFocus()
          onClear()
        },
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
private fun SettingHistoryDetailPreview() {
  StreamTvTheme {
    SettingHistoryDetail(
      titleResId = R.string.setting_item_clear_search_history,
      messageResId = R.string.setting_clear_search_history_message,
      clearedMessageResId = R.string.setting_clear_search_history_done,
      isCleared = false,
      actionFocusRequester = remember { FocusRequester() },
      menuFocusRequester = remember { FocusRequester() },
      onClear = {},
    )
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF010810)
@Composable
private fun SettingHistoryDetailClearedPreview() {
  StreamTvTheme {
    SettingHistoryDetail(
      titleResId = R.string.setting_item_clear_watch_history,
      messageResId = R.string.setting_clear_watch_history_message,
      clearedMessageResId = R.string.setting_clear_watch_history_done,
      isCleared = true,
      actionFocusRequester = remember { FocusRequester() },
      menuFocusRequester = remember { FocusRequester() },
      onClear = {},
    )
  }
}
