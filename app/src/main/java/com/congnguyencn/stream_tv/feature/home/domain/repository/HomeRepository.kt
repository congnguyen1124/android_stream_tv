package com.congnguyencn.stream_tv.feature.home.domain.repository

import com.congnguyencn.stream_tv.feature.home.domain.model.HomeSection

fun interface HomeRepository {
    fun getHomeSections(): List<HomeSection>
}
