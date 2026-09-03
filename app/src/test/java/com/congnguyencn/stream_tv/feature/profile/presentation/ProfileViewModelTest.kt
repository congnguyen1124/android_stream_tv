package com.congnguyencn.stream_tv.feature.profile.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileViewModelTest {
  @Test
  fun `pairing session exposes a code the qr link carries`() {
    val uiState = ProfileViewModel().uiState.value

    assertTrue(uiState.pairingUrl.startsWith("https://"))
    assertTrue(uiState.pairingUrl.endsWith(uiState.pairingCode))
    assertEquals("14:17", uiState.pairingValidUntilLabel)
  }

  @Test
  fun `selecting phone sign in only swaps the invitation copy`() {
    val viewModel = ProfileViewModel()
    val pairingBefore = viewModel.uiState.value

    assertFalse(pairingBefore.isPhoneSignInSelected)

    viewModel.selectPhoneSignIn()

    val pairingAfter = viewModel.uiState.value
    assertTrue(pairingAfter.isPhoneSignInSelected)
    assertEquals(pairingBefore.pairingUrl, pairingAfter.pairingUrl)
    assertEquals(pairingBefore.pairingCode, pairingAfter.pairingCode)
  }
}
