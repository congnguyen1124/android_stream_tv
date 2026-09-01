package com.congnguyencn.stream_tv.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
data class StreamTvTopBarItem(
  val id: String,
  @param:DrawableRes val iconResId: Int,
  @param:StringRes val titleResId: Int,
  val role: StreamTvTopBarItemRole = StreamTvTopBarItemRole.Destination,
)

enum class StreamTvTopBarItemRole {
  Destination,
  Profile,
}
