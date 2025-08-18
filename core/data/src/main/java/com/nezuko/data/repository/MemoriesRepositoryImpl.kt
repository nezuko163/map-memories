package com.nezuko.data.repository

import com.nezuko.common.Paginated
import com.nezuko.data.BackendClient
import com.nezuko.domain.model.Location
import com.nezuko.domain.model.Memory
import com.nezuko.domain.repository.MemoriesRepository
import kotlinx.coroutines.delay
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class MemoriesRepositoryImpl @Inject constructor(
    private val client: BackendClient
) : MemoriesRepository {
    private val cache = ConcurrentHashMap<Int, Memory>()

    override suspend fun getNearestMemories(location: Location): Collection<Memory> {
        delay(1000)
        return List(20) { client.generateMemory(20) }.also {
            for (memory in it) {
                cache.computeIfAbsent(memory.id) { id -> memory }
            }
        }
    }

    override suspend fun getMemoriesByBounds(
        topLeft: Location,
        topRight: Location,
        bottomLeft: Location,
        bottomRight: Location
    ): List<Memory> {
        return client.generateByLocation(
            topLeft = topLeft,
            topRight = topRight,
            bottomLeft = bottomLeft,
            bottomRight = bottomRight
        )
    }

    override suspend fun getMemoriesFYP(
        uid: UUID,
        page: Int
    ): Paginated<Memory> {
        delay(100)
        return client.generatePaginated(page, maxPages = 4).also {
            for (memory in it.items) {
                cache.putIfAbsent(memory.id, memory)
            }
        }
    }

    override suspend fun loadMemories(memoriesIds: List<Int>) {
        for (memory in client.getByIds(memoriesIds)) {
            cache.putIfAbsent(memory.id, memory)
        }
    }

    override suspend fun getMemoryById(memoryId: Int): Memory {
        return cache[memoryId] ?: client.getByIds(listOf(memoryId)).first()
            .also { cache.putIfAbsent(memoryId, it) }
    }
}