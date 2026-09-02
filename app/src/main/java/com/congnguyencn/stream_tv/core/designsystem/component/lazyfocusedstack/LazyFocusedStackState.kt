package com.congnguyencn.stream_tv.core.designsystem.component.lazyfocusedstack

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import kotlin.math.roundToInt

@Stable
class LazyFocusedStackState internal constructor(initialPosition: LazyFocusedStackPosition) {
  var selectedPosition by mutableStateOf(initialPosition)
    private set

  var isScrollInProgress by mutableStateOf(false)
    private set

  internal var pendingPosition by mutableStateOf<LazyFocusedStackPosition?>(null)
    private set

  internal var horizontalOffsetPx by mutableFloatStateOf(0f)
    private set

  internal var verticalOffsetPx by mutableFloatStateOf(0f)
    private set

  internal var selectionBounds by mutableStateOf(LazyFocusedStackSelectionBounds.Empty)
    private set

  private var columns by mutableStateOf(emptyList<List<LazyFocusedStackItemRange>>())
  private var layoutInfo by mutableStateOf(LazyFocusedStackLayoutInfo.Empty)

  internal val activePosition: LazyFocusedStackPosition
    get() = pendingPosition ?: selectedPosition

  internal fun updateLayoutInfo(
    newLayoutInfo: LazyFocusedStackLayoutInfo,
    newColumns: List<List<LazyFocusedStackItemRange>>,
  ) {
    if (layoutInfo == newLayoutInfo && columns == newColumns) return

    Snapshot.withMutableSnapshot {
      if (columns != newColumns) {
        columns = newColumns
        selectedPosition = normalizePosition(selectedPosition, newColumns)
        pendingPosition = null
        isScrollInProgress = false
      }
      layoutInfo = newLayoutInfo
      if (!isScrollInProgress) {
        newLayoutInfo.placements[selectedPosition]?.let { placement ->
          horizontalOffsetPx = placement.horizontalOffsetPx
          verticalOffsetPx = placement.verticalOffsetPx
          selectionBounds = placement.selectionBounds
        }
      }
    }
  }

  internal fun target(direction: LazyFocusedStackDirection): LazyFocusedStackPosition? =
    lazyFocusedStackTarget(columns, selectedPosition, direction)

  suspend fun scrollToItem(columnIndex: Int, itemIndex: Int) {
    if (columns.isEmpty() || isScrollInProgress) return
    val normalized = normalizePosition(LazyFocusedStackPosition(columnIndex, itemIndex), columns)
    val placement = layoutInfo.placements[normalized]
    Snapshot.withMutableSnapshot {
      selectedPosition = normalized
      pendingPosition = null
      placement?.let {
        horizontalOffsetPx = it.horizontalOffsetPx
        verticalOffsetPx = it.verticalOffsetPx
        selectionBounds = it.selectionBounds
      }
    }
  }

  internal suspend fun moveSelection(
    direction: LazyFocusedStackDirection,
    animationSpec: AnimationSpec<Float> = tween(LazyFocusedStackDefaults.ScrollDurationMillis),
  ) {
    if (isScrollInProgress) return
    val target = target(direction) ?: return
    val targetPlacement = layoutInfo.placements[target] ?: return
    val startHorizontalOffset = horizontalOffsetPx
    val startVerticalOffset = verticalOffsetPx
    val startBounds = selectionBounds

    Snapshot.withMutableSnapshot {
      pendingPosition = target
      isScrollInProgress = true
    }

    var completed = false
    try {
      animate(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = animationSpec,
      ) { fraction, _ ->
        Snapshot.withMutableSnapshot {
          horizontalOffsetPx = lerp(startHorizontalOffset, targetPlacement.horizontalOffsetPx, fraction)
          verticalOffsetPx = lerp(startVerticalOffset, targetPlacement.verticalOffsetPx, fraction)
          selectionBounds = startBounds.lerp(targetPlacement.selectionBounds, fraction)
        }
      }
      completed = true
    } finally {
      Snapshot.withMutableSnapshot {
        if (completed) {
          selectedPosition = target
          horizontalOffsetPx = targetPlacement.horizontalOffsetPx
          verticalOffsetPx = targetPlacement.verticalOffsetPx
          selectionBounds = targetPlacement.selectionBounds
        }
        pendingPosition = null
        isScrollInProgress = false
      }
    }
  }

  companion object {
    val Saver: Saver<LazyFocusedStackState, List<Int>> = Saver(
      save = { state -> listOf(state.selectedPosition.columnIndex, state.selectedPosition.itemIndex) },
      restore = { saved ->
        LazyFocusedStackState(
          LazyFocusedStackPosition(
            columnIndex = saved.getOrElse(0) { 0 },
            itemIndex = saved.getOrElse(1) { 0 },
          ),
        )
      },
    )
  }
}

@Composable
fun rememberLazyFocusedStackState(initialColumnIndex: Int = 0, initialItemIndex: Int = 0): LazyFocusedStackState =
  rememberSaveable(saver = LazyFocusedStackState.Saver) {
    LazyFocusedStackState(
      LazyFocusedStackPosition(
        columnIndex = initialColumnIndex.coerceAtLeast(0),
        itemIndex = initialItemIndex.coerceAtLeast(0),
      ),
    )
  }

internal data class LazyFocusedStackSelectionBounds(val left: Int, val top: Int, val width: Int, val height: Int) {
  fun lerp(other: LazyFocusedStackSelectionBounds, fraction: Float): LazyFocusedStackSelectionBounds =
    LazyFocusedStackSelectionBounds(
      left = lerp(left.toFloat(), other.left.toFloat(), fraction).roundToInt(),
      top = lerp(top.toFloat(), other.top.toFloat(), fraction).roundToInt(),
      width = lerp(width.toFloat(), other.width.toFloat(), fraction).roundToInt(),
      height = lerp(height.toFloat(), other.height.toFloat(), fraction).roundToInt(),
    )

  companion object {
    val Empty = LazyFocusedStackSelectionBounds(0, 0, 0, 0)
  }
}

internal data class LazyFocusedStackPlacement(
  val horizontalOffsetPx: Float,
  val verticalOffsetPx: Float,
  val selectionBounds: LazyFocusedStackSelectionBounds,
)

internal data class LazyFocusedStackLayoutInfo(
  val placements: Map<LazyFocusedStackPosition, LazyFocusedStackPlacement>,
) {
  companion object {
    val Empty = LazyFocusedStackLayoutInfo(emptyMap())
  }
}

private fun normalizePosition(
  position: LazyFocusedStackPosition,
  columns: List<List<LazyFocusedStackItemRange>>,
): LazyFocusedStackPosition {
  val requestedItems = columns.getOrNull(position.columnIndex).orEmpty()
  if (requestedItems.isNotEmpty()) {
    return position.copy(itemIndex = position.itemIndex.coerceIn(requestedItems.indices))
  }
  return firstLazyFocusedStackPosition(columns) ?: LazyFocusedStackPosition(0, 0)
}

private fun lerp(start: Float, end: Float, fraction: Float): Float = start + (end - start) * fraction
