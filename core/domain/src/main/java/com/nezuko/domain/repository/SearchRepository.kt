package com.nezuko.domain.repository

import com.nezuko.common.Paginated
import com.nezuko.domain.model.Memory

interface SearchRepository {
    suspend fun searchMemoriesByName(name: String, page: Int): Paginated<Memory>
    suspend fun suggestionForText(text: String): List<String>
}