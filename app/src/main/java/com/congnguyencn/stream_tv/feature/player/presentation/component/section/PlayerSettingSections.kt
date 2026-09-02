package com.congnguyencn.stream_tv.feature.player.presentation.component.section

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

private object PlayerSettingSectionDefaults {
  val ItemShape = RoundedCornerShape(8.dp)
  val ItemIconSize = 24.dp
  val TrailingIconSize = 16.dp
}

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
      .fillMaxSize()
      .handlePlayerSectionExit(onBack = onBack, dismissOnLeft = dismissOnLeft)
      .focusGroup(),
  ) {
    PlayerSectionHeader(title = stringResource(R.string.player_settings))
    Spacer(modifier = Modifier.size(16.dp))
    LazyColumn(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(4.dp),
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
      .fillMaxSize()
      .handlePlayerSectionExit(onBack = onBack, dismissOnLeft = dismissOnLeft)
      .focusGroup(),
  ) {
    PlayerSectionHeader(title = stringResource(item.category.titleResId()))
    Spacer(modifier = Modifier.size(20.dp))
    LazyColumn(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(8.dp),
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
      fontSize = 24.sp,
      lineHeight = 30.sp,
    ),
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
    modifier = modifier
      .fillMaxWidth()
      .focusProperties { canFocus = isFocusEnabled }
      .testTag("player-setting-${item.category.name.lowercase()}"),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
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
          style = StreamTvTheme.typography.bodyLarge.copy(lineHeight = 20.sp),
        )
        Text(
          text = item.selectedLabel,
          color = LocalContentColor.current.copy(alpha = 0.68f),
          style = StreamTvTheme.typography.labelMedium.copy(
            fontSize = 12.sp,
            lineHeight = 16.sp,
          ),
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
    modifier = modifier
      .fillMaxWidth()
      .focusProperties { canFocus = isFocusEnabled }
      .testTag("player-setting-option-${option.id}"),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = option.label,
        modifier = Modifier.weight(1f),
        style = StreamTvTheme.typography.bodyLarge.copy(lineHeight = 20.sp),
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
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit,
) {
  Surface(
    onClick = onClick,
    modifier = modifier,
    shape = ClickableSurfaceDefaults.shape(shape = PlayerSettingSectionDefaults.ItemShape),
    scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
    colors = ClickableSurfaceDefaults.colors(
      containerColor = StreamTvColors.Transparent,
      contentColor = StreamTvColors.Neutral10,
      focusedContainerColor = StreamTvColors.NeutralWhite,
      focusedContentColor = StreamTvColors.NeutralBlack,
      pressedContainerColor = StreamTvColors.Primary60,
      pressedContentColor = StreamTvColors.NeutralWhite,
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
