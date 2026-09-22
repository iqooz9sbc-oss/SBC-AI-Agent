package com.yourapp.features.headshot

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yourapp.core.credits.CreditViewModel // existing ViewModel from your app
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Headshot Generator feature.
 *
 * Adapts to your existing CreditViewModel. This file assumes CreditViewModel exposes:
 *   - val userRole: StateFlow<String>            // "ADMIN" or "USER"
 *   - val creditBalance: StateFlow<Int>
 *   - suspend fun deductCredits(amount: Int): Boolean   // true on success, false if insufficient
 * If your actual CreditViewModel's API differs, adjust the three call sites marked
 * below (checkRoleAndCredits / deduction) — the generation flow itself doesn't change.
 */
class HeadshotViewModel(
    private val creditViewModel: CreditViewModel,
    private val repository: HeadshotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HeadshotUiState>(HeadshotUiState.Idle)
    val uiState: StateFlow<HeadshotUiState> = _uiState.asStateFlow()

    private val _selectedStyle = MutableStateFlow(HeadshotStyle.CORPORATE_SUIT)
    val selectedStyle: StateFlow<HeadshotStyle> = _selectedStyle.asStateFlow()

    // Re-exposed so the screen can show a live credits badge without reaching
    // into CreditViewModel directly.
    val creditBalance: StateFlow<Int> = creditViewModel.creditBalance

    private val _events = Channel<HeadshotEvent>(Channel.BUFFERED)
    val events: Flow<HeadshotEvent> = _events.receiveAsFlow()

    fun onStyleSelected(style: HeadshotStyle) {
        _selectedStyle.value = style
    }

    fun onImageSelected(bitmap: Bitmap) {
        _uiState.value = HeadshotUiState.ImageSelected(bitmap)
    }

    fun onImageSelectionFailed(reason: String) {
        _uiState.value = HeadshotUiState.Error(inputImage = null, message = reason)
    }

    fun generateHeadshot() {
        val currentState = _uiState.value
        val inputImage = when (currentState) {
            is HeadshotUiState.ImageSelected -> currentState.inputImage
            is HeadshotUiState.Success -> currentState.inputImage
            is HeadshotUiState.Error -> currentState.inputImage
            else -> null
        }

        if (inputImage == null) {
            viewModelScope.launch {
                _events.send(HeadshotEvent.ShowSnackbar("Please select a photo first."))
            }
            return
        }

        viewModelScope.launch {
            val role = creditViewModel.userRole.first()
            val isAdmin = role == "ADMIN"

            if (!isAdmin) {
                val balance = creditViewModel.creditBalance.first()
                if (balance < HEADSHOT_CREDIT_COST) {
                    _events.send(HeadshotEvent.ShowPaywall)
                    return@launch
                }
            }

            _uiState.value = HeadshotUiState.Loading(inputImage)

            val result = repository.generateHeadshot(inputImage, _selectedStyle.value)

            result.fold(
                onSuccess = { outputBitmap ->
                    if (!isAdmin) {
                        // Deduct only after a confirmed successful generation, so a
                        // failed API call never costs the user credits.
                        val deducted = creditViewModel.deductCredits(HEADSHOT_CREDIT_COST)
                        if (!deducted) {
                            // Rare race: balance changed between the check above and now
                            // (e.g. spent elsewhere in another tab/session).
                            _events.send(HeadshotEvent.ShowPaywall)
                            _uiState.value = HeadshotUiState.Error(
                                inputImage = inputImage,
                                message = "Your credit balance changed. Please top up and try again."
                            )
                            return@launch
                        }
                    }
                    _uiState.value = HeadshotUiState.Success(inputImage, outputBitmap)
                },
                onFailure = { error ->
                    val message = (error as? GeminiApiException)?.userMessage
                        ?: "Something went wrong. Please try again."
                    _uiState.value = HeadshotUiState.Error(inputImage, message)
                    _events.send(HeadshotEvent.ShowSnackbar(message))
                }
            )
        }
    }

    fun onImageSaved(uri: android.net.Uri) {
        viewModelScope.launch {
            _events.send(HeadshotEvent.ImageSaved(uri))
        }
    }

    fun reset() {
        _uiState.value = HeadshotUiState.Idle
    }

    companion object {
        /**
         * Factory so this ViewModel can take constructor dependencies while still
         * being created via the standard viewModel() Compose helper. If your app
         * uses Hilt, replace this with a normal @HiltViewModel + @Inject constructor
         * instead and drop this factory.
         */
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
