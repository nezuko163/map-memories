package com.nezuko.domain.repository

import com.nezuko.common.Paginated
import com.nezuko.domain.model.Location
import com.nezuko.domain.model.Memory
import java.util.UUID

interface MemoriesRepository {
    suspend fun getNearestMemories(location: Location): Collection<Memory>
    suspend fun getMemoriesFYP(uid: UUID, page: Int): Paginated<Memory>
}