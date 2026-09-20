package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memory_items ORDER BY isPinned DESC, timestamp DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memory_items")
    suspend fun getMemoriesSnapshot(): List<MemoryEntity>

    @Query("SELECT * FROM memory_items WHERE category = :category ORDER BY isPinned DESC, timestamp DESC")
    fun getMemoriesByCategory(category: String): Flow<List<MemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(item: MemoryEntity): Long

    @Update
    suspend fun updateMemory(item: MemoryEntity)

    @Query("DELETE FROM memory_items WHERE id = :id")
    suspend fun deleteMemoryById(id: Long)

    @Query("DELETE FROM memory_items")
    suspend fun clearAllMemories()
}
