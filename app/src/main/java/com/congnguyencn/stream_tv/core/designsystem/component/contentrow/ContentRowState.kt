package com.congnguyencn.stream_tv.core.designsystem.component.contentrow

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot

/** State holder for a [ContentRow]. Indices always refer to the real backing collection. */
@Stable
class ContentRowState internal constructor(
    initialSelectedIndex: Int,
) {
    var selectedIndex by mutableIntStateOf(initialSelectedIndex.coerceAtLeast(0))
        private set

    var isScrollInProgress by mutableStateOf(false)
        private set

    internal var itemCount by mutableIntStateOf(0)
        private set

    internal var animationOffsetPx by mutableFloatStateOf(0f)
        private set

    internal var selectionBounds by mutableStateOf(ContentRowSelectionBounds.Empty)
        private set

    private var forwardStepPx by mutableFloatStateOf(0f)
    private var backwardStepPx by mutableFloatStateOf(0f)

    /**
     * Immediately selects [index]. Looping collections wrap; finite collections clamp to an edge.
     */
    suspend fun scrollToItem(index: Int) {
        if (itemCount == 0) return
        Snapshot.withMutableSnapshot {
            selectedIndex = normalizeContentRowIndex(index, itemCount)
            animationOffsetPx = 0f
        }
    }

    internal fun updateItemCount(newItemCount: Int) {
        require(newItemCount >= 0) { "ContentRow item count must be non-negative" }
        if (itemCount == newItemCount) return

        Snapshot.withMutableSnapshot {
            itemCount = newItemCount
            selectedIndex = normalizeContentRowIndex(selectedIndex, newItemCount)
            if (newItemCount == 0) {
                animationOffsetPx = 0f
                selectionBounds = ContentRowSelectionBounds.Empty
                forwardStepPx = 0f
                backwardStepPx = 0f
            }
        }
    }

    internal fun updateLayoutInfo(
        bounds: ContentRowSelectionBounds,
        forwardStepPx: Float,
        backwardStepPx: Float,
    ) {
        if (selectionBounds != bounds) selectionBounds = bounds
        if (this.forwardStepPx != forwardStepPx) this.forwardStepPx = forwardStepPx
        if (this.backwardStepPx != backwardStepPx) this.backwardStepPx = backwardStepPx
    }

    internal suspend fun moveSelection(
        direction: Int,
        animationSpec: AnimationSpec<Float> = tween(ContentRowDefaults.ScrollDurationMillis),
    ) {
        val isAtFiniteEnd = direction > 0 &&
            selectedIndex == itemCount - 1 &&
            !isContentRowLoopingEnabled(itemCount)
        if (
            direction == 0 ||
            itemCount <= 1 ||
            isScrollInProgress ||
            (direction < 0 && selectedIndex == 0) ||
            isAtFiniteEnd
        ) return

        val normalizedDirection = direction.coerceIn(-1, 1)
        val stepPx = if (normalizedDirection > 0) forwardStepPx else backwardStepPx
        if (stepPx <= 0f) return

        isScrollInProgress = true
        var completed = false
        try {
            val targetOffset = if (normalizedDirection > 0) -stepPx else stepPx
            animate(
                initialValue = 0f,
                targetValue = targetOffset,
                animationSpec = animationSpec,
            ) { value, _ ->
                animationOffsetPx = value
            }
            completed = true
        } finally {
            Snapshot.withMutableSnapshot {
                if (completed) {
                    selectedIndex = contentRowTargetIndex(
                        currentIndex = selectedIndex,
                        delta = normalizedDirection,
                        itemCount = itemCount,
                    )
                }
                animationOffsetPx = 0f
                isScrollInProgress = false
            }
        }
    }

    companion object {
        val Saver: Saver<ContentRowState, Int> = Saver(
            save = { state -> state.selectedIndex },
            restore = ::ContentRowState,
        )
    }
}

@Composable
fun rememberContentRowState(initialSelectedIndex: Int = 0): ContentRowState =
    rememberSaveable(saver = ContentRowState.Saver) {
        ContentRowState(initialSelectedIndex)
    }

internal data class ContentRowSelectionBounds(
    val left: Int,
    val top: Int,
    val width: Int,
    val height: Int,
) {
    companion object {
        val Empty = ContentRowSelectionBounds(left = 0, top = 0, width = 0, height = 0)
    }
}

internal fun contentRowTargetIndex(currentIndex: Int, delta: Int, itemCount: Int): Int {
    if (itemCount <= 0) return 0
    return when {
        delta > 0 &&
            currentIndex >= itemCount - 1 &&
            isContentRowLoopingEnabled(itemCount) -> 0
        delta > 0 -> (currentIndex + 1).coerceAtMost(itemCount - 1)
        delta < 0 -> (currentIndex - 1).coerceAtLeast(0)
        else -> currentIndex.coerceIn(0, itemCount - 1)
    }
}

internal fun isContentRowLoopingEnabled(itemCount: Int): Boolean =
    itemCount > ContentRowDefaults.LoopingItemCountThreshold

private fun normalizeContentRowIndex(index: Int, itemCount: Int): Int = when {
    itemCount <= 0 -> 0
    isContentRowLoopingEnabled(itemCount) -> index.floorMod(itemCount)
    else -> index.coerceIn(0, itemCount - 1)
}

private fun Int.floorMod(divisor: Int): Int = ((this % divisor) + divisor) % divisor
