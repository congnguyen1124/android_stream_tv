package com.congnguyencn.stream_tv.feature.profile.presentation.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.congnguyencn.stream_tv.core.designsystem.tokens.StreamTvDimensions

/**
 * Profile sign-in dimensions.
 *
 * The vertical budget is deliberate: the pairing panel has to fit under the 80-unit top bar on a
 * 540-unit-high television viewport without scrolling, because nothing on this screen except the
 * phone action can take focus and therefore nothing can scroll it into view.
 */
internal object ProfileUiDefaults {
  //region Screen
  val ScreenTopPadding = StreamTvDimensions.TopBarHeight + 8.dp
  val ScreenBottomPadding = 20.dp
  val ScreenColumnGap = 32.dp
  //endregion

  //region Sign-in column
  val SignInColumnMaxWidth = 400.dp
  val LogoWidth = 168.dp
  val LogoHeight = 34.dp
  val LogoToTitleSpacing = 16.dp
  val TitleToDescriptionSpacing = 10.dp
  val DescriptionToActionSpacing = 24.dp
  const val DescriptionLineCount = 3
  //endregion

  //region Pairing panel
  val PanelWidth = 344.dp
  val PanelShape = RoundedCornerShape(14.dp)
  val PanelBorderWidth = 1.dp
  val PanelHorizontalPadding = 20.dp
  val PanelVerticalPadding = 16.dp
  val PanelTitleToStepsSpacing = 14.dp
  val PanelStepSpacing = 12.dp
  val PanelStepsToQrSpacing = 14.dp
  val PanelQrSize = 124.dp
  val PanelQrToDividerSpacing = 12.dp
  val PanelDividerToCodeSpacing = 10.dp
  val PanelCodeLabelSpacing = 2.dp
  val PanelCodeToValiditySpacing = 6.dp
  const val PanelGradientEndAlpha = 0.35f
  //endregion

  //region Steps
  val StepBadgeSize = 20.dp
  val StepBadgeToTextSpacing = 10.dp
  val StepTextToArtSpacing = 8.dp
  val StepBadgeFontSize = 11.sp
  val StepFontSize = 13.sp
  const val StepTextLineCount = 2
  //endregion

  //region Mobile app illustration
  val AppArtHeight = 30.dp
  val AppArtShape = RoundedCornerShape(8.dp)
  val AppArtHorizontalPadding = 8.dp
  val AppArtItemSpacing = 9.dp
  val AppArtIconSize = 12.dp
  val AppArtHomeLabelSpacing = 4.dp
  val AppArtHomeLabelFontSize = 9.sp
  val AppArtProfileRingSize = 19.dp
  val AppArtProfileRingWidth = 1.dp
  val AppArtProfileIconSize = 11.dp
  //endregion

  //region Divider and code
  val DividerThickness = 1.dp
  val DividerLabelSpacing = 10.dp
  val DividerLabelFontSize = 11.sp
  val DividerLabelLetterSpacing = 1.sp
  val CodeFontSize = 28.sp
  val CodeLetterSpacing = 2.sp
  val ValidityFontSize = 12.sp
  //endregion
}
