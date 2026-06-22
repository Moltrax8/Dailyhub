package com.moltrax.personalnoteapp.ui.screen.food

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moltrax.personalnoteapp.data.local.preferences.AppPreferences
import com.moltrax.personalnoteapp.service.GeminiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FoodScannerState(
    val image: Bitmap? = null,
    val result: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class FoodScannerViewModel @Inject constructor(
    private val geminiService: GeminiService,
    private val prefs: AppPreferences,
) : ViewModel() {

    private val _state = MutableStateFlow(FoodScannerState())
    val state: StateFlow<FoodScannerState> = _state.asStateFlow()

    fun setImage(bitmap: Bitmap) {
        _state.update { it.copy(image = bitmap, result = null, error = null) }
    }

    fun analyse() {
        val image = _state.value.image ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val key = prefs.geminiApiKey.first()
            if (key.isBlank()) {
                _state.update { it.copy(isLoading = false, error = "Gemini API anahtarı ayarlanmamış.") }
                return@launch
            }
            runCatching { geminiService.analyseFood(image, key) }
                .onSuccess { result -> _state.update { it.copy(isLoading = false, result = result) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun reset() { _state.update { FoodScannerState() } }
}
