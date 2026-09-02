package com.congnguyencn.stream_tv.core.designsystem.component.lazyfocusedstack

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LazyFocusedStackNavigationTest {
  private val early = LazyFocusedStackItemRange(startMinute = 6 * 60, endMinute = 8 * 60)
  private val midday = LazyFocusedStackItemRange(startMinute = 12 * 60, endMinute = 13 * 60)
  private val late = LazyFocusedStackItemRange(startMinute = 22 * 60, endMinute = 24 * 60)

  @Test
  fun `right skips an empty column and keeps the selected time`() {
    val columns = listOf(
      listOf(early, late),
      emptyList(),
      listOf(early, midday, late),
    )

    assertEquals(
      LazyFocusedStackPosition(columnIndex = 2, itemIndex = 2),
      lazyFocusedStackTarget(
        columns = columns,
        current = LazyFocusedStackPosition(columnIndex = 0, itemIndex = 1),
        direction = LazyFocusedStackDirection.Right,
      ),
    )
  }

  @Test
  fun `horizontal movement chooses a program containing the current midpoint`() {
    val columns = listOf(
      listOf(midday),
      listOf(
        LazyFocusedStackItemRange(10 * 60, 12 * 60),
        LazyFocusedStackItemRange(12 * 60, 14 * 60),
        LazyFocusedStackItemRange(14 * 60, 16 * 60),
      ),
    )

    assertEquals(
      LazyFocusedStackPosition(columnIndex = 1, itemIndex = 1),
      lazyFocusedStackTarget(
        columns = columns,
        current = LazyFocusedStackPosition(columnIndex = 0, itemIndex = 0),
        direction = LazyFocusedStackDirection.Right,
      ),
    )
  }

  @Test
  fun `down reaches the last program then releases the boundary`() {
    val columns = listOf(listOf(early, midday, late))

    assertEquals(
      LazyFocusedStackPosition(columnIndex = 0, itemIndex = 2),
      lazyFocusedStackTarget(
        columns,
        LazyFocusedStackPosition(columnIndex = 0, itemIndex = 1),
        LazyFocusedStackDirection.Down,
      ),
    )
    assertNull(
      lazyFocusedStackTarget(
        columns,
        LazyFocusedStackPosition(columnIndex = 0, itemIndex = 2),
        LazyFocusedStackDirection.Down,
      ),
    )
  }

  @Test
  fun `left and right release focus at the first and last non-empty columns`() {
    val columns = listOf(listOf(early), emptyList(), listOf(late))

    assertNull(
      lazyFocusedStackTarget(
        columns,
        LazyFocusedStackPosition(0, 0),
        LazyFocusedStackDirection.Left,
      ),
    )
    assertNull(
      lazyFocusedStackTarget(
        columns,
        LazyFocusedStackPosition(2, 0),
        LazyFocusedStackDirection.Right,
      ),
    )
  }
}
