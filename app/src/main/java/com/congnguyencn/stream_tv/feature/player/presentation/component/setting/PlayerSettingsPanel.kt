package com.congnguyencn.stream_tv.feature.player.presentation.component.setting

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingCategory
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingOptionUiItem
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingUiItem
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingsUiState

private object PlayerSettingsPanelDefaults {
  const val AnimationDurationMillis = 240
  val Shape = RoundedCornerShape(16.dp)
  val ContentPadding = 20.dp
  val ItemShape = RoundedCornerShape(10.dp)
  val ItemIconSize = 24.dp
}

/**
 * Shared Settings tree used unchanged by both player orientations.
 *
 * The root remains composed underneath a child, preserving its focus requester and lazy-list state.
 * Back therefore pops one layer and reliably restores the row that opened the child.
 */
@Composable
internal fun PlayerSettingsPanel(
  settings: PlayerSettingsUiState,
  navigationState: PlayerSettingsNavigationState,
  onQualitySelected: (String) -> Unit,
  onSubtitleSelected: (String) -> Unit,
  onAudioSelected: (String) -> Unit,
  onDismissed: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val rootRequesters = remember(settings.items.map(PlayerSettingUiItem::category)) {
    settings.items.associate { item -> item.category to FocusRequester() }
  }
  val childFocusRequester = remember { FocusRequester() }
  LaunchedEffect(settings) {
    val wasOpen = navigationState.isVisible
    navigationState.sync(settings)
    if (wasOpen && !navigationState.isVisible) onDismissed()
  }

  LaunchedEffect(navigationState.isVisible, navigationState.activeCategory, settings) {
    if (!navigationState.isVisible) return@LaunchedEffect

    withFrameMillis { }
    val activeCategory = navigationState.activeCategory
    if (activeCategory == null) {
      val target = navigationState.rootFocusCategory ?: settings.items.firstOrNull()?.category
      target?.let { category -> rootRequesters[category]?.requestFocus() }
    } else {
      childFocusRequester.requestFocus()
    }
  }

  val navigateBack = {
    val closesPanel = navigationState.activeCategory == null
    navigationState.pop()
    if (closesPanel) onDismissed()
  }

  BackHandler(enabled = navigationState.isVisible, onBack = navigateBack)

  AnimatedVisibility(
    visible = navigationState.isVisible,
    modifier = modifier,
    enter = slideInHorizontally(
      animationSpec = tween(
        durationMillis = PlayerSettingsPanelDefaults.AnimationDurationMillis,
        easing = FastOutSlowInEasing,
      ),
      initialOffsetX = { width -> width },
    ) + fadeIn(),
    exit = slideOutHorizontally(
      animationSpec = tween(
        durationMillis = PlayerSettingsPanelDefaults.AnimationDurationMillis,
        easing = FastOutSlowInEasing,
      ),
      targetOffsetX = { width -> width },
    ) + fadeOut(),
  ) {
    SettingsPanelSurface(
      settings = settings,
      navigationState = navigationState,
      rootRequesters = rootRequesters,
      childFocusRequester = childFocusRequester,
      onQualitySelected = onQualitySelected,
      onSubtitleSelected = onSubtitleSelected,
      onAudioSelected = onAudioSelected,
      onBack = navigateBack,
    )
  }
}

@Composable
private fun SettingsPanelSurface(
  settings: PlayerSettingsUiState,
  navigationState: PlayerSettingsNavigationState,
  rootRequesters: Map<PlayerSettingCategory, FocusRequester>,
  childFocusRequester: FocusRequester,
  onQualitySelected: (String) -> Unit,
  onSubtitleSelected: (String) -> Unit,
  onAudioSelected: (String) -> Unit,
  onBack: () -> Unit,
) {
  val activeCategory = navigationState.activeCategory
  Surface(
    modifier = Modifier
      .fillMaxSize()
      .testTag("player-settings-panel"),
    shape = PlayerSettingsPanelDefaults.Shape,
    colors = SurfaceDefaults.colors(containerColor = StreamTvColors.Neutral100),
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      SettingsRoot(
        settings = settings,
        focusRequesters = rootRequesters,
        isFocusEnabled = navigationState.isVisible && activeCategory == null,
        onCategorySelected = { category -> navigationState.openCategory(category, settings) },
        onBack = onBack,
        modifier = Modifier
          .fillMaxSize()
          .alpha(if (activeCategory == null) 1f else 0f)
          .then(hiddenSettingRootSemantics(activeCategory)),
      )

      activeCategory?.let { category ->
        val item = settings.item(category) ?: return@let
        SettingsOptions(
          item = item,
          firstFocusRequester = childFocusRequester,
          onOptionSelected = { option ->
            selectSettingOption(
              category = category,
              optionId = option.id,
              onQualitySelected = onQualitySelected,
              onSubtitleSelected = onSubtitleSelected,
              onAudioSelected = onAudioSelected,
            )
          },
          onBack = onBack,
          modifier = Modifier.fillMaxSize(),
        )
      }
    }
  }
}

private fun hiddenSettingRootSemantics(activeCategory: PlayerSettingCategory?): Modifier =
  if (activeCategory == null) Modifier else Modifier.clearAndSetSemantics { }

private fun selectSettingOption(
  category: PlayerSettingCategory,
  optionId: String,
  onQualitySelected: (String) -> Unit,
  onSubtitleSelected: (String) -> Unit,
  onAudioSelected: (String) -> Unit,
) {
  when (category) {
    PlayerSettingCategory.Quality -> onQualitySelected(optionId)
    PlayerSettingCategory.Subtitles -> onSubtitleSelected(optionId)
    PlayerSettingCategory.Audio -> onAudioSelected(optionId)
  }
}

