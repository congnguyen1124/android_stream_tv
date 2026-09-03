package com.congnguyencn.stream_tv.feature.search.presentation.component

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme

private const val KeyboardColumns = 7
private val KeyboardKeyGap = 8.dp
private val MinimumKeySize = 24.dp
private val MaximumKeySize = 60.dp
private val LetterKeys = ('a'..'z').map(Char::toString)
private val SymbolKeys = listOf(
  "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
  ".", ",", "/", ":", ";", "-", "_", "~", "?", "!", "=", "+", "&", "@", "%", "#", "*", "|",
)

/**
 * Compact TV keyboard with the same insert/delete/caret controls as the Downloader keyboard.
 * Focus scale is fixed at 1f so adjacent keys never overlap.
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
internal fun SearchVirtualKeyboard(
  onKey: (String) -> Unit,
  onBackspace: () -> Unit,
  onClear: () -> Unit,
  onCursorLeft: () -> Unit,
  onCursorRight: () -> Unit,
  onSearch: () -> Unit,
  firstKeyFocusRequester: FocusRequester,
  searchKeyFocusRequester: FocusRequester,
  topBarFocusRequester: FocusRequester,
  leftExitFocusRequester: FocusRequester,
  onMoveToResults: () -> Boolean,
  modifier: Modifier = Modifier,
) {
  var uppercase by remember { mutableStateOf(false) }
  var symbolMode by remember { mutableStateOf(false) }
  val gridRows = maxOf(
    (LetterKeys.size + KeyboardColumns - 1) / KeyboardColumns,
    (SymbolKeys.size + KeyboardColumns - 1) / KeyboardColumns,
  )

  BoxWithConstraints(
    modifier = modifier.fillMaxHeight(),
    contentAlignment = Alignment.TopStart,
  ) {
    val keySizeByHeight = (maxHeight - KeyboardKeyGap * gridRows) / (gridRows + 1)
    val keySizeByWidth = (maxWidth - KeyboardKeyGap * KeyboardColumns) / 8.5f
    val keySize = minOf(keySizeByHeight, keySizeByWidth).coerceIn(MinimumKeySize, MaximumKeySize)
    val gridHeight = keySize * gridRows + KeyboardKeyGap * (gridRows - 1)
    val gridWidth = keySize * KeyboardColumns + KeyboardKeyGap * (KeyboardColumns - 1)
    val functionColumnWidth = keySize * 1.5f
    val functionKeyHeight = (gridHeight - KeyboardKeyGap * 2) / 3
    val keyboardWidth = gridWidth + KeyboardKeyGap + functionColumnWidth
    val characterKeys = if (symbolMode) SymbolKeys else LetterKeys

    Column(
      modifier = Modifier.width(keyboardWidth),
      verticalArrangement = Arrangement.spacedBy(KeyboardKeyGap),
    ) {
      Row(horizontalArrangement = Arrangement.spacedBy(KeyboardKeyGap)) {
        LazyVerticalGrid(
          columns = GridCells.Fixed(KeyboardColumns),
          modifier = Modifier
            .width(gridWidth)
            .height(gridHeight),
          horizontalArrangement = Arrangement.spacedBy(KeyboardKeyGap),
          verticalArrangement = Arrangement.spacedBy(KeyboardKeyGap),
          userScrollEnabled = false,
        ) {
          itemsIndexed(
            items = characterKeys,
            key = { _, key -> key },
          ) { index, base ->
            val label = if (uppercase && !symbolMode) base.uppercase() else base
            SearchKeyboardKey(
              onClick = { onKey(label) },
              modifier = Modifier
                .fillMaxWidth()
                .height(keySize)
                .testTag("search-key-$base")
                .focusProperties {
                  if (index < KeyboardColumns) up = topBarFocusRequester
                  if (index == 0) left = leftExitFocusRequester
                }
                .then(
                  if (index == 0) {
                    Modifier.focusRequester(firstKeyFocusRequester)
                  } else {
                    Modifier
                  },
                ),
            ) {
              Text(text = label, style = StreamTvTheme.typography.bodyLarge)
            }
          }
        }

        Column(
          modifier = Modifier.width(functionColumnWidth),
          verticalArrangement = Arrangement.spacedBy(KeyboardKeyGap),
        ) {
          SearchKeyboardKey(
            onClick = { symbolMode = !symbolMode },
            modifier = Modifier
              .fillMaxWidth()
              .height(functionKeyHeight)
              .focusProperties { up = topBarFocusRequester },
            isFunction = true,
          ) {
            Text(
              text = if (symbolMode) "ABC" else "?123",
              style = StreamTvTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            )
          }
          SearchKeyboardKey(
            onClick = { uppercase = !uppercase },
            modifier = Modifier
              .fillMaxWidth()
              .height(functionKeyHeight),
            isFunction = true,
            selected = uppercase && !symbolMode,
          ) {
            Icon(
              imageVector = Icons.Default.KeyboardArrowUp,
              contentDescription = "Shift",
              modifier = Modifier.size(21.dp),
            )
          }
          SearchKeyboardKey(
            onClick = onBackspace,
            modifier = Modifier
              .fillMaxWidth()
              .height(functionKeyHeight),
            isFunction = true,
          ) {
            Icon(
              imageVector = Icons.Default.Clear,
              contentDescription = "Backspace",
              modifier = Modifier.size(20.dp),
            )
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(KeyboardKeyGap),
      ) {
        SearchKeyboardKey(
          onClick = { onKey(" ") },
          modifier = Modifier
            .height(keySize)
            .weight(1f)
            .moveToResultsOnDown(onMoveToResults),
          isFunction = true,
        ) {
          Text(text = "Space", style = StreamTvTheme.typography.labelMedium)
        }
        SearchKeyboardKey(
          onClick = onCursorLeft,
          modifier = Modifier
            .height(keySize)
            .width(keySize)
            .moveToResultsOnDown(onMoveToResults),
          isFunction = true,
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Move caret left",
            modifier = Modifier.size(19.dp),
          )
        }
        SearchKeyboardKey(
          onClick = onCursorRight,
          modifier = Modifier
            .height(keySize)
            .width(keySize)
            .moveToResultsOnDown(onMoveToResults),
          isFunction = true,
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Move caret right",
            modifier = Modifier.size(19.dp),
          )
        }
        SearchKeyboardKey(
          onClick = onClear,
          modifier = Modifier
            .height(keySize)
            .width(keySize * 1.35f)
            .moveToResultsOnDown(onMoveToResults),
          isFunction = true,
        ) {
          Text(text = "Clear", style = StreamTvTheme.typography.labelMedium)
        }
        SearchKeyboardKey(
          onClick = onSearch,
          modifier = Modifier
            .height(keySize)
            .width(keySize * 2.75f)
            .testTag("search-key-search")
            .focusRequester(searchKeyFocusRequester)
            .moveToResultsOnDown(onMoveToResults),
          selected = true,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = null,
              modifier = Modifier.size(17.dp),
            )
            Text(
              text = "Search",
              style = StreamTvTheme.typography.labelMedium,
              maxLines = 1,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SearchKeyboardKey(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isFunction: Boolean = false,
  selected: Boolean = false,
  content: @Composable () -> Unit,
) {
  val containerColor = when {
    selected -> StreamTvColors.Primary80
    isFunction -> StreamTvColors.TransparentWhite10
    else -> StreamTvColors.Neutral100
  }
  val contentColor = when {
    selected -> StreamTvColors.NeutralWhite
    isFunction -> StreamTvColors.Neutral10
    else -> StreamTvColors.Neutral20
  }

  Surface(
    onClick = onClick,
    modifier = modifier,
    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
    scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
    colors = ClickableSurfaceDefaults.colors(
      containerColor = containerColor,
      contentColor = contentColor,
      focusedContainerColor = StreamTvColors.Primary30,
      focusedContentColor = StreamTvColors.NeutralBlack,
      pressedContainerColor = StreamTvColors.Primary50,
      pressedContentColor = StreamTvColors.NeutralWhite,
    ),
  ) {
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center,
    ) {
      content()
    }
  }
}

private fun Modifier.moveToResultsOnDown(
  onMoveToResults: () -> Boolean,
): Modifier = onPreviewKeyEvent { event ->
  if (event.key != Key.DirectionDown) return@onPreviewKeyEvent false
  if (event.type == KeyEventType.KeyDown) onMoveToResults()
  true
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF010810)
@Composable
private fun SearchVirtualKeyboardPreview() {
  StreamTvTheme {
    SearchVirtualKeyboard(
      onKey = {},
      onBackspace = {},
      onClear = {},
      onCursorLeft = {},
      onCursorRight = {},
      onSearch = {},
      firstKeyFocusRequester = remember { FocusRequester() },
      searchKeyFocusRequester = remember { FocusRequester() },
      topBarFocusRequester = remember { FocusRequester() },
      leftExitFocusRequester = remember { FocusRequester() },
      onMoveToResults = { true },
      modifier = Modifier
        .width(650.dp)
        .height(300.dp),
    )
  }
}
