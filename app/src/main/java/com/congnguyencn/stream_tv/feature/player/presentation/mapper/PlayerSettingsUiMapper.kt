package com.congnguyencn.stream_tv.feature.player.presentation.mapper

import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingCategory
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingOptionUiItem
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingUiItem
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingsUiState
import com.congnguyencn.streamplayer.model.StreamTvAudioTrack
import com.congnguyencn.streamplayer.model.StreamTvTextTrack
import com.congnguyencn.streamplayer.model.StreamTvVideoTrack
import java.util.Locale

/** Maps the player library's track snapshot into the UI tree used by both player orientations. */
internal fun buildPlayerSettingsUiState(
  audioTracks: List<StreamTvAudioTrack>,
  textTracks: List<StreamTvTextTrack>,
  videoTracks: List<StreamTvVideoTrack>,
): PlayerSettingsUiState = PlayerSettingsUiState(
  items = buildList {
    videoTracks.toQualitySetting()?.let(::add)
    textTracks.toSubtitleSetting()?.let(::add)
    audioTracks.toAudioSetting()?.let(::add)
  },
)

private fun List<StreamTvVideoTrack>.toQualitySetting(): PlayerSettingUiItem? {
  if (isEmpty()) return null

  val selectedTrackCount = count(StreamTvVideoTrack::isSelected)
  val isAutoSelected = size > 1 && selectedTrackCount != 1
  val concreteOptions = sortedWith(
    compareByDescending<StreamTvVideoTrack> { track -> track.height }
      .thenByDescending { track -> track.bitrate },
  ).map { track ->
    PlayerSettingOptionUiItem(
      id = track.id,
      label = track.qualityLabel(),
      isSelected = track.isSelected && !isAutoSelected,
    )
  }
  val options = if (size > 1) {
    listOf(
      PlayerSettingOptionUiItem(
        id = StreamTvVideoTrack.AUTO_ID,
        label = AUTO_LABEL,
        isSelected = isAutoSelected,
      ),
    ) + concreteOptions
  } else {
    concreteOptions
  }

  return PlayerSettingUiItem(
    category = PlayerSettingCategory.Quality,
    selectedLabel = options.selectedOrFirstLabel(),
    options = options,
  )
}

private fun List<StreamTvTextTrack>.toSubtitleSetting(): PlayerSettingUiItem? {
  if (isEmpty()) return null

  val options = buildList<PlayerSettingOptionUiItem> {
    add(
      PlayerSettingOptionUiItem(
        id = StreamTvTextTrack.OFF_ID,
        label = OFF_LABEL,
        isSelected = this@toSubtitleSetting.none(StreamTvTextTrack::isSelected),
      ),
    )
    this@toSubtitleSetting.forEach { track ->
      add(
        PlayerSettingOptionUiItem(
          id = track.id,
          label = track.label.displayTrackName(language = track.language),
          isSelected = track.isSelected,
        ),
      )
    }
  }

  return PlayerSettingUiItem(
    category = PlayerSettingCategory.Subtitles,
    selectedLabel = options.selectedOrFirstLabel(),
    options = options,
  )
}

private fun List<StreamTvAudioTrack>.toAudioSetting(): PlayerSettingUiItem? {
  if (isEmpty()) return null

  val options = map { track ->
    val name = track.label.displayTrackName(language = track.language)
    PlayerSettingOptionUiItem(
      id = track.id,
      label = if (track.isDefaultSelected) "$name · $ORIGINAL_LABEL" else name,
      isSelected = track.isSelected,
    )
  }

  return PlayerSettingUiItem(
    category = PlayerSettingCategory.Audio,
    selectedLabel = options.selectedOrFirstLabel(),
    options = options,
  )
}

private fun StreamTvVideoTrack.qualityLabel(): String = when {
  height > 0 -> "${height}p"
  bitrate > 0 -> "${bitrate / BITS_PER_KILOBIT} kbps"
  else -> "Video"
}

private fun String.displayTrackName(language: String): String = trim()
  .ifBlank { language.trim().uppercase(Locale.ROOT) }
  .ifBlank { UNKNOWN_LABEL }

private fun List<PlayerSettingOptionUiItem>.selectedOrFirstLabel(): String =
  firstOrNull(PlayerSettingOptionUiItem::isSelected)?.label.orEmpty().ifBlank {
    firstOrNull()?.label.orEmpty()
  }

private const val BITS_PER_KILOBIT = 1_000
private const val AUTO_LABEL = "Auto"
private const val OFF_LABEL = "Off"
private const val ORIGINAL_LABEL = "Original"
private const val UNKNOWN_LABEL = "Unknown"
