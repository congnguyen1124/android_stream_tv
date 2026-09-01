package com.congnguyencn.stream_tv.feature.player.domain.repository

import com.congnguyencn.stream_tv.feature.player.domain.model.PlayerDetails
import com.congnguyencn.stream_tv.feature.player.domain.model.PlayerDetailsRequest

fun interface PlayerDetailsRepository {
  fun getDetails(request: PlayerDetailsRequest): PlayerDetails
}
