package com.congnguyencn.stream_tv.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme

@Composable
fun StreamTvActionScreen(
    title: String,
    description: String,
    actionText: String,
    contentFocusRequester: FocusRequester,
    topBarFocusRequester: FocusRequester,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(contentFocusRequester) {
        contentFocusRequester.requestFocus()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = title,
                color = StreamTvColors.NeutralWhite,
                textAlign = TextAlign.Center,
                style = StreamTvTheme.typography.headlineLarge,
            )
            Text(
                text = description,
                color = StreamTvColors.Neutral20,
                textAlign = TextAlign.Center,
                style = StreamTvTheme.typography.bodyLarge,
            )
            StreamTvButton(
                text = actionText,
                onClick = onActionClick,
                modifier = Modifier
                    .focusRequester(contentFocusRequester)
                    .focusProperties { up = topBarFocusRequester },
            )
        }
    }
}
