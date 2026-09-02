package com.congnguyencn.stream_tv.core.designsystem.component.contentrow

import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.foundation.lazy.layout.LazyLayoutMeasurePolicy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * A horizontal lazy layout controlled by one fixed focus target.
 *
 * Items move underneath [selectedItem] while that overlay stays at the leading content edge.
 * Previous items slide out beyond that edge instead of reserving an empty card-sized slot.
 * Collections with more than five items append one duplicate cycle for a seamless forward loop;
 * smaller collections stop at their final item. Set [loopingEnabled] to false for ordered rows
 * whose first and last positions have meaning, such as popularity rankings.
 * Item content must not add its own focus target.
 */
@Composable
fun ContentRow(
  modifier: Modifier = Modifier,
  state: ContentRowState = rememberContentRowState(),
  contentPadding: PaddingValues = ContentRowDefaults.ContentPadding,
  itemSpacing: Dp = ContentRowDefaults.ItemSpacing,
  selectedItemContentPadding: Dp = ContentRowDefaults.SelectedItemContentPadding,
  verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
  enabled: Boolean = true,
  loopingEnabled: Boolean = true,
  selectedItemModifier: Modifier = Modifier,
  onSelectedItemClick: (index: Int) -> Unit = {},
  selectedItem: @Composable (isFocused: Boolean) -> Unit = { isFocused ->
    ContentRowDefaults.SelectedItem(isFocused = isFocused)
  },
  content: ContentRowScope.() -> Unit,
) {
  val itemProvider = remember(content) {
    ContentRowScopeImpl().apply(content).build()
  }
  val loopingItemProvider = remember(itemProvider, loopingEnabled) {
    LoopingContentRowItemProvider(
      source = itemProvider,
      loopingEnabled = loopingEnabled,
    )
  }
  val interactionSource = remember { MutableInteractionSource() }
  val isFocused by interactionSource.collectIsFocusedAsState()
  val coroutineScope = rememberCoroutineScope()

  SideEffect {
    state.updateItemCount(
      newItemCount = itemProvider.itemCount,
      loopingEnabled = loopingEnabled,
    )
  }

  Box(modifier = modifier.fillMaxWidth()) {
    LazyLayout(
      itemProvider = { loopingItemProvider },
      modifier = Modifier.fillMaxWidth(),
      measurePolicy = rememberContentRowMeasurePolicy(
        itemProvider = loopingItemProvider,
        realItemCount = itemProvider.itemCount,
        state = state,
        contentPadding = contentPadding,
        itemSpacing = itemSpacing,
        selectedItemContentPadding = selectedItemContentPadding,
        verticalAlignment = verticalAlignment,
      ),
    )

    if (itemProvider.itemCount > 0) {
      ContentRowSelectionOverlay(
        bounds = state.selectionBounds,
        modifier = Modifier.matchParentSize(),
        selectedItemModifier = selectedItemModifier
          .onPreviewKeyEvent { event ->
            handleContentRowKeyEvent(
              event = event,
              state = state,
              enabled = enabled,
              coroutineScope = coroutineScope,
              onSelectedItemClick = onSelectedItemClick,
            )
          }
          .focusProperties { left = FocusRequester.Default }
          .focusable(
            enabled = enabled,
            interactionSource = interactionSource,
          ),
      ) {
        selectedItem(isFocused)
      }
    }
  }
}

private fun handleContentRowKeyEvent(
  event: KeyEvent,
  state: ContentRowState,
  enabled: Boolean,
  coroutineScope: CoroutineScope,
  onSelectedItemClick: (index: Int) -> Unit,
): Boolean {
  val isKeyDown = event.type == KeyEventType.KeyDown
  val isActionable = isKeyDown && enabled && !state.isScrollInProgress

  return when (event.key) {
    // At the first item the event stays unconsumed so focus can leave the row.
    Key.DirectionLeft -> if (isKeyDown && state.selectedIndex == 0) {
      false
    } else {
      if (isActionable) {
        coroutineScope.launch { state.moveSelection(-1) }
      }
      true
    }

    Key.DirectionRight -> {
      if (isActionable) {
        coroutineScope.launch { state.moveSelection(1) }
      }
      true
    }

    Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
      if (isActionable) {
        onSelectedItemClick(state.selectedIndex)
      }
      true
    }

    else -> false
  }
}

