package com.nezuko.domain.repository

import com.nezuko.common.Paginated
import com.nezuko.domain.model.EmptyModel

interface EmptyRepository {
    suspend fun exampleList(page: Int): Paginated<EmptyModel>
}