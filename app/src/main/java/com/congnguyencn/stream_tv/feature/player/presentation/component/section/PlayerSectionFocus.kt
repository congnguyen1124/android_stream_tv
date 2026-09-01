package com.congnguyencn.stream_tv.feature.player.presentation.component.section

import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

internal suspend fun awaitPlayerSectionFrame() {
  withFrameMillis { }
}

internal fun Modifier.handlePlayerSectionExit(onBack: () -> Unit, dismissOnLeft: Boolean): Modifier =
  onPreviewKeyEvent { event ->
    val shouldDismiss = event.key == Key.Back ||
      event.key == Key.Escape ||
      (dismissOnLeft && event.key == Key.DirectionLeft)
    if (event.type == KeyEventType.KeyDown && shouldDismiss) {
      onBack()
      true
    } else {
      false
    }
  }
