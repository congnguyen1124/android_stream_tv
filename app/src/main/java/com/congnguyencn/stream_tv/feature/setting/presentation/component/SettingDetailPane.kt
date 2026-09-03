package com.congnguyencn.stream_tv.feature.setting.presentation.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.setting.presentation.SettingUiState
import com.congnguyencn.stream_tv.feature.setting.presentation.model.SettingItemUi

/**
 * Renders whatever the selected menu entry is about.
 *
 * The `when` is exhaustive on purpose: a new menu entry cannot be added without deciding what its
 * pane shows. Panes cross-fade because selection changes as focus moves through the menu, and a
 * hard swap on every key press reads as flashing.
 */
@Composable
internal fun SettingDetailPane(
  uiState: SettingUiState,
  actionFocusRequester: FocusRequester,
  menuFocusRequester: FocusRequester,
  onOpenSignIn: () -> Unit,
  onClearSearchHistory: () -> Unit,
  onClearWatchHistory: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Crossfade(
    targetState = uiState.selectedItem,
    animationSpec = tween(SettingUiDefaults.DetailCrossFadeDurationMillis),
    label = "SettingDetailPane",
    modifier = modifier.testTag("setting-detail-pane"),
  ) { item ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        // The pane leaving the cross-fade is still in the tree; keeping it focusable would let a
        // Right press land on a control that is about to disappear.
        .focusProperties { canFocus = item == uiState.selectedItem },
    ) {
      when (item) {
        SettingItemUi.ManageSubscription,
        SettingItemUi.PaymentHistory,
        SettingItemUi.GiftCode,
        -> SettingSignInRequiredDetail(
          actionFocusRequester = actionFocusRequester,
          menuFocusRequester = menuFocusRequester,
          onGetStarted = onOpenSignIn,
        )

        SettingItemUi.ManageDevices -> SettingSystemInfoDetail(
          isLoading = uiState.isLoadingSystemInfo,
          systemInfo = uiState.systemInfo,
          errorMessage = uiState.systemInfoErrorMessage,
        )

        SettingItemUi.TermsOfService -> SettingDocumentDetail(
          titleResId = R.string.setting_item_terms_of_service,
          bodyResId = R.string.setting_terms_body,
        )

        SettingItemUi.PrivacyPolicy -> SettingDocumentDetail(
          titleResId = R.string.setting_item_privacy_policy,
          bodyResId = R.string.setting_privacy_policy_body,
        )

        SettingItemUi.SendFeedback -> SettingFeedbackDetail()

        SettingItemUi.ClearSearchHistory -> SettingHistoryDetail(
          titleResId = R.string.setting_item_clear_search_history,
          messageResId = R.string.setting_clear_search_history_message,
          clearedMessageResId = R.string.setting_clear_search_history_done,
          isCleared = uiState.isSearchHistoryCleared,
          actionFocusRequester = actionFocusRequester,
          menuFocusRequester = menuFocusRequester,
          onClear = onClearSearchHistory,
        )

        SettingItemUi.ClearWatchHistory -> SettingHistoryDetail(
          titleResId = R.string.setting_item_clear_watch_history,
          messageResId = R.string.setting_clear_watch_history_message,
          clearedMessageResId = R.string.setting_clear_watch_history_done,
          isCleared = uiState.isWatchHistoryCleared,
          actionFocusRequester = actionFocusRequester,
          menuFocusRequester = menuFocusRequester,
          onClear = onClearWatchHistory,
        )
      }
    }
  }
}

/**
 * Whether the pane for [item] owns a focusable control.
 *
 * The menu needs this before the pane composes: it decides where a Right press goes, and pointing
 * Right at a pane that has no control would drop focus.
 */
internal fun SettingItemUi.hasDetailAction(isSearchHistoryCleared: Boolean, isWatchHistoryCleared: Boolean): Boolean =
  when (this) {
    SettingItemUi.ManageSubscription,
    SettingItemUi.PaymentHistory,
    SettingItemUi.GiftCode,
    -> true

    SettingItemUi.ClearSearchHistory -> !isSearchHistoryCleared
    SettingItemUi.ClearWatchHistory -> !isWatchHistoryCleared

    SettingItemUi.ManageDevices,
    SettingItemUi.TermsOfService,
    SettingItemUi.PrivacyPolicy,
    SettingItemUi.SendFeedback,
    -> false
  }

@Composable
internal fun SettingDetailLabel(text: String, modifier: Modifier = Modifier) {
  Text(
    text = text,
    color = StreamTvColors.NeutralWhite,
    style = StreamTvTheme.typography.labelMedium.copy(
      fontSize = SettingUiDefaults.DetailLabelFontSize,
    ),
    modifier = modifier,
  )
}

@Composable
internal fun SettingDetailCard(title: String, subtitle: String, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(SettingUiDefaults.DetailCardShape)
      .background(StreamTvColors.TransparentWhite5)
      .border(
        width = SettingUiDefaults.DetailCardBorderWidth,
        color = StreamTvColors.TransparentWhite10,
        shape = SettingUiDefaults.DetailCardShape,
      )
      .padding(SettingUiDefaults.DetailCardPadding),
  ) {
    Text(
      text = title,
      color = StreamTvColors.NeutralWhite,
      style = StreamTvTheme.typography.titleLarge.copy(
        fontSize = SettingUiDefaults.DetailCardTitleFontSize,
      ),
    )
    Text(
      text = subtitle,
      color = StreamTvColors.Neutral20,
      style = StreamTvTheme.typography.labelMedium.copy(
        fontSize = SettingUiDefaults.DetailCardSubtitleFontSize,
      ),
      modifier = Modifier.padding(top = SettingUiDefaults.DetailCardTitleSpacing),
    )
  }
}

@Composable
internal fun SettingDetailMessage(text: String, textAlign: TextAlign, modifier: Modifier = Modifier) {
  Text(
    text = text,
    color = StreamTvColors.Neutral20,
    textAlign = textAlign,
    style = StreamTvTheme.typography.bodyLarge.copy(
      fontSize = SettingUiDefaults.DetailBodyFontSize,
      lineHeight = SettingUiDefaults.DetailBodyLineHeight,
    ),
    modifier = modifier,
  )
}

@Composable
internal fun SettingDetailPlaceholder(text: String) {
  Box(
    modifier = Modifier.fillMaxSize(),
  ) {
    SettingDetailMessage(
      text = text,
      textAlign = TextAlign.Start,
      modifier = Modifier.testTag("setting-detail-placeholder"),
    )
  }
}
