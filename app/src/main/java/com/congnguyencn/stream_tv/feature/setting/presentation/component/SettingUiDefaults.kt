package com.congnguyencn.stream_tv.feature.setting.presentation.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.congnguyencn.stream_tv.core.designsystem.tokens.StreamTvDimensions

/**
 * Settings dimensions.
 *
 * The menu is a fixed-width column and the detail pane takes the rest, so both are sized from the
 * 540-unit television viewport: nine entries plus three group labels have to fit under the top bar
 * without the list scrolling on a 720p device.
 */
internal object SettingUiDefaults {
  //region Screen
  val ScreenTopPadding = StreamTvDimensions.TopBarHeight + 8.dp
  val ScreenBottomPadding = 20.dp
  val TitleToBodySpacing = 10.dp
  val TitleFontSize = 22.sp
  val PaneGap = 32.dp
  //endregion

  //region Menu
  val MenuWidth = 208.dp
  val MenuItemHeight = 29.dp
  val MenuItemSpacing = 4.dp
  val MenuItemHorizontalPadding = 8.dp
  val MenuItemShape = RoundedCornerShape(6.dp)
  val MenuItemBorderWidth = 1.dp
  val MenuSectionSpacing = 8.dp
  val MenuSectionLabelBottomSpacing = 4.dp
  val MenuSectionLabelFontSize = 11.sp
  val MenuItemFontSize = 13.sp
  const val MenuItemFocusedScale = 1f
  //endregion

  //region Detail pane
  val DetailCardShape = RoundedCornerShape(10.dp)
  val DetailCardBorderWidth = 1.dp
  val DetailCardPadding = 16.dp
  val DetailSectionLabelSpacing = 8.dp
  val DetailSectionSpacing = 18.dp
  val DetailHeadingSpacing = 12.dp
  val DetailBodyLineHeight = 22.sp
  val DetailBodyFontSize = 14.sp
  val DetailLabelFontSize = 13.sp
  val DetailCardTitleFontSize = 18.sp
  val DetailCardSubtitleFontSize = 13.sp
  val DetailCardTitleSpacing = 4.dp
  const val DetailCrossFadeDurationMillis = 180
  //endregion

  //region Sign-in gate
  val GateLogoWidth = 152.dp
  val GateLogoHeight = 31.dp
  val GateLogoToMessageSpacing = 20.dp
  val GateMessageWidth = 380.dp
  val GateMessageToActionSpacing = 22.dp
  //endregion

  //region Feedback
  val FeedbackQrSize = 116.dp
  val FeedbackQrToUrlSpacing = 12.dp
  val FeedbackMessageWidth = 340.dp
  //endregion

  //region History action
  val HistoryMessageWidth = 400.dp
  val HistoryMessageToActionSpacing = 18.dp
  //endregion
}
