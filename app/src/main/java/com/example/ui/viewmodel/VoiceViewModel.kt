package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.MemoryRepository
import com.example.data.repository.SettingsRepository
import com.example.engine.AIAgentEngine
import com.example.voice.VoiceController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class VoiceState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING
}

class VoiceViewModel(
    private val voiceController: VoiceController,
    private val settingsRepository: SettingsRepository,
    private val memoryRepository: MemoryRepository,
    private val aiEngine: AIAgentEngine
) : ViewModel() {

    val settings = settingsRepository.settings
    val isSpeakingFromController = voiceController.isSpeaking

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _transcript = MutableStateFlow("")
    val transcript: StateFlow<String> = _transcript.asStateFlow()

    private val _lastAiResponse = MutableStateFlow("")
    val lastAiResponse: StateFlow<String> = _lastAiResponse.asStateFlow()

    fun updateSpeechParams(rate: Float, pitch: Float) {
        settingsRepository.setSpeechParams(rate, pitch)
    }

    fun onSpeechResult(spokenText: String) {
        if (spokenText.isBlank()) {
            _voiceState.value = VoiceState.IDLE
            return
        }

        _transcript.value = spokenText
        _voiceState.value = VoiceState.PROCESSING

        viewModelScope.launch {
            try {
                val currentSettings = settings.value
                val memories = memoryRepository.getMemoriesSnapshot()
                val response = aiEngine.processQuery(
                    prompt = spokenText,
                    settings = currentSettings,
                    memories = memories
                )

                _lastAiResponse.value = response.content
                _voiceState.value = VoiceState.SPEAKING

                voiceController.speak(
                    text = response.content,
                    languageCode = response.language,
                    rate = currentSettings.speechRate,
                    pitch = currentSettings.speechPitch
                )
            } catch (e: Exception) {
                _lastAiResponse.value = "Sorry, error processing voice command: ${e.localizedMessage}"
                _voiceState.value = VoiceState.IDLE
            }
        }
    }

    fun testSpeak(text: String, languageCode: String) {
        val currentSettings = settings.value
        _voiceState.value = VoiceState.SPEAKING
        voiceController.speak(
            text = text,
            languageCode = languageCode,
            rate = currentSettings.speechRate,
            pitch = currentSettings.speechPitch
        )
    }

    fun stopSpeaking() {
        voiceController.stop()
        _voiceState.value = VoiceState.IDLE
    }

    fun startListeningSimulation(samplePhrase: String) {
        _voiceState.value = VoiceState.LISTENING
        _transcript.value = samplePhrase
        onSpeechResult(samplePhrase)
    }

    class Factory(
        private val voiceController: VoiceController,
        private val settingsRepository: SettingsRepository,
        private val memoryRepository: MemoryRepository,
        private val aiEngine: AIAgentEngine
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VoiceViewModel(
                voiceController,
                settingsRepository,
                memoryRepository,
                aiEngine
            ) as T
        }
    }
}
