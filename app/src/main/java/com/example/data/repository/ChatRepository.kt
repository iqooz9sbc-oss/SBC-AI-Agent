package com.example.data.repository

import com.example.data.local.ChatMessageDao
import com.example.data.local.ChatMessageEntity
import com.example.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRepository(private val dao: ChatMessageDao) {

    fun getMessages(): Flow<List<ChatMessage>> {
        return dao.getAllMessages().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun saveMessage(message: ChatMessage): Long {
        return dao.insertMessage(ChatMessageEntity.fromDomain(message))
    }

    suspend fun deleteMessage(id: Long) {
        dao.deleteMessageById(id)
    }

    suspend fun clearHistory() {
        dao.clearAllMessages()
    }
}
