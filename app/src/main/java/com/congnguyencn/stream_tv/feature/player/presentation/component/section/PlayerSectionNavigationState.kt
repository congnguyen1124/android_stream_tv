package com.congnguyencn.stream_tv.feature.player.presentation.component.section

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Section and focus-transition state shared by both player orientations.
 *
 * Parent sections remain in [sectionLayers] while a child is active. Focus is parked during every
 * enter transition and while a child exits, so a disappearing control can never trigger Compose's
 * spatial fallback into the video surface or another list item.
 */
@Stable
internal class PlayerSectionNavigationState {
  private val sectionStack = mutableStateListOf<PlayerSection>()

  var exitingSection: PlayerSection? by mutableStateOf(null)
    private set

  var isEnteringSection: Boolean by mutableStateOf(false)
    private set

  val panelSection: PlayerSection?
    get() = exitingSection ?: sectionStack.lastOrNull()

  val sectionLayers: List<PlayerSection>
    get() = if (exitingSection == null) {
      sectionStack.toList()
    } else {
      sectionStack.toList() + checkNotNull(exitingSection)
    }

  val isPanelEntering: Boolean
    get() = isEnteringSection && exitingSection == null

  val isPanelExiting: Boolean
    get() = exitingSection != null

  val isPanelSettled: Boolean
    get() = panelSection != null && !isPanelEntering && !isPanelExiting

  val hasSectionInPlay: Boolean
    get() = sectionStack.isNotEmpty() || exitingSection != null

  val isAtBaseLevel: Boolean
    get() = sectionStack.isEmpty() && exitingSection == null

  val shouldParkFocus: Boolean
    get() = isPanelEntering || (isPanelExiting && sectionStack.isNotEmpty())

  val isReturningToBase: Boolean
    get() = isPanelExiting && sectionStack.isEmpty()

  fun openRoot(section: PlayerSection) {
    if (!section.isRoot || !isAtBaseLevel) return
    push(section)
  }

  fun openChild(section: PlayerSection) {
    if (isEnteringSection || exitingSection != null || !isValidChild(sectionStack.lastOrNull(), section)) return
    push(section)
  }

  private fun push(section: PlayerSection) {
    sectionStack.add(section)
    isEnteringSection = true
  }

  fun onSectionEnterFinished() {
    if (isEnteringSection) isEnteringSection = false
  }

  fun dismissCurrentSection(): Boolean {
    if (exitingSection != null || sectionStack.isEmpty()) return false

    exitingSection = sectionStack.removeAt(sectionStack.lastIndex)
    isEnteringSection = false
    return true
  }

  fun onSectionExitFinished() {
    exitingSection = null
  }

  fun reset() {
    sectionStack.clear()
    exitingSection = null
    isEnteringSection = false
  }

  private fun isValidChild(parent: PlayerSection?, child: PlayerSection): Boolean = when (child) {
    is PlayerSection.Replies -> parent == PlayerSection.Comments

    is PlayerSection.ReplyDetail ->
      parent is PlayerSection.Replies && parent.commentId == child.commentId

    is PlayerSection.SettingOptions -> parent == PlayerSection.Settings

    PlayerSection.Metadata,
    PlayerSection.Comments,
    PlayerSection.Settings,
    -> false
  }
}

@Composable
internal fun rememberPlayerSectionNavigationState(): PlayerSectionNavigationState =
  remember { PlayerSectionNavigationState() }
