package com.congnguyencn.stream_tv.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.congnguyencn.stream_tv.R

private val RobotoRegular = FontFamily(Font(R.font.roboto_regular_400))
private val RobotoMedium = FontFamily(Font(R.font.roboto_medium_500))
private val RobotoBold = FontFamily(Font(R.font.roboto_bold_700))

@Immutable
data class StreamTvTypography(
  val labelMedium: TextStyle = TextStyle(
    fontFamily = RobotoMedium,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
  ),
  val bodyLarge: TextStyle = TextStyle(
    fontFamily = RobotoRegular,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
  ),
  val titleLarge: TextStyle = TextStyle(
    fontFamily = RobotoMedium,
    fontWeight = FontWeight.Medium,
    fontSize = 20.sp,
  ),
  val headlineLarge: TextStyle = TextStyle(
    fontFamily = RobotoBold,
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
  ),
)

internal val DefaultStreamTvTypography = StreamTvTypography()
