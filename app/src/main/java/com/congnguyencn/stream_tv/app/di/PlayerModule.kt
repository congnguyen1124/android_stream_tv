package com.congnguyencn.stream_tv.app.di

import android.content.Context
import com.congnguyencn.stream_tv.feature.player.data.repository.DummyPlayerDetailsRepository
import com.congnguyencn.stream_tv.feature.player.data.source.PlayerDummyDataSource
import com.congnguyencn.stream_tv.feature.player.domain.repository.PlayerDetailsRepository
import com.congnguyencn.stream_tv.feature.player.presentation.StreamTvPlayerFactory
import com.congnguyencn.streamplayer.StreamTvPlayerManager
import com.congnguyencn.streamplayer.config.StreamTvPlayerConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object PlayerModule {
  @Provides
  @Singleton
  fun providePlayerDummyDataSource(): PlayerDummyDataSource = PlayerDummyDataSource()

  @Provides
  @Singleton
  fun providePlayerDetailsRepository(dataSource: PlayerDummyDataSource): PlayerDetailsRepository =
    DummyPlayerDetailsRepository(dataSource)

  /**
   * Supplies players configured for this app's single surface.
   *
   * [StreamTvPlayerConfig.Tv] because every destination here plays one item to its end: steady
   * buffering beats a fast start, and with no item ever re-entered there is nothing for a cache to
   * make faster — so skipping it keeps background writes off the same connection as the video.
   *
   * Switch to [StreamTvPlayerConfig.Feed] if a swipeable shorts feed lands, where the opposite
   * trade-off applies. Nothing else has to change.
   */
  @Provides
  @Singleton
  fun provideStreamTvPlayerFactory(@ApplicationContext context: Context): StreamTvPlayerFactory =
    object : StreamTvPlayerFactory {
      override fun create(): StreamTvPlayerManager = StreamTvPlayerManager.create(
        context = context,
        config = StreamTvPlayerConfig.Tv,
      )
    }
}
