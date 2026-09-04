package com.congnguyencn.stream_tv.feature.player.presentation.component.section

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingCategory
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingOptionUiItem
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingUiItem
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingsUiState

/**
 * Layout metrics for the settings sections.
 *
 * `PlayerSideSection` sizes the compact settings panel from these values, so they are the single
 * source for both the rows and the panel that holds them. Keeping a second copy next to the panel is
 * what let the two drift apart and left the root list overflowing its own viewport.
 */
internal object PlayerSettingSectionDefaults {
  val ItemShape = RoundedCornerShape(8.dp)
  val RootItemHeight = 54.dp
  val OptionItemHeight = 42.dp
  val ItemIconSize = 20.dp
  val TrailingIconSize = 14.dp
  val ItemSpacing = 4.dp
  val HeaderSpacing = 14.dp
  val HeaderFontSize = 24.sp
  val HeaderLineHeight = 30.sp
  val LabelFontSize = 15.sp
  val LabelLineHeight = 20.sp
  val CaptionFontSize = 12.sp
  val CaptionLineHeight = 16.sp
  val InactiveCaptionAlpha = 0.6f
}

/**
 * Centres glyphs inside their line box.
 *
 * Compose adds a line's extra leading below the glyphs, so text in a fixed-height row renders
 * visibly above the row's centre even with `CenterVertically`.
 */
private fun TextStyle.centeredInLineBox(): TextStyle = copy(
  lineHeightStyle = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
  ),
  platformStyle = PlatformTextStyle(includeFontPadding = false),
)

@Composable
internal fun PlayerSettingRootSection(
  settings: PlayerSettingsUiState,
  restoredCategory: PlayerSettingCategory?,
  isFocusEnabled: Boolean,
  focusRequester: FocusRequester,
  onCategorySelected: (PlayerSettingCategory) -> Unit,
  onBack: () -> Unit,
  dismissOnLeft: Boolean,
  modifier: Modifier = Modifier,
) {
  val targetCategory = restoredCategory
    ?.takeIf { category -> settings.item(category) != null }
    ?: settings.items.firstOrNull()?.category

  LaunchedEffect(isFocusEnabled, targetCategory, settings) {
    if (isFocusEnabled && targetCategory != null) {
      awaitPlayerSectionFrame()
      focusRequester.requestFocus()
    }
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .handlePlayerSectionExit(onBack = onBack, dismissOnLeft = dismissOnLeft)
      .focusGroup(),
  ) {
    PlayerSectionHeader(title = stringResource(R.string.player_settings))
    Spacer(modifier = Modifier.size(PlayerSettingSectionDefaults.HeaderSpacing))

    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(PlayerSettingSectionDefaults.ItemSpacing),
    ) {
      items(
        items = settings.items,
        key = { item -> item.category.name },
      ) { item ->
        PlayerSettingRootItem(
          item = item,
          isFocusEnabled = isFocusEnabled,
          onClick = { onCategorySelected(item.category) },
          modifier = if (item.category == targetCategory) {
            Modifier.focusRequester(focusRequester)
          } else {
            Modifier
          },
        )
      }
    }
  }
}

@Composable
internal fun PlayerSettingOptionsSection(
  item: PlayerSettingUiItem,
  isFocusEnabled: Boolean,
  focusRequester: FocusRequester,
  onOptionSelected: (PlayerSettingOptionUiItem) -> Unit,
  onBack: () -> Unit,
  dismissOnLeft: Boolean,
  modifier: Modifier = Modifier,
) {
  val focusIndex = item.options.indexOfFirst(PlayerSettingOptionUiItem::isSelected).coerceAtLeast(0)

  LaunchedEffect(isFocusEnabled, item) {
    if (isFocusEnabled && item.options.isNotEmpty()) {
      awaitPlayerSectionFrame()
      focusRequester.requestFocus()
    }
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .handlePlayerSectionExit(onBack = onBack, dismissOnLeft = dismissOnLeft)
      .focusGroup(),
  ) {
    PlayerSectionHeader(title = stringResource(item.category.titleResId()))
    Spacer(modifier = Modifier.size(PlayerSettingSectionDefaults.HeaderSpacing))
    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(PlayerSettingSectionDefaults.ItemSpacing),
    ) {
      itemsIndexed(
        items = item.options,
        key = { _, option -> option.id },
      ) { index, option ->
        PlayerSettingOptionItem(
          option = option,
          isFocusEnabled = isFocusEnabled,
          onClick = { onOptionSelected(option) },
          modifier = if (index == focusIndex) {
            Modifier.focusRequester(focusRequester)
          } else {
            Modifier
          },
        )
      }
    }
  }
}

