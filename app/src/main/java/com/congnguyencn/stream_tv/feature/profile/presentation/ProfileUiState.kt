package com.congnguyencn.stream_tv.feature.profile.presentation

import androidx.compose.runtime.Immutable

/**
 * The sign-in state of the Profile destination.
 *
 * Pairing is presentation-only: [pairingUrl] and [pairingCode] describe a dummy session that no
 * backend ever confirms, so the screen never transitions to a signed-in profile.
 */
@Immutable
data class ProfileUiState(
  val pairingUrl: String,
  val pairingCode: String,
  val pairingValidUntilLabel: String,
  val isPhoneSignInSelected: Boolean,
)
