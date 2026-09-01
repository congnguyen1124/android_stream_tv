package com.congnguyencn.stream_tv.feature.player.data.repository

import com.congnguyencn.stream_tv.feature.player.data.mapper.toDomain
import com.congnguyencn.stream_tv.feature.player.data.source.PlayerDummyDataSource
import com.congnguyencn.stream_tv.feature.player.domain.model.PlayerDetails
import com.congnguyencn.stream_tv.feature.player.domain.model.PlayerDetailsRequest
import com.congnguyencn.stream_tv.feature.player.domain.repository.PlayerDetailsRepository

internal class DummyPlayerDetailsRepository(private val dataSource: PlayerDummyDataSource) : PlayerDetailsRepository {
  override fun getDetails(request: PlayerDetailsRequest): PlayerDetails = dataSource.getDetails(request).toDomain()
}
