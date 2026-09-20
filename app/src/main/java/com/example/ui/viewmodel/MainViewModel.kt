package com.example.ui.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.repository.SettingsRepository
import com.example.domain.model.AgentSettings
import com.example.domain.model.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class NavTab(
    val titleEn: String,
    val titleBn: String,
    val icon: ImageVector,
    val testTag: String
) {
    CHAT("Chat", "চ্যাট", Icons.Default.ChatBubble, "tab_chat"),
    MEMORY("Memory", "মেমোরি", Icons.Default.Psychology, "tab_memory"),
    VOICE("Voice", "ভয়েস", Icons.Default.Mic, "tab_voice"),
    VISION("Vision", "ভিশন", Icons.Default.Visibility, "tab_vision"),
    AUTOMATION("Automation", "অটোমেশন", Icons.Default.SmartToy, "tab_automation"),
    SETTINGS("Settings", "সেটিংস", Icons.Default.Settings, "tab_settings")
}

class MainViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _currentTab = MutableStateFlow(NavTab.CHAT)
    val currentTab: StateFlow<NavTab> = _currentTab.asStateFlow()

    val settings: StateFlow<AgentSettings> = settingsRepository.settings

    fun selectTab(tab: NavTab) {
        _currentTab.value = tab
    }

    fun toggleOnlineMode() {
        val current = settings.value.isOnlineMode
        settingsRepository.setOnlineMode(!current)
    }

    fun toggleLanguage() {
        val nextLang = when (settings.value.selectedLanguage) {
            AppLanguage.ENGLISH -> AppLanguage.BANGLA
            AppLanguage.BANGLA -> AppLanguage.BANGLISH
            AppLanguage.BANGLISH -> AppLanguage.AUTO
            AppLanguage.AUTO -> AppLanguage.ENGLISH
        }
        settingsRepository.setLanguage(nextLang)
    }

    class Factory(private val settingsRepository: SettingsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(settingsRepository) as T
        }
    }
}
