package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AutomationDao {
    @Query("SELECT * FROM automation_rules ORDER BY id ASC")
    fun getAllRules(): Flow<List<AutomationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: AutomationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<AutomationEntity>)

    @Update
    suspend fun updateRule(rule: AutomationEntity)

    @Query("DELETE FROM automation_rules WHERE id = :id")
    suspend fun deleteRuleById(id: Long)

    @Query("UPDATE automation_rules SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setRuleEnabled(id: Long, isEnabled: Boolean)

    @Query("UPDATE automation_rules SET lastExecutedTime = :timestamp, executionCount = executionCount + 1 WHERE id = :id")
    suspend fun markExecuted(id: Long, timestamp: Long = System.currentTimeMillis())
}
