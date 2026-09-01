package com.congnguyencn.stream_tv.feature.player.presentation.mapper

import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingCategory
import com.congnguyencn.streamplayer.model.StreamTvAudioTrack
import com.congnguyencn.streamplayer.model.StreamTvTextTrack
import com.congnguyencn.streamplayer.model.StreamTvVideoTrack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerSettingsUiMapperTest {
  @Test
  fun `maps all available tracks into the shared ordered settings tree`() {
    val state = buildPlayerSettingsUiState(
      audioTracks = listOf(
        audioTrack(id = "en", label = "English", isDefault = true, isSelected = true),
        audioTrack(id = "ja", label = "Japanese", isDefault = false, isSelected = false),
      ),
      textTracks = listOf(
        textTrack(id = "cc-en", label = "English CC", isSelected = false),
      ),
      videoTracks = listOf(
        videoTrack(id = "1080", height = 1080, isSelected = true),
        videoTrack(id = "720", height = 720, isSelected = true),
      ),
    )

    assertEquals(
      listOf(
        PlayerSettingCategory.Quality,
        PlayerSettingCategory.Subtitles,
        PlayerSettingCategory.Audio,
      ),
      state.items.map { item -> item.category },
    )
    assertEquals("Auto", state.item(PlayerSettingCategory.Quality)?.selectedLabel)
    assertEquals("Off", state.item(PlayerSettingCategory.Subtitles)?.selectedLabel)
    assertEquals("English · Original", state.item(PlayerSettingCategory.Audio)?.selectedLabel)
  }

  @Test
  fun `marks one concrete rendition selected instead of auto`() {
    val state = buildPlayerSettingsUiState(
      audioTracks = emptyList(),
      textTracks = emptyList(),
      videoTracks = listOf(
        videoTrack(id = "1080", height = 1080, isSelected = true),
        videoTrack(id = "720", height = 720, isSelected = false),
      ),
    )

    val quality = requireNotNull(state.item(PlayerSettingCategory.Quality))
    assertEquals("1080p", quality.selectedLabel)
    assertFalse(quality.options.first().isSelected)
    assertTrue(quality.options.first { option -> option.id == "1080" }.isSelected)
  }

  @Test
  fun `omits settings when the player publishes no selectable tracks`() {
    val state = buildPlayerSettingsUiState(
      audioTracks = emptyList(),
      textTracks = emptyList(),
      videoTracks = emptyList(),
    )

    assertFalse(state.isAvailable)
  }

  private fun audioTrack(id: String, label: String, isDefault: Boolean, isSelected: Boolean): StreamTvAudioTrack =
    StreamTvAudioTrack(
      id = id,
      language = id,
      label = label,
      isDefaultSelected = isDefault,
      isSelected = isSelected,
    )

  private fun textTrack(id: String, label: String, isSelected: Boolean): StreamTvTextTrack = StreamTvTextTrack(
    id = id,
    language = id,
    label = label,
    isSelected = isSelected,
  )

  private fun videoTrack(id: String, height: Int, isSelected: Boolean): StreamTvVideoTrack = StreamTvVideoTrack(
    id = id,
    width = height * 16 / 9,
    height = height,
    bitrate = height * 1_000,
    isSelected = isSelected,
  )
}
