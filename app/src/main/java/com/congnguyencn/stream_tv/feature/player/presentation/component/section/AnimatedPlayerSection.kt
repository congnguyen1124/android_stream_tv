package com.congnguyencn.stream_tv.feature.player.presentation.component.section

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

private const val PlayerSectionAnimationDurationMillis = 300

@Composable
internal fun AnimatedPlayerSection(
  isEntering: Boolean,
  isExiting: Boolean,
  onEnterAnimationFinished: () -> Unit,
  onExitAnimationFinished: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  val offsetFraction = remember { Animatable(if (isEntering) 1f else 0f) }
  val currentOnEnterAnimationFinished = rememberUpdatedState(onEnterAnimationFinished)
  val currentOnExitAnimationFinished = rememberUpdatedState(onExitAnimationFinished)

  LaunchedEffect(isEntering, isExiting) {
    when {
      isEntering -> {
        offsetFraction.snapTo(1f)
        offsetFraction.animateTo(
          targetValue = 0f,
          animationSpec = tween(
            durationMillis = PlayerSectionAnimationDurationMillis,
            easing = FastOutSlowInEasing,
          ),
        )
        currentOnEnterAnimationFinished.value()
      }

      isExiting -> {
        offsetFraction.animateTo(
          targetValue = 1f,
          animationSpec = tween(
            durationMillis = PlayerSectionAnimationDurationMillis,
            easing = FastOutSlowInEasing,
          ),
        )
        currentOnExitAnimationFinished.value()
      }

      else -> offsetFraction.snapTo(0f)
    }
  }

  Box(
    modifier = modifier.graphicsLayer {
      translationX = size.width * offsetFraction.value
    },
  ) {
    content()
  }
}
