package com.congnguyencn.stream_tv.feature.player.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.congnguyencn.stream_tv.feature.player.presentation.component.VerticalPlayerScreen

/**
 * Binds [PlayerViewModel] to [VerticalPlayerScreen] for portrait playback.
 *
 * Shares the ViewModel type with [PlayerRoute] — the playback logic is identical, only the framing
 * differs — while still getting its own instance, since this is a separate destination.
 */
@Composable
internal fun VerticalPlayerRoute(
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: PlayerViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  PausePlaybackWhenStopped(viewModel = viewModel)

  BackHandler(onBack = onBack)

  VerticalPlayerScreen(
    uiState = uiState,
    playerManager = viewModel.playerManager,
    onTogglePlayPause = viewModel::togglePlayPause,
    onRetry = viewModel::retryPlayback,
    modifier = modifier,
  )
}
