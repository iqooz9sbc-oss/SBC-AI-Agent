package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.domain.model.AIPersona
import com.example.domain.model.AgentSettings
import com.example.domain.model.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("agent_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AgentSettings> = _settings.asStateFlow()

    private fun loadSettings(): AgentSettings {
        val isOnline = prefs.getBoolean("is_online", true)
        val langName = prefs.getString("language", AppLanguage.AUTO.name) ?: AppLanguage.AUTO.name
        val personaName = prefs.getString("persona", AIPersona.ASSISTANT.name) ?: AIPersona.ASSISTANT.name
        val apiKey = prefs.getString("custom_api_key", "") ?: ""
        val localUrl = prefs.getString("local_endpoint", "http://localhost:8080/v1") ?: "http://localhost:8080/v1"
        val speechRate = prefs.getFloat("speech_rate", 1.0f)
        val speechPitch = prefs.getFloat("speech_pitch", 1.0f)
        val autoSpeak = prefs.getBoolean("auto_speak", false)
        val useTermux = prefs.getBoolean("use_termux", false)

        return AgentSettings(
            isOnlineMode = isOnline,
            selectedLanguage = try { AppLanguage.valueOf(langName) } catch (e: Exception) { AppLanguage.AUTO },
            persona = try { AIPersona.valueOf(personaName) } catch (e: Exception) { AIPersona.ASSISTANT },
            customApiKey = apiKey,
            localEndpointUrl = localUrl,
            speechRate = speechRate,
            speechPitch = speechPitch,
            autoSpeakResponses = autoSpeak,
            useLocalTermuxServer = useTermux
        )
    }

    fun setOnlineMode(isOnline: Boolean) {
        prefs.edit().putBoolean("is_online", isOnline).apply()
        _settings.value = _settings.value.copy(isOnlineMode = isOnline)
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString("language", language.name).apply()
        _settings.value = _settings.value.copy(selectedLanguage = language)
    }

    fun setPersona(persona: AIPersona) {
        prefs.edit().putString("persona", persona.name).apply()
        _settings.value = _settings.value.copy(persona = persona)
    }

    fun setCustomApiKey(key: String) {
        prefs.edit().putString("custom_api_key", key).apply()
        _settings.value = _settings.value.copy(customApiKey = key)
    }

    fun setLocalEndpointUrl(url: String) {
        prefs.edit().putString("local_endpoint", url).apply()
        _settings.value = _settings.value.copy(localEndpointUrl = url)
    }

    fun setSpeechParams(rate: Float, pitch: Float) {
        prefs.edit().putFloat("speech_rate", rate).putFloat("speech_pitch", pitch).apply()
        _settings.value = _settings.value.copy(speechRate = rate, speechPitch = pitch)
    }

    fun setAutoSpeak(autoSpeak: Boolean) {
        prefs.edit().putBoolean("auto_speak", autoSpeak).apply()
        _settings.value = _settings.value.copy(autoSpeakResponses = autoSpeak)
    }

    fun setUseLocalTermuxServer(enabled: Boolean) {
        prefs.edit().putBoolean("use_termux", enabled).apply()
        _settings.value = _settings.value.copy(useLocalTermuxServer = enabled)
    }
}
