package com.congnguyencn.stream_tv.app.di

import com.congnguyencn.stream_tv.feature.setting.data.repository.BuildSettingRepository
import com.congnguyencn.stream_tv.feature.setting.domain.repository.SettingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object SettingModule {
  /** Backed by the real build and device values, so Settings reports the device it runs on. */
  @Provides
  @Singleton
  fun provideSettingRepository(): SettingRepository = BuildSettingRepository()
}
