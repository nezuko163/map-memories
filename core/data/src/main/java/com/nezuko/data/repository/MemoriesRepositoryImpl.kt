package com.nezuko.data.repository

import com.nezuko.common.Paginated
import com.nezuko.data.BackendClient
import com.nezuko.domain.model.Location
import com.nezuko.domain.model.Memory
import com.nezuko.domain.repository.MemoriesRepository
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject

class MemoriesRepositoryImpl @Inject constructor(
    private val client: BackendClient
) : MemoriesRepository {
    override suspend fun getNearestMemories(location: Location): Collection<Memory> {
        delay(1000)
        return List(20) { client.generateMemory(20) }
    }

    override suspend fun getMemoriesFYP(
        uid: UUID,
        page: Int
    ): Paginated<Memory> {
        delay(100)
        return client.generatePaginated(page, maxPages = 4)
    }
}