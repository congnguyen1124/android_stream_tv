package com.congnguyencn.stream_tv.feature.home.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.core.designsystem.tokens.StreamTvDimensions

@Composable
internal fun HomeSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        modifier = modifier.padding(horizontal = StreamTvDimensions.ScreenHorizontalPadding),
        color = StreamTvColors.NeutralWhite,
        style = StreamTvTheme.typography.titleLarge,
    )
}
