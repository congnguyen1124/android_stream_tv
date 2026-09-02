package com.congnguyencn.stream_tv.core.designsystem.component.lazyfocusedstack

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.dp
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvSurface
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LazyFocusedStackTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun oneFocusOwnerMovesRightAcrossAnEmptyColumn() {
    lateinit var state: LazyFocusedStackState

    composeRule.setContent {
      val focusRequester = remember { FocusRequester() }
      state = rememberLazyFocusedStackState(initialColumnIndex = 0, initialItemIndex = 1)
      val columns = remember {
        listOf(
          testColumn("one", listOf(0 to 60, 60 to 120)),
          testColumn("empty", emptyList()),
          testColumn("three", listOf(0 to 45, 45 to 120)),
        )
      }

      StreamTvTheme {
        StreamTvSurface {
          LazyFocusedStack(
            columns = columns,
            modifier = Modifier.size(width = 720.dp, height = 400.dp),
            state = state,
            selectedItemModifier = Modifier
              .focusRequester(focusRequester)
              .testTag("stack-selector"),
            leadingHeader = { Box(Modifier.fillMaxSize()) },
            columnHeader = { Box(Modifier.fillMaxSize()) },
            timeLabel = { Box(Modifier.fillMaxSize()) },
            itemContent = { _, _ -> Box(Modifier.fillMaxSize()) },
          )
        }
      }

      LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }

    composeRule.onAllNodes(isFocusable()).assertCountEquals(1)
    composeRule.runOnIdle {
      assertEquals(
        LazyFocusedStackPosition(2, 1),
        state.target(LazyFocusedStackDirection.Right),
      )
    }
    composeRule
      .onNodeWithTag("stack-selector")
      .assertIsFocused()
      .performKeyInput { pressKey(Key.DirectionRight) }

    composeRule.waitForIdle()
    assertEquals(LazyFocusedStackPosition(2, 1), state.selectedPosition)
    composeRule.onAllNodes(isFocusable()).assertCountEquals(1)
  }

  @Test
  fun rightAtTheLastColumnReleasesFocusToTheNextView() {
    composeRule.setContent {
      val stackFocusRequester = remember { FocusRequester() }

      StreamTvTheme {
        StreamTvSurface {
          Row {
            LazyFocusedStack(
              columns = listOf(testColumn("only", listOf(0 to 60))),
              modifier = Modifier.size(width = 600.dp, height = 400.dp),
              selectedItemModifier = Modifier
                .focusRequester(stackFocusRequester)
                .testTag("boundary-stack-selector"),
              leadingHeader = { Box(Modifier.fillMaxSize()) },
              columnHeader = { Box(Modifier.fillMaxSize()) },
              timeLabel = { Box(Modifier.fillMaxSize()) },
              itemContent = { _, _ -> Box(Modifier.fillMaxSize()) },
            )
            Box(
              modifier = Modifier
                .size(48.dp)
                .focusable()
                .testTag("right-focus-target"),
            )
          }
        }
      }

      LaunchedEffect(Unit) { stackFocusRequester.requestFocus() }
    }

    composeRule
      .onNodeWithTag("boundary-stack-selector")
      .assertIsFocused()
      .performKeyInput { pressKey(Key.DirectionRight) }

    composeRule.onNodeWithTag("right-focus-target").assertIsFocused()
  }

  private fun testColumn(key: String, ranges: List<Pair<Int, Int>>): LazyFocusedStackColumn<String, String> =
    LazyFocusedStackColumn(
      key = key,
      header = key,
      items = ranges.mapIndexed { index, range ->
        LazyFocusedStackItem(
          key = "$key-$index",
          startMinute = range.first,
          endMinute = range.second,
          value = "$key-$index",
        )
      },
    )
}
