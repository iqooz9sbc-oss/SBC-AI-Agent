package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.MemoryRepository
import com.example.domain.model.MemoryCategory
import com.example.domain.model.MemoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MemoryViewModel(
    private val memoryRepository: MemoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<MemoryCategory?>(null)
    val selectedCategory: StateFlow<MemoryCategory?> = _selectedCategory.asStateFlow()

    private val rawMemories = memoryRepository.getAllMemories()

    val filteredMemories: StateFlow<List<MemoryItem>> = combine(
        rawMemories,
        _searchQuery,
        _selectedCategory
    ) { memories, query, category ->
        memories.filter { item ->
            val matchesCategory = category == null || item.category == category
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.content.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Seed helpful initial personal memories if database is empty
        viewModelScope.launch {
            val current = memoryRepository.getMemoriesSnapshot()
            if (current.isEmpty()) {
                memoryRepository.addMemory(
                    MemoryItem(
                        title = "Favorite Programming Language",
                        content = "Kotlin & Jetpack Compose for Android",
                        category = MemoryCategory.PREFERENCE,
                        isPinned = true
                    )
                )
                memoryRepository.addMemory(
                    MemoryItem(
                        title = "বিকাশকারী পরিচিতি (Developer Bio)",
                        content = "সফটওয়্যার ডেভেলপার ও এআই প্রযুক্তি প্রেমী",
                        category = MemoryCategory.PERSONAL,
                        language = "bn",
                        isPinned = true
                    )
                )
                memoryRepository.addMemory(
                    MemoryItem(
                        title = "Preferred Response Style",
                        content = "Friendly, bilingual in English and Bangla with actionable code examples.",
                        category = MemoryCategory.NOTE
                    )
                )
            }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategorySelected(category: MemoryCategory?) {
        _selectedCategory.value = category
    }

    fun addMemory(title: String, content: String, category: MemoryCategory, language: String) {
        if (title.isBlank() || content.isBlank()) return
        viewModelScope.launch {
            memoryRepository.addMemory(
                MemoryItem(
                    title = title.trim(),
                    content = content.trim(),
                    category = category,
                    language = language
                )
            )
        }
    }

    fun togglePin(item: MemoryItem) {
        viewModelScope.launch {
            memoryRepository.updateMemory(item.copy(isPinned = !item.isPinned))
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            memoryRepository.deleteMemory(id)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            memoryRepository.clearAll()
        }
    }

    class Factory(private val memoryRepository: MemoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MemoryViewModel(memoryRepository) as T
        }
    }
}
