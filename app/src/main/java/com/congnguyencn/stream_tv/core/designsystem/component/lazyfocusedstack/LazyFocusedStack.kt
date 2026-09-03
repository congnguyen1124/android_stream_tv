package com.congnguyencn.stream_tv.core.designsystem.component.lazyfocusedstack

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.foundation.lazy.layout.LazyLayoutMeasurePolicy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvSurface
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * A two-dimensional time stack controlled by one focus target.
 *
 * The program grid is lazy in both axes. Channel headers and the time ruler are owned by this
 * component so they cannot drift out of sync with the grid. Items immediately outside the viewport
 * plus every possible next D-pad target are composed ahead of time.
 */
@Composable
@Suppress("LongMethod")
fun <C, T> LazyFocusedStack(
  columns: List<LazyFocusedStackColumn<C, T>>,
  modifier: Modifier = Modifier,
  state: LazyFocusedStackState = rememberLazyFocusedStackState(),
  startMinute: Int = 0,
  endMinute: Int = 24 * 60,
  columnWidth: Dp = LazyFocusedStackDefaults.ColumnWidth,
  columnSpacing: Dp = LazyFocusedStackDefaults.ColumnSpacing,
  headerHeight: Dp = LazyFocusedStackDefaults.HeaderHeight,
  timeRulerWidth: Dp = LazyFocusedStackDefaults.TimeRulerWidth,
  hourHeight: Dp = LazyFocusedStackDefaults.HourHeight,
  itemVerticalSpacing: Dp = LazyFocusedStackDefaults.ItemVerticalSpacing,
  selectedItemPadding: Dp = LazyFocusedStackDefaults.SelectedItemPadding,
  enabled: Boolean = true,
  selectedItemModifier: Modifier = Modifier,
  onSelectedItemClick: (columnIndex: Int, itemIndex: Int) -> Unit = { _, _ -> },
  leadingHeader: @Composable () -> Unit,
  columnHeader: @Composable (column: C) -> Unit,
  timeLabel: @Composable (minute: Int) -> Unit,
  itemContent: @Composable (item: T, isSelected: Boolean) -> Unit,
  selectedItem: @Composable (isFocused: Boolean) -> Unit = { isFocused ->
    LazyFocusedStackDefaults.SelectedItem(isFocused = isFocused)
  },
) {
  require(endMinute > startMinute) { "LazyFocusedStack endMinute must be after startMinute" }
  val normalizedColumns = remember(columns, startMinute, endMinute) {
    columns.map { column ->
      column.copy(
        items = column.items
          .filter { item -> item.startMinute < endMinute && item.endMinute > startMinute }
          .sortedWith(compareBy(LazyFocusedStackItem<T>::startMinute).thenBy(LazyFocusedStackItem<T>::endMinute)),
      )
    }
  }
  val navigationColumns = remember(normalizedColumns) {
    normalizedColumns.map { column ->
      column.items.map { item ->
        LazyFocusedStackItemRange(
          startMinute = item.startMinute.coerceAtLeast(startMinute),
          endMinute = item.endMinute.coerceAtMost(endMinute),
        )
      }
    }
  }
  val itemProvider = remember(normalizedColumns, state, itemContent) {
    LazyFocusedStackItemProvider(
      columns = normalizedColumns,
      state = state,
      itemContent = itemContent,
    )
  }
  val interactionSource = remember { MutableInteractionSource() }
  val isFocused by interactionSource.collectIsFocusedAsState()
  val coroutineScope = rememberCoroutineScope()

  Layout(
    modifier = modifier,
    content = {
      Box(modifier = Modifier.fillMaxSize()) {
        LazyLayout(
          itemProvider = { itemProvider },
          modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .drawLazyFocusedStackGrid(
              state = state,
              columnCount = normalizedColumns.size,
              startMinute = startMinute,
              endMinute = endMinute,
              columnWidth = columnWidth,
              columnSpacing = columnSpacing,
              hourHeight = hourHeight,
            ),
          measurePolicy = rememberLazyFocusedStackMeasurePolicy(
            itemProvider = itemProvider,
            navigationColumns = navigationColumns,
            state = state,
            startMinute = startMinute,
            endMinute = endMinute,
            columnWidth = columnWidth,
            columnSpacing = columnSpacing,
            hourHeight = hourHeight,
            itemVerticalSpacing = itemVerticalSpacing,
            selectedItemPadding = selectedItemPadding,
          ),
        )

        if (itemProvider.itemCount > 0) {
          LazyFocusedStackSelectionOverlay(
            bounds = state.selectionBounds,
            modifier = Modifier.fillMaxSize(),
            selectedItemModifier = selectedItemModifier
              .onPreviewKeyEvent { event ->
                handleLazyFocusedStackKeyEvent(
                  event = event,
                  state = state,
                  enabled = enabled,
                  coroutineScope = coroutineScope,
                  onSelectedItemClick = onSelectedItemClick,
                )
              }
              .focusProperties {
                left = FocusRequester.Default
                right = FocusRequester.Default
                down = FocusRequester.Default
              }
              .focusable(enabled = enabled, interactionSource = interactionSource),
          ) {
            selectedItem(isFocused)
          }
        }
      }

      LazyFocusedStackHeaders(
        columns = normalizedColumns,
        state = state,
        columnWidth = columnWidth,
        columnSpacing = columnSpacing,
        modifier = Modifier.fillMaxSize(),
        columnHeader = columnHeader,
      )
      LazyFocusedStackTimeRuler(
        startMinute = startMinute,
        endMinute = endMinute,
        state = state,
        hourHeight = hourHeight,
        modifier = Modifier.fillMaxSize(),
        timeLabel = timeLabel,
      )
      Box(modifier = Modifier.fillMaxSize()) {
        leadingHeader()
      }
    },
  ) { measurables, constraints ->
    require(constraints.hasBoundedWidth && constraints.hasBoundedHeight) {
      "LazyFocusedStack requires bounded width and height"
    }
    val layoutWidth = constraints.maxWidth
    val layoutHeight = constraints.maxHeight
    val headerHeightPx = headerHeight.roundToPx().coerceAtMost(layoutHeight)
    val timeRulerWidthPx = timeRulerWidth.roundToPx().coerceAtMost(layoutWidth)
    val gridWidth = (layoutWidth - timeRulerWidthPx).coerceAtLeast(0)
    val gridHeight = (layoutHeight - headerHeightPx).coerceAtLeast(0)

    val grid = measurables[0].measure(Constraints.fixed(gridWidth, gridHeight))
    val headers = measurables[1].measure(Constraints.fixed(gridWidth, headerHeightPx))
    val timeRuler = measurables[2].measure(Constraints.fixed(timeRulerWidthPx, gridHeight))
    val corner = measurables[3].measure(Constraints.fixed(timeRulerWidthPx, headerHeightPx))

    layout(layoutWidth, layoutHeight) {
      grid.placeRelative(timeRulerWidthPx, headerHeightPx)
      headers.placeRelative(timeRulerWidthPx, 0)
      timeRuler.placeRelative(0, headerHeightPx)
      corner.placeRelative(0, 0)
    }
  }
}

