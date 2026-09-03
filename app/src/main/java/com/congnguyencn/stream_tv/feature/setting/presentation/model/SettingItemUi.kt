package com.congnguyencn.stream_tv.feature.setting.presentation.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.congnguyencn.stream_tv.R

/**
 * The Settings menu entries, in the order they are shown.
 *
 * Modeled as an enum so that every place deciding what a menu entry does — the detail pane above
 * all — has to name each entry, and adding one fails to compile until its detail exists.
 */
@Immutable
internal enum class SettingItemUi(@StringRes val labelResId: Int) {
  ManageSubscription(labelResId = R.string.setting_item_manage_subscription),
  PaymentHistory(labelResId = R.string.setting_item_payment_history),
  ManageDevices(labelResId = R.string.setting_item_manage_devices),
  GiftCode(labelResId = R.string.setting_item_gift_code),
  TermsOfService(labelResId = R.string.setting_item_terms_of_service),
  PrivacyPolicy(labelResId = R.string.setting_item_privacy_policy),
  SendFeedback(labelResId = R.string.setting_item_send_feedback),
  ClearSearchHistory(labelResId = R.string.setting_item_clear_search_history),
  ClearWatchHistory(labelResId = R.string.setting_item_clear_watch_history),
}

/** One labeled group of menu entries. The group label is never focusable. */
@Immutable
internal data class SettingSectionUi(@StringRes val titleResId: Int, val items: List<SettingItemUi>)

/**
 * The menu is a fixed structure, not screen state: it never varies with data, so it lives here as a
 * constant instead of being rebuilt into every state emission.
 */
internal object SettingMenuUi {
  val Sections = listOf(
    SettingSectionUi(
      titleResId = R.string.setting_section_account,
      items = listOf(
        SettingItemUi.ManageSubscription,
        SettingItemUi.PaymentHistory,
        SettingItemUi.ManageDevices,
        SettingItemUi.GiftCode,
      ),
    ),
    SettingSectionUi(
      titleResId = R.string.setting_section_about,
      items = listOf(
        SettingItemUi.TermsOfService,
        SettingItemUi.PrivacyPolicy,
        SettingItemUi.SendFeedback,
      ),
    ),
    SettingSectionUi(
      titleResId = R.string.setting_section_privacy,
      items = listOf(
        SettingItemUi.ClearSearchHistory,
        SettingItemUi.ClearWatchHistory,
      ),
    ),
  )

  val FirstItem = SettingItemUi.ManageSubscription
}
