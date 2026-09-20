package com.example.domain.model

enum class ProviderFormat { OPENAI_COMPATIBLE, ANTHROPIC }

data class AiProviderInfo(
    val id: String,
    val displayName: String,
    val format: ProviderFormat,
    val baseUrl: String,
    val defaultModel: String,
    val note: String
)

data class ProviderSetting(
    val apiKey: String = "",
    val model: String = "",
    val enabled: Boolean = true
)

/**
 * Fallback order after Gemini: free-tier providers first, paid providers last.
 * To add another OpenAI-compatible provider later, add one more AiProviderInfo line here.
 */
object AiProviderCatalog {
    val all: List<AiProviderInfo> = listOf(
        AiProviderInfo(
            id = "openrouter",
            displayName = "OpenRouter",
            format = ProviderFormat.OPENAI_COMPATIBLE,
            baseUrl = "https://openrouter.ai/api/v1",
            defaultModel = "openrouter/free",
            note = "One key, hundreds of models (free ones included)"
        ),
        AiProviderInfo(
            id = "groq",
            displayName = "Groq",
            format = ProviderFormat.OPENAI_COMPATIBLE,
            baseUrl = "https://api.groq.com/openai/v1",
            defaultModel = "llama-3.3-70b-versatile",
            note = "Very fast, has a free tier"
        ),
        AiProviderInfo(
            id = "openai",
            displayName = "OpenAI (ChatGPT)",
            format = ProviderFormat.OPENAI_COMPATIBLE,
            baseUrl = "https://api.openai.com/v1",
            defaultModel = "gpt-5-mini",
            note = "Paid API key (platform.openai.com)"
        ),
        AiProviderInfo(
            id = "anthropic",
            displayName = "Claude (Anthropic)",
            format = ProviderFormat.ANTHROPIC,
            baseUrl = "https://api.anthropic.com/v1",
            defaultModel = "claude-haiku-4-5-20251001",
            note = "Paid API key (console.anthropic.com)"
        )
    )
}
