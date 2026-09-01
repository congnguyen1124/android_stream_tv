package com.congnguyencn.stream_tv.feature.player.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.congnguyencn.stream_tv.feature.player.presentation.component.PlayerScreen

/**
 * Binds [PlayerViewModel] to [PlayerScreen] for landscape playback.
 */
@Composable
internal fun PlayerRoute(
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: PlayerViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  PausePlaybackWhenStopped(viewModel = viewModel)

  BackHandler(onBack = onBack)

  PlayerScreen(
    uiState = uiState,
    playerManager = viewModel.playerManager,
    onTogglePlayPause = viewModel::togglePlayPause,
    onSeekForward = viewModel::seekForward,
    onSeekBack = viewModel::seekBack,
    onRetry = viewModel::retryPlayback,
    modifier = modifier,
  )
}
