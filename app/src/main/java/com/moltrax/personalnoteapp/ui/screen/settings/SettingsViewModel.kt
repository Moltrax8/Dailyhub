package com.moltrax.personalnoteapp.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moltrax.personalnoteapp.data.local.preferences.AppPreferences
import com.moltrax.personalnoteapp.data.remote.drive.DriveAuthService
import com.moltrax.personalnoteapp.domain.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: AppPreferences,
    private val authService: DriveAuthService,
    private val syncRepo: SyncRepository,
) : ViewModel() {

    val themeMode      = prefs.themeMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "system")
    val geminiApiKey   = prefs.geminiApiKey.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")
    val reminderMinutes = prefs.reminderMinutes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 60)
    val lastSyncAt     = prefs.lastSyncAt.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setTheme(mode: String) = viewModelScope.launch { prefs.setThemeMode(mode) }
    fun setGeminiKey(key: String) = viewModelScope.launch { prefs.setGeminiApiKey(key) }
    fun setReminderMinutes(m: Int) = viewModelScope.launch { prefs.setReminderMinutes(m) }
    fun syncNow() = viewModelScope.launch { syncRepo.pushToDrive() }
    fun pullNow() = viewModelScope.launch { syncRepo.pullFromDrive() }

    fun signOut(onDone: () -> Unit) {
        viewModelScope.launch {
            authService.signOut()
            prefs.setSignedIn(false)
            onDone()
        }
    }
}