private fun handleLazyFocusedStackKeyEvent(
  event: KeyEvent,
  state: LazyFocusedStackState,
  enabled: Boolean,
  coroutineScope: CoroutineScope,
  onSelectedItemClick: (columnIndex: Int, itemIndex: Int) -> Unit,
): Boolean {
  val direction = when (event.key) {
    Key.DirectionUp -> LazyFocusedStackDirection.Up
    Key.DirectionDown -> LazyFocusedStackDirection.Down
    Key.DirectionLeft -> LazyFocusedStackDirection.Left
    Key.DirectionRight -> LazyFocusedStackDirection.Right
    else -> null
  }
  if (direction != null) {
    if (!enabled) return false
    if (state.isScrollInProgress) return true
    val target = state.target(direction) ?: return false
    if (event.type == KeyEventType.KeyDown) {
      coroutineScope.launch { state.moveSelection(direction) }
    }
    return target != state.selectedPosition
  }

  return when (event.key) {
    Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
      if (event.type == KeyEventType.KeyDown && enabled && !state.isScrollInProgress) {
        onSelectedItemClick(
          state.selectedPosition.columnIndex,
          state.selectedPosition.itemIndex,
        )
      }
      true
    }

    else -> false
  }
}

@Composable
private fun LazyFocusedStackSelectionOverlay(
  bounds: LazyFocusedStackSelectionBounds,
  modifier: Modifier,
  selectedItemModifier: Modifier,
  content: @Composable () -> Unit,
) {
  Layout(
    modifier = modifier,
    content = {
      Box(modifier = selectedItemModifier) { content() }
    },
  ) { measurables, constraints ->
    val width = constraints.maxWidth
    val height = constraints.maxHeight
    val placeable = measurables.single().measure(
      Constraints.fixed(
        width = bounds.width.coerceIn(0, width),
        height = bounds.height.coerceIn(0, height),
      ),
    )
    layout(width, height) {
      placeable.placeRelative(
        x = bounds.left.coerceIn(0, (width - placeable.width).coerceAtLeast(0)),
        y = bounds.top.coerceIn(0, (height - placeable.height).coerceAtLeast(0)),
      )
    }
  }
}

