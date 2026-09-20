package com.example.domain.model

enum class MemoryCategory(val displayNameEn: String, val displayNameBn: String) {
    PERSONAL("Personal", "ব্যক্তিগত"),
    PREFERENCE("Preference", "পছন্দ"),
    WORK("Work / Task", "কাজ / টাস্ক"),
    NOTE("Knowledge / Note", "জ্ঞান / নোট"),
    FACT("Custom Fact", "তথ্য")
}

data class MemoryItem(
    val id: Long = 0,
    val title: String,
    val content: String,
    val category: MemoryCategory = MemoryCategory.PERSONAL,
    val language: String = "en",
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)
