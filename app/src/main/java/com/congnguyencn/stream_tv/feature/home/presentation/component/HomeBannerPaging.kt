package com.congnguyencn.stream_tv.feature.home.presentation.component

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

/**
 * Paging for [HomeBannerSection]: the looping edges, the D-pad contract and the auto-advance.
 *
 * Split out from the composable because every rule here is arithmetic over page indices, and is
 * covered as such by `HomeBannerPagerTest`.
 */
@Suppress("LongParameterList")
internal fun handleBannerKeyEvent(
  event: KeyEvent,
  pagerState: PagerState,
  currentPage: Int,
  realItemCount: Int,
  hasLoopingEdges: Boolean,
  isFocused: Boolean,
  scope: CoroutineScope,
  onSelect: () -> Unit,
): Boolean {
  val isKeyDown = event.type == KeyEventType.KeyDown

  return when (event.key) {
    Key.DirectionLeft -> {
      if (isKeyDown && canScrollBannerLeft(currentPage, hasLoopingEdges)) {
        scope.launch { scrollBannerPrevious(pagerState, hasLoopingEdges) }
      }
      true
    }

    Key.DirectionRight -> {
      if (isKeyDown && isFocused) {
        scope.launch {
          scrollBannerNext(
            pagerState = pagerState,
            realItemCount = realItemCount,
            hasLoopingEdges = hasLoopingEdges,
          )
        }
      }
      true
    }

    Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
      if (isKeyDown) onSelect()
      true
    }

    else -> false
  }
}

@Composable
@OptIn(ExperimentalCoroutinesApi::class)
internal fun BannerAutoScrollEffect(
  pagerState: PagerState,
  hasLoopingEdges: Boolean,
  autoScrollDurationMillis: Long,
) {
  val scope = rememberCoroutineScope()

  LifecycleResumeEffect(
    pagerState,
    scope,
    hasLoopingEdges,
    autoScrollDurationMillis,
  ) {
    val autoSlideJob = scope.launch {
      snapshotFlow { pagerState.isScrollInProgress }
        .flatMapLatest { isScrollInProgress ->
          if (isScrollInProgress) emptyFlow() else intervalFlow(autoScrollDurationMillis)
        }
        .collectLatest {
          val currentPage = pagerState.currentPage
          val canAutoScroll = canBannerAutoScroll(
            currentPage = currentPage,
            lastIndex = pagerState.pageCount - 1,
            hasLoopingEdges = hasLoopingEdges,
          )
          if (canAutoScroll && !pagerState.isScrollInProgress) {
            pagerState.animateScrollToPage(currentPage + 1)
          }
        }
    }

    val loopEdgeJob = scope.launch {
      snapshotFlow { pagerState.settledPage }
        .mapNotNull { settledPage ->
          bannerLoopTargetPage(
            settledPage = settledPage,
            lastIndex = pagerState.pageCount - 1,
            hasLoopingEdges = hasLoopingEdges,
          )
        }
        .collectLatest(pagerState::scrollToPage)
    }

    onPauseOrDispose {
      autoSlideJob.cancel()
      loopEdgeJob.cancel()
    }
  }
}

private fun canBannerAutoScroll(currentPage: Int, lastIndex: Int, hasLoopingEdges: Boolean): Boolean =
  if (hasLoopingEdges) {
    currentPage != 1 && currentPage != lastIndex - 1 && currentPage in 0..<lastIndex
  } else {
    currentPage < lastIndex
  }

/** Page to jump to when the pager settles on a duplicated edge item, or null while inside the real range. */
private fun bannerLoopTargetPage(settledPage: Int, lastIndex: Int, hasLoopingEdges: Boolean): Int? = when {
  !hasLoopingEdges -> null
  settledPage <= 1 -> lastIndex - HomeBannerDefaults.EdgeItemCount
  settledPage >= lastIndex - 1 -> HomeBannerDefaults.EdgeItemCount
  else -> null
}

private suspend fun scrollBannerNext(pagerState: PagerState, realItemCount: Int, hasLoopingEdges: Boolean) {
  val lastRealPage = if (hasLoopingEdges) {
    HomeBannerDefaults.EdgeItemCount + realItemCount - 1
  } else {
    pagerState.pageCount - 1
  }
  if (pagerState.currentPage >= lastRealPage) return
  pagerState.animateScrollToPage(pagerState.currentPage + 1)
}

private suspend fun scrollBannerPrevious(pagerState: PagerState, hasLoopingEdges: Boolean) {
  val firstRealPage = if (hasLoopingEdges) HomeBannerDefaults.EdgeItemCount else 0
  if (pagerState.currentPage <= firstRealPage) return
  pagerState.animateScrollToPage(pagerState.currentPage - 1)
}

private fun canScrollBannerLeft(currentPage: Int, hasLoopingEdges: Boolean): Boolean =
  currentPage > if (hasLoopingEdges) HomeBannerDefaults.EdgeItemCount else 0

internal fun Int.toBannerRealIndex(realItemCount: Int, hasLoopingEdges: Boolean): Int {
  if (realItemCount <= 0) return 0
  if (!hasLoopingEdges) return coerceIn(0, realItemCount - 1)
  val shiftedIndex = this - HomeBannerDefaults.EdgeItemCount
  return (shiftedIndex % realItemCount + realItemCount) % realItemCount
}

internal fun <T> List<T>.toLoopingBannerItems(): List<T> = when {
  size <= HomeBannerDefaults.EdgeItemCount -> this

  else -> buildList(size + HomeBannerDefaults.EdgeItemCount * 2) {
    add(this@toLoopingBannerItems[this@toLoopingBannerItems.lastIndex - 1])
    add(this@toLoopingBannerItems.last())
    addAll(this@toLoopingBannerItems)
    add(this@toLoopingBannerItems.first())
    add(this@toLoopingBannerItems[1])
  }
}

private fun intervalFlow(durationMillis: Long) = flow {
  while (true) {
    delay(durationMillis)
    emit(Unit)
  }
}
