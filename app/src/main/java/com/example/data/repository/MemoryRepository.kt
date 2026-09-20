package com.example.data.repository

import com.example.data.local.MemoryDao
import com.example.data.local.MemoryEntity
import com.example.domain.model.MemoryCategory
import com.example.domain.model.MemoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MemoryRepository(private val dao: MemoryDao) {

    fun getAllMemories(): Flow<List<MemoryItem>> {
        return dao.getAllMemories().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getMemoriesSnapshot(): List<MemoryItem> {
        return dao.getMemoriesSnapshot().map { it.toDomain() }
    }

    fun getMemoriesByCategory(category: MemoryCategory): Flow<List<MemoryItem>> {
        return dao.getMemoriesByCategory(category.name).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun addMemory(item: MemoryItem): Long {
        return dao.insertMemory(MemoryEntity.fromDomain(item))
    }

    suspend fun updateMemory(item: MemoryItem) {
        dao.updateMemory(MemoryEntity.fromDomain(item))
    }

    suspend fun deleteMemory(id: Long) {
        dao.deleteMemoryById(id)
    }

    suspend fun clearAll() {
        dao.clearAllMemories()
    }
}
