package com.congnguyencn.stream_tv.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Icon
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.SelectableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.core.designsystem.tokens.StreamTvDimensions
import kotlin.math.max

@Composable
fun StreamTvTopBar(
    items: List<StreamTvTopBarItem>,
    selectedItemId: String?,
    contentFocusRequester: FocusRequester,
    onItemClick: (StreamTvTopBarItem) -> Unit,
    modifier: Modifier = Modifier,
    onFocusStateChanged: (hasFocus: Boolean) -> Unit = {},
) {
    val navigationItems = remember(items) {
        items.filter { it.role == StreamTvTopBarItemRole.Destination }
    }
    val profileItem = remember(items) {
        items.firstOrNull { it.role == StreamTvTopBarItemRole.Profile }
    }
    val visibleItems = remember(navigationItems, profileItem) {
        buildList {
            addAll(navigationItems)
            profileItem?.let(::add)
        }
    }
    val itemFocusRequesters = remember(visibleItems.map(StreamTvTopBarItem::id)) {
        visibleItems.associate { it.id to FocusRequester() }
    }
    val horizontalArrangement = rememberRightAlignedTopBarArrangement()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(StreamTvDimensions.TopBarGradientHeight)
            .onFocusChanged { focusState -> onFocusStateChanged(focusState.hasFocus) }
            .focusGroup(),
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .focusProperties {
                    onEnter = {
                        val targetItem = visibleItems.firstOrNull { it.id == selectedItemId }
                            ?: navigationItems.firstOrNull()
                            ?: profileItem
                        targetItem?.let { itemFocusRequesters[it.id]?.requestFocus() }
                    }
                },
            contentPadding = PaddingValues(
                start = StreamTvDimensions.ScreenHorizontalPadding,
                end = StreamTvDimensions.ScreenHorizontalPadding,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = horizontalArrangement,
        ) {
            item(key = "stream-tv-logo", contentType = { "AppBar" }) {
                StreamTvAppBar(
                    colors = SurfaceDefaults.colors(containerColor = StreamTvColors.Transparent),
                )
            }

            items(
                items = navigationItems,
                key = StreamTvTopBarItem::id,
                contentType = { "Destination" },
            ) { item ->
                DestinationItem(
                    item = item,
                    selected = item.id == selectedItemId,
                    contentFocusRequester = contentFocusRequester,
                    focusRequester = itemFocusRequesters.getValue(item.id),
                    onClick = { onItemClick(item) },
                )
            }

            profileItem?.let { item ->
                item(key = item.id, contentType = { "Profile" }) {
                    ProfileItem(
                        item = item,
                        selected = item.id == selectedItemId,
                        contentFocusRequester = contentFocusRequester,
                        focusRequester = itemFocusRequesters.getValue(item.id),
                        onClick = { onItemClick(item) },
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberRightAlignedTopBarArrangement(): Arrangement.Horizontal {
    val spacingPx = with(LocalDensity.current) {
        StreamTvDimensions.NavigationItemSpacing.roundToPx()
    }
    return remember(spacingPx) {
        rightAlignedTopBarArrangement(spacingPx)
    }
}

private fun rightAlignedTopBarArrangement(spacingPx: Int): Arrangement.Horizontal =
    object : Arrangement.Horizontal {
        override fun Density.arrange(
            totalSize: Int,
            sizes: IntArray,
            layoutDirection: LayoutDirection,
            outPositions: IntArray,
        ) {
            if (sizes.isEmpty()) return

            val positions = IntArray(sizes.size)
            positions[0] = 0

            if (sizes.lastIndex >= FirstNavigationItemIndex) {
                val navigationItemCount = sizes.lastIndex
                val groupWidth = (FirstNavigationItemIndex..sizes.lastIndex).sumOf { sizes[it] } +
                    spacingPx * (navigationItemCount - 1)
                var current = max(
                    totalSize - groupWidth,
                    sizes[0] + spacingPx,
                )

                for (index in FirstNavigationItemIndex..sizes.lastIndex) {
                    positions[index] = current
                    current += sizes[index] + spacingPx
                }
            }

            if (layoutDirection == LayoutDirection.Rtl) {
                for (index in positions.indices) {
                    outPositions[index] = totalSize - positions[index] - sizes[index]
                }
            } else {
                positions.copyInto(outPositions)
            }
        }
    }

private const val FirstNavigationItemIndex = 1
private const val ItemExpansionDurationMillis = 180

@Composable
private fun DestinationItem(
    item: StreamTvTopBarItem,
    selected: Boolean,
    contentFocusRequester: FocusRequester,
    focusRequester: FocusRequester,
    onClick: () -> Unit,
) {
    var isFocused by remember(item.id) { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .onFocusChanged { isFocused = it.isFocused }
            .focusRequester(focusRequester)
            .focusProperties { down = contentFocusRequester },
        selected = selected,
        border = SelectableSurfaceDefaults.border(),
        scale = SelectableSurfaceDefaults.scale(focusedScale = 1f),
        colors = SelectableSurfaceDefaults.colors(
            containerColor = StreamTvColors.Transparent,
            focusedContainerColor = StreamTvColors.NeutralWhite,
            focusedContentColor = StreamTvColors.NeutralBlack,
            focusedSelectedContainerColor = StreamTvColors.NeutralWhite,
            focusedSelectedContentColor = StreamTvColors.NeutralBlack,
            selectedContainerColor = StreamTvColors.TransparentWhite20,
            selectedContentColor = StreamTvColors.NeutralWhite,
            pressedContainerColor = StreamTvColors.Primary60,
            pressedContentColor = StreamTvColors.NeutralWhite,
        ),
        shape = SelectableSurfaceDefaults.shape(shape = CircleShape),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                imageVector = ImageVector.vectorResource(item.iconResId),
                contentDescription = stringResource(item.titleResId),
                tint = LocalContentColor.current,
            )
            AnimatedVisibility(
                visible = isFocused,
                enter = expandHorizontally(
                    animationSpec = tween(ItemExpansionDurationMillis),
                    expandFrom = Alignment.Start,
                ) + fadeIn(animationSpec = tween(ItemExpansionDurationMillis)),
                exit = shrinkHorizontally(
                    animationSpec = tween(ItemExpansionDurationMillis),
                    shrinkTowards = Alignment.Start,
                ) + fadeOut(animationSpec = tween(ItemExpansionDurationMillis)),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(item.titleResId),
                        style = StreamTvTheme.typography.labelMedium,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileItem(
    item: StreamTvTopBarItem,
    selected: Boolean,
    contentFocusRequester: FocusRequester,
    focusRequester: FocusRequester,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .padding(start = 10.dp)
            .focusRequester(focusRequester)
            .focusProperties { down = contentFocusRequester },
        selected = selected,
        colors = SelectableSurfaceDefaults.colors(
            containerColor = StreamTvColors.Transparent,
            contentColor = StreamTvColors.Neutral20,
            focusedContainerColor = StreamTvColors.Transparent,
            focusedContentColor = StreamTvColors.NeutralWhite,
            selectedContainerColor = StreamTvColors.Transparent,
            selectedContentColor = StreamTvColors.NeutralWhite,
        ),
        border = SelectableSurfaceDefaults.border(
            focusedBorder = Border(
                inset = 4.dp,
                border = BorderStroke(2.dp, StreamTvColors.NeutralWhite),
                shape = CircleShape,
            ),
            selectedBorder = Border(
                inset = 4.dp,
                border = BorderStroke(2.dp, StreamTvColors.TransparentWhite20),
                shape = CircleShape,
            ),
        ),
        scale = SelectableSurfaceDefaults.scale(focusedScale = 1f),
        shape = SelectableSurfaceDefaults.shape(shape = CircleShape),
        onClick = onClick,
    ) {
        Icon(
            modifier = Modifier
                .size(32.dp)
                .padding(6.dp),
            imageVector = ImageVector.vectorResource(item.iconResId),
            contentDescription = stringResource(item.titleResId),
            tint = LocalContentColor.current,
        )
    }
}
