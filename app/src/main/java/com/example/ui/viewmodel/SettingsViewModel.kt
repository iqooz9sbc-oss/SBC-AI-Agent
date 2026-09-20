package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ChatRepository
import com.example.data.repository.CreditRepository
import com.example.data.repository.MemoryRepository
import com.example.data.repository.SettingsRepository
import com.example.domain.model.AIPersona
import com.example.domain.model.AgentSettings
import com.example.domain.model.AppLanguage
import com.example.domain.model.CreditPackage
import com.example.domain.model.SubscriptionTier
import com.example.domain.model.UserCreditProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val chatRepository: ChatRepository,
    private val memoryRepository: MemoryRepository,
    private val creditRepository: CreditRepository
) : ViewModel() {

    val settings: StateFlow<AgentSettings> = settingsRepository.settings

    val creditProfile: StateFlow<UserCreditProfile> = creditRepository.creditProfile

    val creditPackages: List<CreditPackage> = creditRepository.availableCreditPackages

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun setOnlineMode(enabled: Boolean) {
        settingsRepository.setOnlineMode(enabled)
    }

    fun setLanguage(language: AppLanguage) {
        settingsRepository.setLanguage(language)
    }

    fun setPersona(persona: AIPersona) {
        settingsRepository.setPersona(persona)
    }

    fun setApiKey(key: String) {
        settingsRepository.setCustomApiKey(key)
        _statusMessage.value = "API key updated."
    }

    fun setLocalEndpointUrl(url: String) {
        settingsRepository.setLocalEndpointUrl(url)
    }

    fun setUseLocalTermuxServer(enabled: Boolean) {
        settingsRepository.setUseLocalTermuxServer(enabled)
    }

    fun setAutoSpeak(enabled: Boolean) {
        settingsRepository.setAutoSpeak(enabled)
    }

    fun purchaseCredits(pkg: CreditPackage) {
        creditRepository.purchaseCredits(pkg)
        _statusMessage.value = "🎉 Successfully purchased +${pkg.credits} AI Credits!"
    }

    fun upgradeSubscription(tier: SubscriptionTier) {
        creditRepository.upgradeSubscription(tier)
        _statusMessage.value = "🚀 Upgraded to ${tier.displayNameEn}! Added monthly credits."
    }

    fun toggleCurrency() {
        creditRepository.toggleCurrency()
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            chatRepository.clearHistory()
            _statusMessage.value = "Chat history cleared."
        }
    }

    fun clearMemories() {
        viewModelScope.launch {
            memoryRepository.clearAll()
            _statusMessage.value = "All saved memories cleared."
        }
    }

    fun dismissStatusMessage() {
        _statusMessage.value = null
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
        private val chatRepository: ChatRepository,
        private val memoryRepository: MemoryRepository,
        private val creditRepository: CreditRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(
                settingsRepository,
                chatRepository,
                memoryRepository,
                creditRepository
            ) as T
        }
    }
}