@Composable
private fun <C, T> LazyFocusedStackHeaders(
  columns: List<LazyFocusedStackColumn<C, T>>,
  state: LazyFocusedStackState,
  columnWidth: Dp,
  columnSpacing: Dp,
  modifier: Modifier,
  columnHeader: @Composable (column: C) -> Unit,
) {
  Layout(
    modifier = modifier
      .clipToBounds()
      .drawBehind {
        val strokeWidth = 1.dp.toPx()
        drawLine(
          color = StreamTvColors.TransparentWhite20,
          start = androidx.compose.ui.geometry.Offset(0f, size.height - strokeWidth / 2f),
          end = androidx.compose.ui.geometry.Offset(size.width, size.height - strokeWidth / 2f),
          strokeWidth = strokeWidth,
        )
        val step = columnWidth.toPx() + columnSpacing.toPx()
        repeat(columns.size) { index ->
          val x = index * step + columnWidth.toPx() + state.horizontalOffsetPx
          if (x in 0f..size.width) {
            drawLine(
              color = StreamTvColors.TransparentWhite20,
              start = androidx.compose.ui.geometry.Offset(x, 0f),
              end = androidx.compose.ui.geometry.Offset(x, size.height),
              strokeWidth = strokeWidth,
            )
          }
        }
      },
    content = { columns.forEach { column -> columnHeader(column.header) } },
  ) { measurables, constraints ->
    val columnWidthPx = columnWidth.roundToPx()
    val stepPx = columnWidthPx + columnSpacing.roundToPx()
    val placeables = measurables.map { measurable ->
      measurable.measure(Constraints.fixed(columnWidthPx, constraints.maxHeight))
    }
    layout(constraints.maxWidth, constraints.maxHeight) {
      placeables.forEachIndexed { index, placeable ->
        val x = index * stepPx + state.horizontalOffsetPx.roundToInt()
        if (x < constraints.maxWidth && x + placeable.width > 0) {
          placeable.placeRelative(x, 0)
        }
      }
    }
  }
}

