package com.aiassistant.app.features.headshot

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aiassistant.app.core.credits.CreditViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HeadshotViewModel(
    private val creditViewModel: CreditViewModel,
    private val repository: HeadshotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HeadshotUiState>(HeadshotUiState.Idle)
    val uiState: StateFlow<HeadshotUiState> = _uiState.asStateFlow()

    private val _selectedStyle = MutableStateFlow(HeadshotStyle.CORPORATE)
    val selectedStyle: StateFlow<HeadshotStyle> = _selectedStyle.asStateFlow()

    private val _events = MutableSharedFlow<HeadshotEvent>()
    val events: SharedFlow<HeadshotEvent> = _events.asSharedFlow()

    val creditBalance = creditViewModel.creditBalance
    val userRole = creditViewModel.userRole

    fun onImageSelected(bitmap: Bitmap) {
        _uiState.value = HeadshotUiState.ImageSelected(bitmap)
    }

    fun onImageSelectionFailed(reason: String) {
        viewModelScope.launch {
            _events.emit(HeadshotEvent.ShowSnackbar(reason))
        }
    }

    fun onStyleSelected(style: HeadshotStyle) {
        _selectedStyle.value = style
    }

    fun generateHeadshot() {
        val currentState = _uiState.value
        val inputImage = when (currentState) {
            is HeadshotUiState.ImageSelected -> currentState.inputImage
            is HeadshotUiState.Error -> currentState.inputImage
            else -> null
        } ?: return

        viewModelScope.launch {
            val isAdmin = userRole.value == "ADMIN"
            val currentCredits = creditBalance.value
            val requiredCredits = 5

            if (!isAdmin && currentCredits < requiredCredits) {
                _events.emit(HeadshotEvent.ShowPaywall)
                return@launch
            }

            _uiState.value = HeadshotUiState.Loading(inputImage)

            val result = repository.generateHeadshot(inputImage, _selectedStyle.value)

            result.fold(
                onSuccess = { outputImage ->
                    if (!isAdmin) {
                        creditViewModel.deductCredits(requiredCredits)
                    }
                    _uiState.value = HeadshotUiState.Success(inputImage, outputImage)
                },
                onFailure = { error ->
                    _uiState.value = HeadshotUiState.Error(
                        inputImage = inputImage,
                        message = error.localizedMessage ?: "Failed to generate headshot"
                    )
                }
            )
        }
    }

    fun reset() {
        _uiState.value = HeadshotUiState.Idle
    }

    fun onImageSaved(uri: Uri) {
        viewModelScope.launch {
            _events.emit(HeadshotEvent.ImageSaved(uri))
        }
    }

    companion object {
        fun provideFactory(
            creditViewModel: CreditViewModel,
            repository: HeadshotRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HeadshotViewModel(creditViewModel, repository) as T
            }
        }
    }
}

