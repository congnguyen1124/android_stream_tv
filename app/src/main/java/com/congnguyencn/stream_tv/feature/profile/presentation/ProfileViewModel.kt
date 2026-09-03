package com.congnguyencn.stream_tv.feature.profile.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
internal class ProfileViewModel @Inject constructor() : ViewModel() {
  private val mutableUiState = MutableStateFlow(
    ProfileUiState(
      pairingUrl = "$DummyPairingBaseUrl?code=$DummyPairingCode",
      pairingCode = DummyPairingCode,
      pairingValidUntilLabel = DummyPairingValidUntilLabel,
      isPhoneSignInSelected = false,
    ),
  )
  val uiState: StateFlow<ProfileUiState> = mutableUiState.asStateFlow()

  fun selectPhoneSignIn() {
    mutableUiState.update { it.copy(isPhoneSignInSelected = true) }
  }

  private companion object {
    /**
     * A placeholder pairing session. The dummy values are constant so that the QR symbol, the
     * printed code, and the validity label stay in agreement and every render is reproducible; a
     * real session would be issued per device and would expire.
     */
    const val DummyPairingBaseUrl = "https://tv.streamtv.example.com/pair"
    const val DummyPairingCode = "XHSZ-QBKX"
    const val DummyPairingValidUntilLabel = "14:17"
  }
}