@Composable
private fun LazyFocusedStackTimeRuler(
  startMinute: Int,
  endMinute: Int,
  state: LazyFocusedStackState,
  hourHeight: Dp,
  modifier: Modifier,
  timeLabel: @Composable (minute: Int) -> Unit,
) {
  val firstHour = floor(startMinute / 60f).toInt()
  val lastHour = ceil(endMinute / 60f).toInt()
  val hours = remember(firstHour, lastHour) { (firstHour..lastHour).map { hour -> hour * 60 } }
  Layout(
    modifier = modifier
      .clipToBounds()
      .drawBehind {
        val strokeWidth = 1.dp.toPx()
        val verticalLineX = size.width - strokeWidth / 2f
        drawLine(
          color = StreamTvColors.TransparentWhite20,
          start = androidx.compose.ui.geometry.Offset(verticalLineX, 0f),
          end = androidx.compose.ui.geometry.Offset(verticalLineX, size.height),
          strokeWidth = strokeWidth,
        )
        val hourHeightPx = hourHeight.toPx()
        hours.forEach { minute ->
          val y = (minute - startMinute) / 60f * hourHeightPx + state.verticalOffsetPx
          if (y in 0f..size.height) {
            drawLine(
              color = StreamTvColors.TransparentWhite20,
              start = androidx.compose.ui.geometry.Offset(size.width - 10.dp.toPx(), y),
              end = androidx.compose.ui.geometry.Offset(size.width, y),
              strokeWidth = strokeWidth,
            )
          }
        }
      },
    content = { hours.forEach { minute -> timeLabel(minute) } },
  ) { measurables, constraints ->
    val hourHeightPx = hourHeight.roundToPx()
    val labelHeight = 24.dp.roundToPx()
    val contentHeightPx = ((endMinute - startMinute) / 60f * hourHeightPx).roundToInt()
    val minimumVerticalOffset = (constraints.maxHeight - contentHeightPx).coerceAtMost(0)
    val placeables = measurables.map { measurable ->
      measurable.measure(Constraints.fixed(constraints.maxWidth, labelHeight))
    }
    layout(constraints.maxWidth, constraints.maxHeight) {
      placeables.forEachIndexed { index, placeable ->
        val minute = hours[index]
        val markerY = ((minute - startMinute) / 60f * hourHeightPx).roundToInt() +
          state.verticalOffsetPx.roundToInt() - placeable.height / 2
        val y = when {
          minute == startMinute && markerY < 0 && state.verticalOffsetPx == 0f -> 0

          minute == endMinute &&
            state.verticalOffsetPx.roundToInt() <= minimumVerticalOffset &&
            markerY + placeable.height > constraints.maxHeight ->
            constraints.maxHeight - placeable.height

          else -> markerY
        }
        if (y < constraints.maxHeight && y + placeable.height > 0) {
          placeable.placeRelative(0, y)
        }
      }
    }
  }
}

