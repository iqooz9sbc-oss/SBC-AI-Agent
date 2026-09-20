package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.ChatMessage
import com.example.domain.model.MessageSender

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String, // "USER", "AI", "SYSTEM"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isOnline: Boolean = true,
    val language: String = "en",
    val imageUri: String? = null,
    val modelTag: String = "AI",
    val contentBn: String? = null,
    val contentBanglish: String? = null,
    val isBossStrategyProposal: Boolean = false,
    val isStrategyExecuted: Boolean = false,
    val strategyActionId: String? = null
) {
    fun toDomain(): ChatMessage {
        return ChatMessage(
            id = id,
            sender = try { MessageSender.valueOf(sender) } catch (e: Exception) { MessageSender.AI },
            content = content,
            timestamp = timestamp,
            isOnline = isOnline,
            language = language,
            imageUri = imageUri,
            modelTag = modelTag,
            contentBn = contentBn,
            contentBanglish = contentBanglish,
            isBossStrategyProposal = isBossStrategyProposal,
            isStrategyExecuted = isStrategyExecuted,
            strategyActionId = strategyActionId
        )
    }

    companion object {
        fun fromDomain(msg: ChatMessage): ChatMessageEntity {
            return ChatMessageEntity(
                id = msg.id,
                sender = msg.sender.name,
                content = msg.content,
                timestamp = msg.timestamp,
                isOnline = msg.isOnline,
                language = msg.language,
                imageUri = msg.imageUri,
                modelTag = msg.modelTag,
                contentBn = msg.contentBn,
                contentBanglish = msg.contentBanglish,
                isBossStrategyProposal = msg.isBossStrategyProposal,
                isStrategyExecuted = msg.isStrategyExecuted,
                strategyActionId = msg.strategyActionId
            )
        }
    }
}
