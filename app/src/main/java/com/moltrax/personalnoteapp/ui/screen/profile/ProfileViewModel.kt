package com.moltrax.personalnoteapp.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moltrax.personalnoteapp.data.local.preferences.AppPreferences
import com.moltrax.personalnoteapp.data.remote.drive.DriveAuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ProfileUiState(
    /** Görünen ad — kullanıcının elle düzenlediği ad; yoksa Google hesabı adı; o da yoksa varsayılan. */
    val displayName: String = "Kullanıcı",
    val photoUrl: String? = null,
)

/**
 * Profil başlığının salt-okunur verisini sağlar: görünen ad ve profil fotoğrafı. Görünen ad,
 * kullanıcının ayarladığı özel adı (DataStore) önceler; ayarlı değilse Google hesabı adına düşülür.
 * Düzenleme/çıkış işlevleri [com.moltrax.personalnoteapp.ui.screen.settings.SettingsViewModel]'de.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    prefs: AppPreferences,
    private val authService: DriveAuthService,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> =
        prefs.displayName.map { custom ->
            val account = authService.getLastSignedInAccount()
            ProfileUiState(
                displayName = custom
                    ?: account?.displayName?.takeIf { it.isNotBlank() }
                    ?: "Kullanıcı",
                photoUrl = account?.photoUrl?.toString(),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())
}
