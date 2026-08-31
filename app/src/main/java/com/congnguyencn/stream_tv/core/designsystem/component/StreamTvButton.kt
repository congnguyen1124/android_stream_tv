package com.congnguyencn.stream_tv.core.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme

@Composable
fun StreamTvButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.colors(
            containerColor = StreamTvColors.TransparentWhite10,
            contentColor = StreamTvColors.NeutralWhite,
            focusedContainerColor = StreamTvColors.NeutralWhite,
            focusedContentColor = StreamTvColors.NeutralBlack,
            pressedContainerColor = StreamTvColors.Primary60,
            pressedContentColor = StreamTvColors.NeutralWhite,
        ),
        scale = ButtonDefaults.scale(focusedScale = 1.08f),
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
    ) {
        Text(
            text = text,
            style = StreamTvTheme.typography.labelMedium,
        )
    }
}