@Composable
private fun <C, T> rememberLazyFocusedStackMeasurePolicy(
  itemProvider: LazyFocusedStackItemProvider<C, T>,
  navigationColumns: List<List<LazyFocusedStackItemRange>>,
  state: LazyFocusedStackState,
  startMinute: Int,
  endMinute: Int,
  columnWidth: Dp,
  columnSpacing: Dp,
  hourHeight: Dp,
  itemVerticalSpacing: Dp,
  selectedItemPadding: Dp,
): LazyLayoutMeasurePolicy = remember(
  itemProvider,
  navigationColumns,
  state,
  startMinute,
  endMinute,
  columnWidth,
  columnSpacing,
  hourHeight,
  itemVerticalSpacing,
  selectedItemPadding,
) {
  LazyLayoutMeasurePolicy { constraints ->
    require(constraints.hasBoundedWidth && constraints.hasBoundedHeight) {
      "LazyFocusedStack grid requires bounded width and height"
    }
    val viewportWidth = constraints.maxWidth
    val viewportHeight = constraints.maxHeight
    val columnWidthPx = columnWidth.roundToPx()
    val columnSpacingPx = columnSpacing.roundToPx()
    val columnStepPx = columnWidthPx + columnSpacingPx
    val hourHeightPx = hourHeight.roundToPx()
    val minuteHeightPx = hourHeightPx / 60f
    val verticalSpacingPx = itemVerticalSpacing.roundToPx()
    val selectionPaddingPx = selectedItemPadding.roundToPx()
    val contentWidth = (
      itemProvider.columnCount * columnStepPx - columnSpacingPx
      ).coerceAtLeast(0)
    val contentHeight = ((endMinute - startMinute) * minuteHeightPx).roundToInt()
    val minHorizontalOffset = (viewportWidth - contentWidth).coerceAtMost(0).toFloat()
    val minVerticalOffset = (viewportHeight - contentHeight).coerceAtMost(0).toFloat()

    val placements = buildMap {
      itemProvider.items.forEach { item ->
        val logicalX = item.position.columnIndex * columnStepPx
        val logicalY = ((item.startMinute - startMinute) * minuteHeightPx).roundToInt()
        val itemHeight = max(
          1,
          (
            (item.endMinute.coerceAtMost(endMinute) - item.startMinute.coerceAtLeast(startMinute)) *
              minuteHeightPx
            ).roundToInt() - verticalSpacingPx,
        )
        val horizontalOffset = (-logicalX).toFloat().coerceIn(minHorizontalOffset, 0f)
        val verticalOffset = (-logicalY).toFloat().coerceIn(minVerticalOffset, 0f)
        val itemLeft = logicalX + horizontalOffset.roundToInt()
        val itemTop = logicalY + verticalOffset.roundToInt()
        put(
          item.position,
          LazyFocusedStackPlacement(
            horizontalOffsetPx = horizontalOffset,
            verticalOffsetPx = verticalOffset,
            selectionBounds = LazyFocusedStackSelectionBounds(
              left = itemLeft - selectionPaddingPx,
              top = itemTop - selectionPaddingPx,
              width = columnWidthPx + selectionPaddingPx * 2,
              height = itemHeight + selectionPaddingPx * 2,
            ),
          ),
        )
      }
    }
    state.updateLayoutInfo(
      newLayoutInfo = LazyFocusedStackLayoutInfo(placements),
      newColumns = navigationColumns,
    )

    val criticalPositions = buildSet {
      add(state.selectedPosition)
      state.pendingPosition?.let(::add)
      LazyFocusedStackDirection.entries.forEach { direction ->
        lazyFocusedStackTarget(navigationColumns, state.selectedPosition, direction)?.let(::add)
      }
    }
    val beyondBoundsX = columnStepPx * LazyFocusedStackDefaults.BeyondBoundsColumnCount
    val beyondBoundsY = minuteHeightPx * LazyFocusedStackDefaults.BeyondBoundsMinuteCount
    val measuredItems = buildList {
      itemProvider.items.forEachIndexed { providerIndex, item ->
        val logicalX = item.position.columnIndex * columnStepPx
        val logicalY = ((item.startMinute - startMinute) * minuteHeightPx).roundToInt()
        val itemHeight = max(
          1,
          (
            (item.endMinute.coerceAtMost(endMinute) - item.startMinute.coerceAtLeast(startMinute)) *
              minuteHeightPx
            ).roundToInt() - verticalSpacingPx,
        )
        val screenX = logicalX + state.horizontalOffsetPx.roundToInt()
        val screenY = logicalY + state.verticalOffsetPx.roundToInt()
        val isInsideOverscan = screenX < viewportWidth + beyondBoundsX &&
          screenX + columnWidthPx > -beyondBoundsX &&
          screenY < viewportHeight + beyondBoundsY &&
          screenY + itemHeight > -beyondBoundsY
        if (isInsideOverscan || item.position in criticalPositions) {
          val placeables = compose(providerIndex).map { measurable ->
            measurable.measure(Constraints.fixed(columnWidthPx, itemHeight))
          }
          add(MeasuredLazyFocusedStackItem(item, placeables, logicalX, logicalY))
        }
      }
    }

    layout(viewportWidth, viewportHeight) {
      measuredItems.forEach { measured ->
        val x = measured.logicalX + state.horizontalOffsetPx.roundToInt()
        val y = measured.logicalY + state.verticalOffsetPx.roundToInt()
        measured.placeables.forEach { placeable -> placeable.placeRelative(x, y) }
      }
    }
  }
}

private class LazyFocusedStackItemProvider<C, T>(
  columns: List<LazyFocusedStackColumn<C, T>>,
  private val state: LazyFocusedStackState,
  private val itemContent: @Composable (item: T, isSelected: Boolean) -> Unit,
) : LazyLayoutItemProvider {
  val columnCount: Int = columns.size
  val items: List<ProvidedLazyFocusedStackItem<T>> = columns.flatMapIndexed { columnIndex, column ->
    column.items.mapIndexed { itemIndex, item ->
      ProvidedLazyFocusedStackItem(
        key = "lazy-focused-stack:${column.key}:${item.key}",
        position = LazyFocusedStackPosition(columnIndex, itemIndex),
        startMinute = item.startMinute,
        endMinute = item.endMinute,
        value = item.value,
      )
    }
  }

  override val itemCount: Int = items.size

  @Composable
  override fun Item(index: Int, key: Any) {
    val item = items[index]
    itemContent(item.value, state.activePosition == item.position)
  }

  override fun getKey(index: Int): Any = items[index].key

  override fun getContentType(index: Int): Any = "LazyFocusedStackProgram"
}

private data class ProvidedLazyFocusedStackItem<T>(
  val key: Any,
  val position: LazyFocusedStackPosition,
  val startMinute: Int,
  val endMinute: Int,
  val value: T,
)

