package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.AutomationRule

@Entity(tableName = "automation_rules")
data class AutomationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val titleBn: String,
    val description: String,
    val descriptionBn: String,
    val triggerText: String,
    val triggerTextBn: String,
    val actionText: String,
    val actionTextBn: String,
    val isEnabled: Boolean = true,
    val lastExecutedTime: Long? = null,
    val executionCount: Int = 0
) {
    fun toDomain(): AutomationRule {
        return AutomationRule(
            id = id,
            title = title,
            titleBn = titleBn,
            description = description,
            descriptionBn = descriptionBn,
            triggerText = triggerText,
            triggerTextBn = triggerTextBn,
            actionText = actionText,
            actionTextBn = actionTextBn,
            isEnabled = isEnabled,
            lastExecutedTime = lastExecutedTime,
            executionCount = executionCount
        )
    }

    companion object {
        fun fromDomain(rule: AutomationRule): AutomationEntity {
            return AutomationEntity(
                id = rule.id,
                title = rule.title,
                titleBn = rule.titleBn,
                description = rule.description,
                descriptionBn = rule.descriptionBn,
                triggerText = rule.triggerText,
                triggerTextBn = rule.triggerTextBn,
                actionText = rule.actionText,
                actionTextBn = rule.actionTextBn,
                isEnabled = rule.isEnabled,
                lastExecutedTime = rule.lastExecutedTime,
                executionCount = rule.executionCount
            )
        }
    }
}
