package com.example.domain.model

data class AutomationRule(
    val id: Long = 0,
    val title: String,
    val titleBn: String = title,
    val description: String,
    val descriptionBn: String = description,
    val triggerText: String,
    val triggerTextBn: String = triggerText,
    val actionText: String,
    val actionTextBn: String = actionText,
    val isEnabled: Boolean = true,
    val lastExecutedTime: Long? = null,
    val executionCount: Int = 0
)
