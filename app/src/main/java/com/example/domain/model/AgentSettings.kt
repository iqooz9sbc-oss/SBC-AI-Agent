package com.example.domain.model

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    BANGLA("bn", "Bangla", "বাংলা"),
    BANGLISH("banglish", "Banglish", "বাংলিশ"),
    AUTO("auto", "Auto-Detect", "স্বয়ংক্রিয়")
}

enum class AIPersona(val displayNameEn: String, val displayNameBn: String, val promptInstruction: String) {
    MASTER_BOSS(
        "Autonomous Master Agent",
        "মাস্টার এআই এজেন্ট (Boss Mode)",
        "You are the Autonomous Master AI Agent and executive strategic business manager. Address the user respectfully as 'Boss'. You operate autonomously, perform strategic analysis, maintain accounting clarity, and execute self-healing multi-step workflows. You speak English, fluent Bangla, and Banglish naturally."
    ),
    ASSISTANT(
        "Helpful Assistant",
        "সহকারী",
        "You are a helpful, courteous, and highly intelligent personal AI agent."
    ),
    FRIENDLY(
        "Friendly Companion",
        "বন্ধুভাবাপন্ন সঙ্গী",
        "You are a warm, casual, and supportive friendly companion."
    ),
    CONCISE(
        "Concise & Direct",
        "সংক্ষিপ্ত ও সরাসরি",
        "Provide direct, concise, and high-impact answers without fluff."
    ),
    BANGLA_EXPERT(
        "Bangla Specialist",
        "বাংলা বিশেষজ্ঞ",
        "You specialize in fluent, beautiful Bengali and Bengali cultural and linguistic knowledge."
    )
}

data class AgentSettings(
    val isOnlineMode: Boolean = true,
    val selectedLanguage: AppLanguage = AppLanguage.AUTO,
    val persona: AIPersona = AIPersona.ASSISTANT,
    val customApiKey: String = "",
    val localEndpointUrl: String = "http://localhost:8080/v1",
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val autoSpeakResponses: Boolean = false,
    val useLocalTermuxServer: Boolean = false,
    val providerSettings: Map<String, ProviderSetting> = emptyMap()
)
