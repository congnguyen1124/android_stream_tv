package com.congnguyencn.stream_tv.core.designsystem.component.contentrow

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ContentRowTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun onlySelectedItemOwnsFocusAndRightWrapsToFirstRealItem() {
    lateinit var state: ContentRowState

    composeRule.setContent {
      val focusRequester = remember { FocusRequester() }
      state = rememberContentRowState(initialSelectedIndex = 9)

      StreamTvTheme {
        StreamTvSurface {
          ContentRow(
            state = state,
            modifier = Modifier.width(720.dp),
            selectedItemModifier = Modifier
              .focusRequester(focusRequester)
              .testTag("content-row-selected-item"),
          ) {
            items(
              count = 10,
              key = { index -> "item-$index" },
            ) { index ->
              Box(
                modifier = Modifier
                  .width(200.dp)
                  .height(112.dp)
                  .testTag("content-row-item-$index"),
              )
            }
          }
        }
      }

      LaunchedEffect(Unit) {
        focusRequester.requestFocus()
      }
    }

    composeRule.onAllNodes(isFocusable()).assertCountEquals(1)
    composeRule
      .onNodeWithTag("content-row-selected-item")
      .assertIsFocused()
      .performKeyInput { pressKey(Key.DirectionRight) }

    composeRule.waitUntil(timeoutMillis = 2_000) { state.selectedIndex == 0 }
    assertEquals(0, state.selectedIndex)
    composeRule.onAllNodes(isFocusable()).assertCountEquals(1)
  }

  @Test
  fun loopingRowKeepsNextCycleVisibleBeforeResettingToItemZero() {
    lateinit var state: ContentRowState

    composeRule.setContent {
      val focusRequester = remember { FocusRequester() }
      state = rememberContentRowState(initialSelectedIndex = 5)

      StreamTvTheme {
        StreamTvSurface {
          ContentRow(
            state = state,
            modifier = Modifier.width(720.dp),
            selectedItemModifier = Modifier
              .focusRequester(focusRequester)
              .testTag("content-row-selected-item"),
          ) {
            items(count = 6) { index ->
              Box(
                modifier = Modifier
                  .width(200.dp)
                  .height(112.dp)
                  .testTag("content-row-item-$index"),
              )
            }
          }
        }
      }

      LaunchedEffect(Unit) {
        focusRequester.requestFocus()
      }
    }

    composeRule.onNodeWithTag("content-row-item-0").assertIsDisplayed()
    composeRule
      .onNodeWithTag("content-row-selected-item")
      .assertIsFocused()
      .performKeyInput { pressKey(Key.DirectionRight) }

    composeRule.waitUntil(timeoutMillis = 2_000) { state.selectedIndex == 0 }
    composeRule.onNodeWithTag("content-row-item-1").assertIsDisplayed()
  }

  @Test
  fun trailingItemIsVisibleWhileSelectionIsStillAnimating() {
    composeRule.setContent {
      val focusRequester = remember { FocusRequester() }

      StreamTvTheme {
        StreamTvSurface {
          ContentRow(
            modifier = Modifier.width(720.dp),
            selectedItemModifier = Modifier
              .focusRequester(focusRequester)
              .testTag("content-row-selected-item"),
          ) {
            items(count = 6) { index ->
              Box(
                modifier = Modifier
                  .width(200.dp)
                  .height(112.dp)
                  .testTag("content-row-item-$index"),
              )
            }
          }
        }
      }

      LaunchedEffect(Unit) {
        focusRequester.requestFocus()
      }
    }

    composeRule.onNodeWithTag("content-row-selected-item").assertIsFocused()
    composeRule.mainClock.autoAdvance = false
    composeRule
      .onNodeWithTag("content-row-selected-item")
      .performKeyInput { pressKey(Key.DirectionRight) }
    composeRule.mainClock.advanceTimeBy(100)

    composeRule.onNodeWithTag("content-row-item-3").assertIsDisplayed()
  }

  @Test
  fun finiteRowStopsAtLastItemWithoutResetting() {
    lateinit var state: ContentRowState

    composeRule.setContent {
      val focusRequester = remember { FocusRequester() }
      state = rememberContentRowState(initialSelectedIndex = 4)

      StreamTvTheme {
        StreamTvSurface {
          ContentRow(
            state = state,
            modifier = Modifier.width(720.dp),
            selectedItemModifier = Modifier
              .focusRequester(focusRequester)
              .testTag("content-row-selected-item"),
          ) {
            items(count = 5) { index ->
              Box(
                modifier = Modifier
                  .width(200.dp)
                  .height(112.dp)
                  .testTag("content-row-item-$index"),
              )
            }
          }
        }
      }

      LaunchedEffect(Unit) {
        focusRequester.requestFocus()
      }
    }

    composeRule
      .onNodeWithTag("content-row-selected-item")
      .assertIsFocused()
      .performKeyInput { pressKey(Key.DirectionRight) }
    composeRule.waitForIdle()

    assertEquals(4, state.selectedIndex)
  }

  @Test
  fun largeRowStopsAtLastItemWhenLoopingIsDisabled() {
    lateinit var state: ContentRowState

    composeRule.setContent {
      val focusRequester = remember { FocusRequester() }
      state = rememberContentRowState(initialSelectedIndex = 7)

      StreamTvTheme {
        StreamTvSurface {
          ContentRow(
            state = state,
            loopingEnabled = false,
            modifier = Modifier.width(720.dp),
            selectedItemModifier = Modifier
              .focusRequester(focusRequester)
              .testTag("content-row-selected-item"),
          ) {
            items(count = 8) { index ->
              Box(
                modifier = Modifier
                  .width(200.dp)
                  .height(112.dp)
                  .testTag("content-row-item-$index"),
              )
            }
          }
        }
      }

      LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }

    composeRule
      .onNodeWithTag("content-row-selected-item")
      .assertIsFocused()
      .performKeyInput { pressKey(Key.DirectionRight) }
    composeRule.waitForIdle()

    assertEquals(7, state.selectedIndex)
  }

  @Test
  fun leftFromFirstItemUsesDefaultFocusSearch() {
    composeRule.setContent {
      val contentRowFocusRequester = remember { FocusRequester() }

      StreamTvTheme {
        StreamTvSurface {
          Row {
            Box(
              modifier = Modifier
                .size(48.dp)
                .focusable()
                .testTag("left-focus-target"),
            )
            ContentRow(
              modifier = Modifier.width(720.dp),
              selectedItemModifier = Modifier
                .focusRequester(contentRowFocusRequester)
                .testTag("content-row-selected-item"),
            ) {
              items(count = 3) { index ->
                Box(
                  modifier = Modifier
                    .width(200.dp)
                    .height(112.dp)
                    .testTag("content-row-item-$index"),
                )
              }
            }
          }
        }
      }

      LaunchedEffect(Unit) {
        contentRowFocusRequester.requestFocus()
      }
    }

    composeRule
      .onNodeWithTag("content-row-selected-item")
      .assertIsFocused()
      .performKeyInput { pressKey(Key.DirectionLeft) }

    composeRule.onNodeWithTag("left-focus-target").assertIsFocused()
  }

  @Test
  fun selectorStaysAtLeadingEdgeAndAddsContentPadding() {
    composeRule.setContent {
      StreamTvTheme {
        StreamTvSurface {
          ContentRow(
            state = rememberContentRowState(initialSelectedIndex = 1),
            modifier = Modifier.width(720.dp),
            selectedItemModifier = Modifier.testTag("content-row-selected-item"),
          ) {
            items(count = 4) { index ->
              Box(
                modifier = Modifier
                  .width(200.dp)
                  .height(112.dp)
                  .testTag("content-row-item-$index"),
              )
            }
          }
        }
      }
    }

    val previousBounds = composeRule
      .onNodeWithTag("content-row-item-0")
      .fetchSemanticsNode()
      .boundsInRoot
    val selectedContentBounds = composeRule
      .onNodeWithTag("content-row-item-1")
      .fetchSemanticsNode()
      .boundsInRoot
    val selectorBounds = composeRule
      .onNodeWithTag("content-row-selected-item")
      .fetchSemanticsNode()
      .boundsInRoot
    val expectedPaddingPx = with(composeRule.density) { 2.dp.toPx() }
    val fullItemWidthPx = with(composeRule.density) { 200.dp.toPx() }

    assertTrue(previousBounds.left <= 0.5f)
    assertTrue(previousBounds.right > 0f)
    assertTrue(previousBounds.width < fullItemWidthPx)
    assertTrue(previousBounds.right < selectedContentBounds.left)
    assertEquals(expectedPaddingPx, selectedContentBounds.left - selectorBounds.left, 0.5f)
    assertEquals(expectedPaddingPx, selectedContentBounds.top - selectorBounds.top, 0.5f)
    assertEquals(expectedPaddingPx, selectorBounds.right - selectedContentBounds.right, 0.5f)
    assertEquals(expectedPaddingPx, selectorBounds.bottom - selectedContentBounds.bottom, 0.5f)
  }
}
