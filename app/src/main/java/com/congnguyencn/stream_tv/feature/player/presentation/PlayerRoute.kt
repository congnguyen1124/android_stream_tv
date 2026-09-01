package com.congnguyencn.stream_tv.feature.player.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.congnguyencn.stream_tv.feature.player.presentation.component.PlayerScreen
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingCategory

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

  PlayerScreen(
    uiState = uiState,
    playerManager = viewModel.playerManager,
    onTogglePlayPause = viewModel::togglePlayPause,
    onSeekForward = viewModel::seekForward,
    onSeekBack = viewModel::seekBack,
    onQualitySelected = { id -> viewModel.selectTrack(PlayerSettingCategory.Quality, id) },
    onSubtitleSelected = { id -> viewModel.selectTrack(PlayerSettingCategory.Subtitles, id) },
    onAudioSelected = { id -> viewModel.selectTrack(PlayerSettingCategory.Audio, id) },
    onRetry = viewModel::retryPlayback,
    onExitPlayer = onBack,
    modifier = modifier,
  )
}
