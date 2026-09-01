package com.congnguyencn.stream_tv.feature.player.presentation.component.section

import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingCategory

internal sealed interface PlayerSection {
  data object Metadata : PlayerSection

  data object Comments : PlayerSection

  data class Replies(val commentId: Long) : PlayerSection

  data class ReplyDetail(val commentId: Long, val replyId: Long) : PlayerSection

  data object Settings : PlayerSection

  data class SettingOptions(val category: PlayerSettingCategory) : PlayerSection

  val isRoot: Boolean
    get() = this == Metadata || this == Comments || this == Settings

  val isSettingTree: Boolean
    get() = this == Settings || this is SettingOptions
}