@Composable
internal fun PlayerSectionHeader(title: String, modifier: Modifier = Modifier) {
  Text(
    text = title,
    modifier = modifier,
    color = StreamTvColors.NeutralWhite,
    style = StreamTvTheme.typography.headlineLarge.copy(
      fontSize = PlayerSettingSectionDefaults.HeaderFontSize,
      lineHeight = PlayerSettingSectionDefaults.HeaderLineHeight,
    ).centeredInLineBox(),
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
  )
}

@Composable
private fun PlayerSettingRootItem(
  item: PlayerSettingUiItem,
  isFocusEnabled: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  PlayerSettingSurface(
    onClick = onClick,
    isCurrentValue = false,
    modifier = modifier
      .fillMaxWidth()
      .heightIn(min = PlayerSettingSectionDefaults.RootItemHeight)
      .focusProperties { canFocus = isFocusEnabled }
      .testTag("player-setting-${item.category.name.lowercase()}"),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = ImageVector.vectorResource(item.category.iconResId()),
        contentDescription = null,
        modifier = Modifier.size(PlayerSettingSectionDefaults.ItemIconSize),
        tint = LocalContentColor.current,
      )
      Spacer(modifier = Modifier.width(8.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = stringResource(item.category.titleResId()),
          style = StreamTvTheme.typography.bodyLarge.copy(
            fontSize = PlayerSettingSectionDefaults.LabelFontSize,
            lineHeight = PlayerSettingSectionDefaults.LabelLineHeight,
          ).centeredInLineBox(),
        )
        Text(
          text = item.selectedLabel,
          color = LocalContentColor.current.copy(
            alpha = PlayerSettingSectionDefaults.InactiveCaptionAlpha,
          ),
          style = StreamTvTheme.typography.labelMedium.copy(
            fontSize = PlayerSettingSectionDefaults.CaptionFontSize,
            lineHeight = PlayerSettingSectionDefaults.CaptionLineHeight,
          ).centeredInLineBox(),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      Icon(
        imageVector = ImageVector.vectorResource(R.drawable.ic_chevron_right),
        contentDescription = null,
        modifier = Modifier.size(PlayerSettingSectionDefaults.TrailingIconSize),
        tint = LocalContentColor.current,
      )
    }
  }
}

@Composable
private fun PlayerSettingOptionItem(
  option: PlayerSettingOptionUiItem,
  isFocusEnabled: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  PlayerSettingSurface(
    onClick = onClick,
    isCurrentValue = option.isSelected,
    modifier = modifier
      .fillMaxWidth()
      .heightIn(min = PlayerSettingSectionDefaults.OptionItemHeight)
      .focusProperties { canFocus = isFocusEnabled }
      .testTag("player-setting-option-${option.id}"),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = option.label,
        modifier = Modifier.weight(1f),
        style = StreamTvTheme.typography.bodyLarge.copy(
          fontSize = PlayerSettingSectionDefaults.LabelFontSize,
          lineHeight = PlayerSettingSectionDefaults.LabelLineHeight,
        ).centeredInLineBox(),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      if (option.isSelected) {
        Icon(
          imageVector = ImageVector.vectorResource(R.drawable.ic_player_check),
          contentDescription = stringResource(R.string.player_setting_selected),
          modifier = Modifier.size(PlayerSettingSectionDefaults.TrailingIconSize),
          tint = LocalContentColor.current,
        )
      }
    }
  }
}

