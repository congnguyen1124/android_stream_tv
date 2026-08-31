package com.congnguyencn.stream_tv.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.home.presentation.component.HomeBannerSection
import com.congnguyencn.stream_tv.feature.home.presentation.component.HomeVerticalBannerSection
import com.congnguyencn.stream_tv.feature.home.presentation.model.HomeContentUiItem
import com.congnguyencn.stream_tv.feature.home.presentation.model.HomeSectionUiItem
import com.congnguyencn.stream_tv.feature.home.presentation.model.HomeSectionViewTypeUi

@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    contentFocusRequester: FocusRequester,
    topBarFocusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(uiState.sections) {
        if (uiState.sections.any { it.viewType == HomeSectionViewTypeUi.Banner }) {
            contentFocusRequester.requestFocus()
        }
    }

    when {
        uiState.isLoading -> HomeMessage(
            message = "Loading your StreamTV home...",
            modifier = modifier,
        )
        uiState.errorMessage != null -> HomeMessage(
            message = uiState.errorMessage,
            modifier = modifier,
        )
        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                top = 22.dp,
                bottom = 54.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(34.dp),
        ) {
            items(
                items = uiState.sections,
                key = HomeSectionUiItem::id,
            ) { section ->
                when (section.viewType) {
                    HomeSectionViewTypeUi.Banner -> HomeBannerSection(
                        title = section.title,
                        items = section.items.requireItemsOfType(),
                        contentFocusRequester = contentFocusRequester,
                        topBarFocusRequester = topBarFocusRequester,
                    )
                    HomeSectionViewTypeUi.VerticalBanner -> HomeVerticalBannerSection(
                        title = section.title,
                        items = section.items.requireItemsOfType(),
                    )
                    HomeSectionViewTypeUi.Videos,
                    HomeSectionViewTypeUi.ListSeries,
                    HomeSectionViewTypeUi.Channels,
                    HomeSectionViewTypeUi.Shorts,
                    -> Unit
                }
            }
        }
    }
}

@Composable
private fun HomeMessage(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(32.dp),
            color = StreamTvColors.Neutral20,
            style = StreamTvTheme.typography.titleLarge,
        )
    }
}

private inline fun <reified T : HomeContentUiItem> List<HomeContentUiItem>.requireItemsOfType(): List<T> =
    map { item ->
        requireNotNull(item as? T) {
            "Expected ${T::class.simpleName}, but received ${item::class.simpleName}"
        }
    }