@Composable
private fun ContentRowSelectionOverlay(
  bounds: ContentRowSelectionBounds,
  modifier: Modifier,
  selectedItemModifier: Modifier,
  content: @Composable () -> Unit,
) {
  Layout(
    content = {
      Box(modifier = selectedItemModifier) {
        content()
      }
    },
    modifier = modifier,
  ) { measurables, constraints ->
    val width = constraints.maxWidth.takeIf { it != Constraints.Infinity } ?: bounds.width
    val height = constraints.maxHeight.takeIf { it != Constraints.Infinity } ?: bounds.height
    val placeable = measurables.singleOrNull()?.measure(
      Constraints.fixed(
        width = bounds.width.coerceAtMost(width),
        height = bounds.height.coerceAtMost(height),
      ),
    )

    layout(width, height) {
      placeable?.placeRelative(
        x = bounds.left,
        y = bounds.top,
      )
    }
  }
}

@Composable
private fun rememberContentRowMeasurePolicy(
  itemProvider: LoopingContentRowItemProvider,
  realItemCount: Int,
  state: ContentRowState,
  contentPadding: PaddingValues,
  itemSpacing: Dp,
  selectedItemContentPadding: Dp,
  verticalAlignment: Alignment.Vertical,
): LazyLayoutMeasurePolicy = remember(
  itemProvider,
  realItemCount,
  state,
  contentPadding,
  itemSpacing,
  selectedItemContentPadding,
  verticalAlignment,
) {
  LazyLayoutMeasurePolicy { constraints ->
    require(constraints.hasBoundedWidth) { "ContentRow requires a bounded width" }

    if (realItemCount == 0) {
      return@LazyLayoutMeasurePolicy layout(
        width = constraints.constrainWidth(0),
        height = constraints.constrainHeight(0),
      ) {}
    }

    val leftPadding = contentPadding.calculateLeftPadding(layoutDirection).roundToPx()
    val rightPadding = contentPadding.calculateRightPadding(layoutDirection).roundToPx()
    val topPadding = contentPadding.calculateTopPadding().roundToPx()
    val bottomPadding = contentPadding.calculateBottomPadding().roundToPx()
    val spacingPx = itemSpacing.roundToPx()
    val selectionPaddingPx = selectedItemContentPadding.roundToPx()
    val availableItemHeight = if (constraints.hasBoundedHeight) {
      (
        constraints.maxHeight -
          topPadding -
          bottomPadding -
          selectionPaddingPx * 2
        ).coerceAtLeast(0)
    } else {
      Constraints.Infinity
    }
    val itemConstraints = Constraints(
      minWidth = 0,
      maxWidth = Constraints.Infinity,
      minHeight = 0,
      maxHeight = availableItemHeight,
    )
    val selectedIndex = state.selectedIndex.coerceIn(0, realItemCount - 1)
    val selectedVirtualIndex = itemProvider.anchorIndex(selectedIndex)
    val measuredByIndex = mutableMapOf<Int, MeasuredContentRowItem>()

    fun measureItem(index: Int): MeasuredContentRowItem = measuredByIndex.getOrPut(index) {
      val placeables = compose(index).map { measurable -> measurable.measure(itemConstraints) }
      MeasuredContentRowItem(
        placeables = placeables,
        width = placeables.sumOf(Placeable::width),
        height = placeables.maxOfOrNull(Placeable::height) ?: 0,
      )
    }

    val selected = measureItem(selectedVirtualIndex)
    val layoutWidth = constraints.maxWidth
    val desiredSelectedX = leftPadding + selectionPaddingPx
    val maximumSelectedX = (
      layoutWidth - rightPadding - selected.width - selectionPaddingPx
      ).coerceAtLeast(leftPadding + selectionPaddingPx)
    val selectedBaseX = desiredSelectedX.coerceAtMost(maximumSelectedX)
    val placedItems = mutableListOf(
      PositionedContentRowItem(selected, selectedBaseX),
    )
    placedItems += positionContentRowItemsAfterSelection(
      firstIndex = selectedVirtualIndex + 1,
      firstX = selectedBaseX + selected.width + spacingPx,
      itemCount = itemProvider.itemCount,
      viewportEnd = layoutWidth - rightPadding,
      spacingPx = spacingPx,
      measureItem = ::measureItem,
    )
    placedItems += positionContentRowItemsBeforeSelection(
      firstIndex = selectedVirtualIndex - 1,
      firstRight = selectedBaseX - spacingPx,
      viewportStart = leftPadding,
      spacingPx = spacingPx,
      measureItem = ::measureItem,
    )

    val next = measuredByIndex[selectedVirtualIndex + 1]
    val previous = measuredByIndex[selectedVirtualIndex - 1]
    val contentHeight = measuredByIndex.values.maxOfOrNull(MeasuredContentRowItem::height) ?: 0
    val layoutHeight = constraints.constrainHeight(
      topPadding + selectionPaddingPx * 2 + contentHeight + bottomPadding,
    )
    val availableHeight = (
      layoutHeight - topPadding - bottomPadding - selectionPaddingPx * 2
      ).coerceAtLeast(0)
    val selectedY = topPadding + selectionPaddingPx +
      verticalAlignment.align(selected.height, availableHeight)

    state.updateLayoutInfo(
      bounds = ContentRowSelectionBounds(
        left = selectedBaseX - selectionPaddingPx,
        top = selectedY - selectionPaddingPx,
        width = selected.width + selectionPaddingPx * 2,
        height = selected.height + selectionPaddingPx * 2,
      ),
      forwardStepPx = next?.let { (selected.width + it.width) / 2f + spacingPx } ?: 0f,
      backwardStepPx = previous?.let { (selected.width + it.width) / 2f + spacingPx } ?: 0f,
    )

    layout(layoutWidth, layoutHeight) {
      val animationOffset = state.animationOffsetPx.roundToInt()
      placedItems.forEach { positionedItem ->
        val itemY = topPadding + selectionPaddingPx + verticalAlignment.align(
          positionedItem.item.height,
          availableHeight,
        )
        var placeableX = positionedItem.x + animationOffset
        positionedItem.item.placeables.forEach { placeable ->
          placeable.placeRelative(placeableX, itemY)
          placeableX += placeable.width
        }
      }
    }
  }
}