@Composable
private fun PlayerSettingSurface(
  onClick: () -> Unit,
  isCurrentValue: Boolean,
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit,
) {
  Surface(
    onClick = onClick,
    modifier = modifier,
    shape = ClickableSurfaceDefaults.shape(shape = PlayerSettingSectionDefaults.ItemShape),
    // A dense list must not grow on focus, or a row would overlap its neighbours.
    scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
    border = ClickableSurfaceDefaults.border(
      border = if (isCurrentValue) {
        Border(
          border = BorderStroke(1.dp, StreamTvColors.TransparentWhite20),
          shape = PlayerSettingSectionDefaults.ItemShape,
        )
      } else {
        Border.None
      },
      focusedBorder = Border.None,
      pressedBorder = Border.None,
    ),
    colors = ClickableSurfaceDefaults.colors(
      containerColor = if (isCurrentValue) {
        StreamTvColors.TransparentWhite10
      } else {
        StreamTvColors.Transparent
      },
      contentColor = if (isCurrentValue) StreamTvColors.NeutralWhite else StreamTvColors.Neutral10,
      focusedContainerColor = StreamTvColors.NeutralWhite,
      focusedContentColor = StreamTvColors.NeutralBlack,
      pressedContainerColor = StreamTvColors.NeutralWhite,
      pressedContentColor = StreamTvColors.NeutralBlack,
    ),
    content = content,
  )
}

private fun PlayerSettingCategory.titleResId(): Int = when (this) {
  PlayerSettingCategory.Quality -> R.string.player_setting_quality
  PlayerSettingCategory.Subtitles -> R.string.player_setting_subtitles
  PlayerSettingCategory.Audio -> R.string.player_setting_audio
}

private fun PlayerSettingCategory.iconResId(): Int = when (this) {
  PlayerSettingCategory.Quality -> R.drawable.ic_hd
  PlayerSettingCategory.Subtitles -> R.drawable.ic_subtitles
  PlayerSettingCategory.Audio -> R.drawable.ic_audio
}

@Preview(device = Devices.TV_720p, showBackground = true, backgroundColor = 0xFF171717)
@Composable
private fun PlayerSettingRootSectionPreview() {
  StreamTvTheme {
    PlayerSettingRootSection(
      settings = PlayerSettingsUiState(
        items = listOf(
          PlayerSettingUiItem(
            category = PlayerSettingCategory.Quality,
            selectedLabel = "1080p",
            options = listOf(
              PlayerSettingOptionUiItem(id = "1080", label = "1080p", isSelected = true),
            ),
          ),
          PlayerSettingUiItem(
            category = PlayerSettingCategory.Subtitles,
            selectedLabel = "English",
            options = listOf(
              PlayerSettingOptionUiItem(id = "en", label = "English", isSelected = true),
            ),
          ),
          PlayerSettingUiItem(
            category = PlayerSettingCategory.Audio,
            selectedLabel = "Original",
            options = listOf(
              PlayerSettingOptionUiItem(id = "original", label = "Original", isSelected = true),
            ),
          ),
        ),
      ),
      restoredCategory = PlayerSettingCategory.Quality,
      isFocusEnabled = false,
      focusRequester = FocusRequester.Default,
      onCategorySelected = {},
      onBack = {},
      dismissOnLeft = true,
      modifier = Modifier
        .width(360.dp)
        .padding(20.dp),
    )
  }
}

@Preview(device = Devices.TV_720p, showBackground = true, backgroundColor = 0xFF171717)
@Composable
private fun PlayerSettingOptionsSectionPreview() {
  StreamTvTheme {
    PlayerSettingOptionsSection(
      item = PlayerSettingUiItem(
        category = PlayerSettingCategory.Quality,
        selectedLabel = "1080p",
        options = listOf(
          PlayerSettingOptionUiItem(id = "auto", label = "Auto", isSelected = false),
          PlayerSettingOptionUiItem(id = "1080", label = "1080p", isSelected = true),
          PlayerSettingOptionUiItem(id = "720", label = "720p", isSelected = false),
          PlayerSettingOptionUiItem(id = "480", label = "480p", isSelected = false),
        ),
      ),
      isFocusEnabled = false,
      focusRequester = FocusRequester.Default,
      onOptionSelected = {},
      onBack = {},
      dismissOnLeft = true,
      modifier = Modifier
        .width(315.dp)
        .padding(20.dp),
    )
  }
}
