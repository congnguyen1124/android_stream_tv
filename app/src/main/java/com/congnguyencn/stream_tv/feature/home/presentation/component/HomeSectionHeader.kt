package com.congnguyencn.stream_tv.feature.home.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme

@Composable
internal fun HomeSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        modifier = modifier.padding(horizontal = 32.dp),
        color = StreamTvColors.NeutralWhite,
        style = StreamTvTheme.typography.titleLarge,
    )
}
