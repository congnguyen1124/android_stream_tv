package com.congnguyencn.stream_tv.core.designsystem.component.lazyfocusedstack

import androidx.compose.runtime.Immutable

/** A time-bounded value placed inside one [LazyFocusedStackColumn]. */
@Immutable
data class LazyFocusedStackItem<T>(
  val key: Any,
  val startMinute: Int,
  val endMinute: Int,
  val value: T,
) {
  init {
    require(startMinute >= 0) { "LazyFocusedStack item start must be non-negative" }
    require(endMinute > startMinute) { "LazyFocusedStack item end must be after its start" }
  }
}

/** One horizontal lane in a [LazyFocusedStack]. Empty columns remain visible and are skipped by D-pad navigation. */
@Immutable
data class LazyFocusedStackColumn<C, T>(
  val key: Any,
  val header: C,
  val items: List<LazyFocusedStackItem<T>>,
)

@Immutable
data class LazyFocusedStackPosition(
  val columnIndex: Int,
  val itemIndex: Int,
)

internal data class LazyFocusedStackItemRange(
  val startMinute: Int,
  val endMinute: Int,
)

internal enum class LazyFocusedStackDirection {
  Up,
  Down,
  Left,
  Right,
}

/**
 * Resolves D-pad movement without depending on Compose layout state.
 *
 * Horizontal movement selects the program nearest to the current program midpoint and skips empty
 * columns. That keeps late-night programs aligned with late-night programs even when schedules do
 * not have identical boundaries.
 */
internal fun lazyFocusedStackTarget(
  columns: List<List<LazyFocusedStackItemRange>>,
  current: LazyFocusedStackPosition,
  direction: LazyFocusedStackDirection,
): LazyFocusedStackPosition? {
  val currentItems = columns.getOrNull(current.columnIndex).orEmpty()
  val currentItem = currentItems.getOrNull(current.itemIndex) ?: return firstLazyFocusedStackPosition(columns)

  return when (direction) {
    LazyFocusedStackDirection.Up -> current.itemIndex
      .takeIf { index -> index > 0 }
      ?.let { index -> current.copy(itemIndex = index - 1) }

    LazyFocusedStackDirection.Down -> current.itemIndex
      .takeIf { index -> index < currentItems.lastIndex }
      ?.let { index -> current.copy(itemIndex = index + 1) }

    LazyFocusedStackDirection.Left,
    LazyFocusedStackDirection.Right -> {
      val columnStep = if (direction == LazyFocusedStackDirection.Left) -1 else 1
      val targetColumnIndex = generateSequence(current.columnIndex + columnStep) { index -> index + columnStep }
        .takeWhile { index -> index in columns.indices }
        .firstOrNull { index -> columns[index].isNotEmpty() }
        ?: return null
      val targetItems = columns[targetColumnIndex]
      val referenceMinute = (currentItem.startMinute + currentItem.endMinute) / 2
      val targetItemIndex = targetItems.indices.minWithOrNull(
        compareBy<Int> { index -> targetItems[index].distanceFrom(referenceMinute) }
          .thenBy { index ->
            val target = targetItems[index]
            kotlin.math.abs((target.startMinute + target.endMinute) / 2 - referenceMinute)
          },
      ) ?: return null

      LazyFocusedStackPosition(targetColumnIndex, targetItemIndex)
    }
  }
}

internal fun firstLazyFocusedStackPosition(
  columns: List<List<LazyFocusedStackItemRange>>,
): LazyFocusedStackPosition? {
  val columnIndex = columns.indexOfFirst(List<LazyFocusedStackItemRange>::isNotEmpty)
  return columnIndex.takeIf { it >= 0 }?.let { LazyFocusedStackPosition(it, 0) }
}

private fun LazyFocusedStackItemRange.distanceFrom(minute: Int): Int = when {
  minute < startMinute -> startMinute - minute
  minute >= endMinute -> minute - endMinute
  else -> 0
}
