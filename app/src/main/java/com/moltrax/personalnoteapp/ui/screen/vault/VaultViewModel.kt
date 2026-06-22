package com.moltrax.personalnoteapp.ui.screen.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moltrax.personalnoteapp.domain.model.VaultEntry
import com.moltrax.personalnoteapp.domain.repository.VaultRepository
import com.moltrax.personalnoteapp.service.VaultService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface VaultUiState {
    data object Locked : VaultUiState
    data object PinSetup : VaultUiState
    data class Unlocked(val entries: List<VaultEntry>, val decryptedContent: Map<String, String> = emptyMap()) : VaultUiState
}

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val vaultRepo: VaultRepository,
    private val vaultService: VaultService,
) : ViewModel() {

    private val _uiState = MutableStateFlow<VaultUiState>(VaultUiState.Locked)
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    private var currentPin: String? = null

    private val entries = vaultRepo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            entries.collect { list ->
                val hasSentinel = list.any { it.id == "sentinel" }
                if (!hasSentinel) {
                    _uiState.update { VaultUiState.PinSetup }
                } else if (_uiState.value is VaultUiState.Unlocked) {
                    _uiState.update { (it as VaultUiState.Unlocked).copy(entries = list.filter { e -> e.id != "sentinel" }) }
                }
            }
        }
    }

    fun setupPin(pin: String) {
        viewModelScope.launch {
            vaultRepo.upsert(vaultService.createSentinel(pin))
            currentPin = pin
            _uiState.update { VaultUiState.Unlocked(entries.value.filter { it.id != "sentinel" }) }
        }
    }

    fun unlock(pin: String): Boolean {
        val sentinel = entries.value.find { it.id == "sentinel" } ?: return false
        if (!vaultService.verifySentinel(sentinel, pin)) return false
        currentPin = pin
        _uiState.update { VaultUiState.Unlocked(entries.value.filter { it.id != "sentinel" }) }
        return true
    }

    fun lock() {
        currentPin = null
        _uiState.update { VaultUiState.Locked }
    }

    fun addEntry(title: String, content: String) {
        val pin = currentPin ?: return
        viewModelScope.launch {
            vaultRepo.upsert(vaultService.createEntry(title, content, pin))
        }
    }

    fun decrypt(entry: VaultEntry): String? {
        val pin = currentPin ?: return null
        return vaultService.decrypt(entry.encryptedContent, entry.iv, pin)
    }

    fun deleteEntry(id: String) { viewModelScope.launch { vaultRepo.delete(id) } }
}
