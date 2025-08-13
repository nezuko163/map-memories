package com.nezuko.data.repository

import com.nezuko.common.PageInfo
import com.nezuko.common.Paginated
import com.nezuko.data.BackendClient
import com.nezuko.domain.model.Memory
import com.nezuko.domain.repository.SearchRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val client: BackendClient
) : SearchRepository {
    override suspend fun searchMemoriesByName(
        name: String,
        page: Int
    ): Paginated<Memory> {
        delay(100)
        return client.generatePaginated(page)

    }
    override suspend fun suggestionForText(text: String): List<String> {
        return client.generateSuggestions(text)
    }
}