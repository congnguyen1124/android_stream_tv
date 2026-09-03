package com.congnguyencn.stream_tv.feature.setting.presentation.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.setting.presentation.model.SettingItemUi
import com.congnguyencn.stream_tv.feature.setting.presentation.model.SettingMenuUi
import com.congnguyencn.stream_tv.feature.setting.presentation.model.SettingSectionUi

/**
 * The grouped entry list. Selection follows focus, so moving through it is what changes the detail
 * pane — there is nothing to confirm with Center.
 *
 * [contentFocusRequester] rides on whichever entry is selected, which is how Down from the top bar
 * returns to the entry the viewer was last reading rather than to the top of the list.
 */
@Composable
internal fun SettingMenu(
  selectedItem: SettingItemUi,
  hasDetailAction: Boolean,
  contentFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
  detailActionFocusRequester: FocusRequester,
  onSelectItem: (SettingItemUi) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.width(SettingUiDefaults.MenuWidth),
    verticalArrangement = Arrangement.spacedBy(SettingUiDefaults.MenuItemSpacing),
  ) {
    SettingMenuUi.Sections.forEachIndexed { sectionIndex, section ->
      item(key = "section-${section.titleResId}", contentType = "SectionLabel") {
        SettingSectionLabel(
          section = section,
          isFirstSection = sectionIndex == 0,
        )
      }

      items(
        items = section.items,
        key = SettingItemUi::name,
        contentType = { "Item" },
      ) { item ->
        val isSelected = item == selectedItem

        SettingMenuItem(
          item = item,
          isSelected = isSelected,
          onSelect = { onSelectItem(item) },
          modifier = Modifier
            .fillMaxWidth()
            .then(if (isSelected) Modifier.focusRequester(contentFocusRequester) else Modifier)
            .focusProperties {
              if (item == SettingMenuUi.FirstItem) up = topBarFocusRequester
              // Right belongs to the detail pane's action when it has one. Cancel keeps focus put
              // rather than letting the search reach across the pane to something unrelated.
              right = if (hasDetailAction) detailActionFocusRequester else FocusRequester.Cancel
            },
        )
      }
    }
  }
}

@Composable
private fun SettingSectionLabel(section: SettingSectionUi, isFirstSection: Boolean) {
  Text(
    text = stringResource(section.titleResId),
    color = StreamTvColors.Neutral30,
    maxLines = 1,
    style = StreamTvTheme.typography.labelMedium.copy(
      fontSize = SettingUiDefaults.MenuSectionLabelFontSize,
    ),
    modifier = Modifier.padding(
      top = if (isFirstSection) 0.dp else SettingUiDefaults.MenuSectionSpacing,
      bottom = SettingUiDefaults.MenuSectionLabelBottomSpacing,
    ),
  )
}

@Composable
private fun SettingMenuItem(
  item: SettingItemUi,
  isSelected: Boolean,
  onSelect: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    onClick = onSelect,
    modifier = modifier
      .height(SettingUiDefaults.MenuItemHeight)
      // Selection follows focus: arriving is the selection, so Center only re-states it.
      .onFocusChanged { focusState -> if (focusState.isFocused) onSelect() }
      .then(
        if (isSelected) {
          Modifier.border(
            width = SettingUiDefaults.MenuItemBorderWidth,
            color = StreamTvColors.TransparentWhite40,
            shape = SettingUiDefaults.MenuItemShape,
          )
        } else {
          Modifier
        },
      )
      .testTag("setting-item-${item.name}"),
    shape = ClickableSurfaceDefaults.shape(SettingUiDefaults.MenuItemShape),
    // A dense list must not grow on focus, or entries would overlap their neighbours.
    scale = ClickableSurfaceDefaults.scale(focusedScale = SettingUiDefaults.MenuItemFocusedScale),
    colors = ClickableSurfaceDefaults.colors(
      containerColor = if (isSelected) StreamTvColors.TransparentWhite10 else StreamTvColors.Neutral90,
      contentColor = if (isSelected) StreamTvColors.NeutralWhite else StreamTvColors.Neutral10,
      focusedContainerColor = StreamTvColors.NeutralWhite,
      focusedContentColor = StreamTvColors.NeutralBlack,
      pressedContainerColor = StreamTvColors.NeutralWhite,
      pressedContentColor = StreamTvColors.NeutralBlack,
    ),
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = SettingUiDefaults.MenuItemHorizontalPadding),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = stringResource(item.labelResId),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        style = StreamTvTheme.typography.labelMedium.copy(
          fontSize = SettingUiDefaults.MenuItemFontSize,
        ),
      )
    }
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF010810)
@Composable
private fun SettingMenuPreview() {
  StreamTvTheme {
    SettingMenu(
      selectedItem = SettingItemUi.ManageDevices,
      hasDetailAction = false,
      contentFocusRequester = remember { FocusRequester() },
      topBarFocusRequester = remember { FocusRequester() },
      detailActionFocusRequester = remember { FocusRequester() },
      onSelectItem = {},
    )
  }
}
