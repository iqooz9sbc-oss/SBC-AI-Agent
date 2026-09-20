package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.MemoryCategory
import com.example.domain.model.MemoryItem

@Entity(tableName = "memory_items")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val category: String, // "PERSONAL", etc.
    val language: String = "en",
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
) {
    fun toDomain(): MemoryItem {
        return MemoryItem(
            id = id,
            title = title,
            content = content,
            category = try { MemoryCategory.valueOf(category) } catch (e: Exception) { MemoryCategory.PERSONAL },
            language = language,
            timestamp = timestamp,
            isPinned = isPinned
        )
    }

    companion object {
        fun fromDomain(item: MemoryItem): MemoryEntity {
            return MemoryEntity(
                id = item.id,
                title = item.title,
                content = item.content,
                category = item.category.name,
                language = item.language,
                timestamp = item.timestamp,
                isPinned = item.isPinned
            )
        }
    }
}
