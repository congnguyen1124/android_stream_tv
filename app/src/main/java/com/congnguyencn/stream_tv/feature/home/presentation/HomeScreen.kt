package com.congnguyencn.stream_tv.feature.home.presentation

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvButton
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme

@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    contentFocusRequester: FocusRequester,
    topBarFocusRequester: FocusRequester,
    onPrimaryActionClick: () -> Unit,
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
                text = stringResource(R.string.home_title),
                color = StreamTvColors.NeutralWhite,
                textAlign = TextAlign.Center,
                style = StreamTvTheme.typography.headlineLarge,
            )
            Text(
                text = stringResource(
                    if (uiState.isReady) {
                        R.string.home_ready_message
                    } else {
                        R.string.home_description
                    },
                ),
                color = StreamTvColors.Neutral20,
                textAlign = TextAlign.Center,
                style = StreamTvTheme.typography.bodyLarge,
            )
            StreamTvButton(
                text = stringResource(R.string.home_primary_action),
                onClick = onPrimaryActionClick,
                modifier = Modifier
                    .focusRequester(contentFocusRequester)
                    .focusProperties { up = topBarFocusRequester },
            )
        }
    }
}