@Composable
private fun SettingsRoot(
  settings: PlayerSettingsUiState,
  focusRequesters: Map<PlayerSettingCategory, FocusRequester>,
  isFocusEnabled: Boolean,
  onCategorySelected: (PlayerSettingCategory) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .focusProperties { canFocus = isFocusEnabled }
      .focusGroup()
      .handleSettingsBack(onBack)
      .padding(PlayerSettingsPanelDefaults.ContentPadding),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    SettingsHeader(title = stringResource(R.string.player_settings))
    LazyColumn(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      items(
        items = settings.items,
        key = { item -> item.category.name },
      ) { item ->
        SettingRootItem(
          item = item,
          focusRequester = focusRequesters.getValue(item.category),
          isFocusEnabled = isFocusEnabled,
          onClick = { onCategorySelected(item.category) },
        )
      }
    }
  }
}

@Composable
private fun SettingsOptions(
  item: PlayerSettingUiItem,
  firstFocusRequester: FocusRequester,
  onOptionSelected: (PlayerSettingOptionUiItem) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .focusGroup()
      .handleSettingsBack(onBack)
      .padding(PlayerSettingsPanelDefaults.ContentPadding),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    SettingsHeader(title = stringResource(item.category.titleResId()))
    val focusIndex = item.options.indexOfFirst(PlayerSettingOptionUiItem::isSelected).coerceAtLeast(0)
    LazyColumn(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      itemsIndexed(
        items = item.options,
        key = { _, option -> option.id },
      ) { index, option ->
        SettingOptionItem(
          option = option,
          onClick = { onOptionSelected(option) },
          modifier = if (index == focusIndex) {
            Modifier.focusRequester(firstFocusRequester)
          } else {
            Modifier
          },
        )
      }
    }
  }
}

@Composable
private fun SettingsHeader(title: String) {
  Text(
    text = title,
    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
    color = StreamTvColors.NeutralWhite,
    style = StreamTvTheme.typography.headlineLarge,
  )
}

@Composable
private fun SettingRootItem(
  item: PlayerSettingUiItem,
  focusRequester: FocusRequester,
  isFocusEnabled: Boolean,
  onClick: () -> Unit,
) {
  SettingsClickableSurface(
    onClick = onClick,
    modifier = Modifier
      .fillMaxWidth()
      .focusRequester(focusRequester)
      .focusProperties { canFocus = isFocusEnabled }
      .testTag("player-setting-${item.category.name.lowercase()}"),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = ImageVector.vectorResource(item.category.iconResId()),
        contentDescription = null,
        modifier = Modifier.size(PlayerSettingsPanelDefaults.ItemIconSize),
        tint = LocalContentColor.current,
      )
      Spacer(modifier = Modifier.width(16.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = stringResource(item.category.titleResId()),
          style = StreamTvTheme.typography.bodyLarge,
        )
        Text(
          text = item.selectedLabel,
          color = LocalContentColor.current.copy(alpha = 0.68f),
          style = StreamTvTheme.typography.labelMedium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      Icon(
        imageVector = ImageVector.vectorResource(R.drawable.ic_player_chevron_right),
        contentDescription = null,
        modifier = Modifier.size(PlayerSettingsPanelDefaults.ItemIconSize),
        tint = LocalContentColor.current,
      )
    }
  }
}

@Composable
private fun SettingOptionItem(option: PlayerSettingOptionUiItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
  SettingsClickableSurface(
    onClick = onClick,
    modifier = modifier
      .fillMaxWidth()
      .testTag("player-setting-option-${option.id}"),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = option.label,
        modifier = Modifier.weight(1f),
        style = StreamTvTheme.typography.bodyLarge,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      if (option.isSelected) {
        Icon(
          imageVector = ImageVector.vectorResource(R.drawable.ic_player_check),
          contentDescription = stringResource(R.string.player_setting_selected),
          modifier = Modifier.size(PlayerSettingsPanelDefaults.ItemIconSize),
          tint = LocalContentColor.current,
        )
      }
    }
  }
}

@Composable
private fun SettingsClickableSurface(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit,
) {
  Surface(
    onClick = onClick,
    modifier = modifier,
    shape = ClickableSurfaceDefaults.shape(shape = PlayerSettingsPanelDefaults.ItemShape),
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

private fun Modifier.handleSettingsBack(onBack: () -> Unit): Modifier = onPreviewKeyEvent { event ->
  val isBackKey = event.key == Key.Back || event.key == Key.Escape || event.key == Key.DirectionLeft
  if (event.type == KeyEventType.KeyDown && isBackKey) {
    onBack()
    true
  } else {
    false
  }
}

private fun PlayerSettingCategory.titleResId(): Int = when (this) {
  PlayerSettingCategory.Quality -> R.string.player_setting_quality
  PlayerSettingCategory.Subtitles -> R.string.player_setting_subtitles
  PlayerSettingCategory.Audio -> R.string.player_setting_audio
}

private fun PlayerSettingCategory.iconResId(): Int = when (this) {
  PlayerSettingCategory.Quality -> R.drawable.ic_player_quality
  PlayerSettingCategory.Subtitles -> R.drawable.ic_player_subtitles
  PlayerSettingCategory.Audio -> R.drawable.ic_player_audio
}