private data class MeasuredLazyFocusedStackItem<T>(
  val item: ProvidedLazyFocusedStackItem<T>,
  val placeables: List<Placeable>,
  val logicalX: Int,
  val logicalY: Int,
)

private fun Modifier.drawLazyFocusedStackGrid(
  state: LazyFocusedStackState,
  columnCount: Int,
  startMinute: Int,
  endMinute: Int,
  columnWidth: Dp,
  columnSpacing: Dp,
  hourHeight: Dp,
): Modifier = this.then(
  Modifier.clipToBounds().background(StreamTvColors.TransparentWhite5),
).then(
  Modifier.drawBehind {
    drawLazyFocusedStackLines(
      state = state,
      columnCount = columnCount,
      startMinute = startMinute,
      endMinute = endMinute,
      columnWidthPx = columnWidth.toPx(),
      columnSpacingPx = columnSpacing.toPx(),
      hourHeightPx = hourHeight.toPx(),
    )
  },
)

private fun DrawScope.drawLazyFocusedStackLines(
  state: LazyFocusedStackState,
  columnCount: Int,
  startMinute: Int,
  endMinute: Int,
  columnWidthPx: Float,
  columnSpacingPx: Float,
  hourHeightPx: Float,
) {
  val gridColor = StreamTvColors.TransparentWhite20
  val stepX = columnWidthPx + columnSpacingPx
  repeat(columnCount + 1) { index ->
    val x = index * stepX + state.horizontalOffsetPx
    if (x in 0f..size.width) {
      drawLine(
        gridColor,
        start = androidx.compose.ui.geometry.Offset(x, 0f),
        end = androidx.compose.ui.geometry.Offset(x, size.height),
      )
    }
  }
  val firstHour = floor(startMinute / 60f).toInt()
  val lastHour = ceil(endMinute / 60f).toInt()
  for (hour in firstHour..lastHour) {
    val y = (hour * 60 - startMinute) / 60f * hourHeightPx + state.verticalOffsetPx
    if (y in 0f..size.height) {
      drawLine(
        gridColor,
        start = androidx.compose.ui.geometry.Offset(0f, y),
        end = androidx.compose.ui.geometry.Offset(size.width, y),
      )
    }
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF020407)
@Composable
private fun LazyFocusedStackPreview() {
  val previewColumns = listOf(
    LazyFocusedStackColumn(
      key = "nature",
      header = "NATURE",
      items = listOf(
        LazyFocusedStackItem("n1", 8 * 60, 9 * 60 + 30, "Wild Earth"),
        LazyFocusedStackItem("n2", 9 * 60 + 30, 11 * 60, "Ocean Frontiers"),
      ),
    ),
    LazyFocusedStackColumn(
      key = "sport",
      header = "SPORT",
      items = listOf(
        LazyFocusedStackItem("s1", 8 * 60, 10 * 60, "Morning Match"),
        LazyFocusedStackItem("s2", 10 * 60, 11 * 60, "Match Review"),
      ),
    ),
  )

  StreamTvTheme {
    StreamTvSurface {
      LazyFocusedStack(
        columns = previewColumns,
        modifier = Modifier
          .fillMaxSize()
          .padding(48.dp),
        leadingHeader = {
          Box(Modifier.fillMaxSize().background(Color(0xFF111820)).padding(12.dp)) {
            Text("MON 25")
          }
        },
        columnHeader = { channel ->
          Box(Modifier.fillMaxSize().background(Color(0xFF111820)).padding(12.dp)) {
            Text(channel)
          }
        },
        timeLabel = { minute ->
          Box(Modifier.fillMaxSize().background(Color(0xFF090D12)).padding(start = 12.dp)) {
            Text("%02d:00".format((minute / 60) % 24))
          }
        },
        itemContent = { title, isSelected ->
          Box(
            Modifier
              .fillMaxSize()
              .padding(2.dp)
              .background(if (isSelected) Color(0xFF245D84) else Color(0xFF172634))
              .padding(12.dp),
          ) {
            Text(title)
          }
        },
        selectedItemModifier = Modifier.testTag("lazy-focused-stack-selector"),
      )
    }
  }
}