private data class MeasuredContentRowItem(val placeables: List<Placeable>, val width: Int, val height: Int)

private data class PositionedContentRowItem(val item: MeasuredContentRowItem, val x: Int)

private fun positionContentRowItemsAfterSelection(
  firstIndex: Int,
  firstX: Int,
  itemCount: Int,
  viewportEnd: Int,
  spacingPx: Int,
  measureItem: (index: Int) -> MeasuredContentRowItem,
): List<PositionedContentRowItem> {
  val positionedItems = mutableListOf<PositionedContentRowItem>()
  var index = firstIndex
  var cursor = firstX

  while (index < itemCount && (positionedItems.isEmpty() || cursor < viewportEnd)) {
    val item = measureItem(index)
    positionedItems += PositionedContentRowItem(item, cursor)
    cursor += item.width + spacingPx
    index += 1
  }
  repeat(ContentRowDefaults.BeyondBoundsItemCount) {
    if (index < itemCount) {
      val item = measureItem(index)
      positionedItems += PositionedContentRowItem(item, cursor)
      cursor += item.width + spacingPx
      index += 1
    }
  }
  return positionedItems
}

private fun positionContentRowItemsBeforeSelection(
  firstIndex: Int,
  firstRight: Int,
  viewportStart: Int,
  spacingPx: Int,
  measureItem: (index: Int) -> MeasuredContentRowItem,
): List<PositionedContentRowItem> {
  val positionedItems = mutableListOf<PositionedContentRowItem>()
  var index = firstIndex
  var cursor = firstRight

  while (index >= 0 && (positionedItems.isEmpty() || cursor > viewportStart)) {
    val item = measureItem(index)
    cursor -= item.width
    positionedItems += PositionedContentRowItem(item, cursor)
    cursor -= spacingPx
    index -= 1
  }
  repeat(ContentRowDefaults.BeyondBoundsItemCount) {
    if (index >= 0) {
      val item = measureItem(index)
      cursor -= item.width
      positionedItems += PositionedContentRowItem(item, cursor)
      cursor -= spacingPx
      index -= 1
    }
  }
  return positionedItems
}
