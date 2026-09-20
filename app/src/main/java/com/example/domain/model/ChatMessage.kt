package com.example.domain.model

enum class MessageSender {
    USER,
    AI,
    SYSTEM
}

data class ChatMessage(
    val id: Long = 0,
    val sender: MessageSender,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isOnline: Boolean = true,
    val language: String = "en", // "en", "bn", "banglish"
    val imageUri: String? = null,
    val modelTag: String = if (isOnline) "Cloud AI" else "Local AI",
    val contentBn: String? = null,
    val contentBanglish: String? = null,
    val isBossStrategyProposal: Boolean = false,
    val isStrategyExecuted: Boolean = false,
    val strategyActionId: String? = null
)
