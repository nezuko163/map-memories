package com.nezuko.data.repository

import com.nezuko.common.PageInfo
import com.nezuko.common.Paginated
import com.nezuko.domain.exception.EmptyResultException
import com.nezuko.domain.model.EmptyModel
import com.nezuko.domain.repository.EmptyRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

class EmptyRepositoryImpl @Inject constructor() : EmptyRepository {
    override suspend fun exampleList(page: Int): Paginated<EmptyModel> {
        delay(1000)
        if (page > 10) throw EmptyResultException()
        when (page) {
            1 -> {
                return Paginated(
                    List(20) { EmptyModel(it + page * 20) },
                    PageInfo(100, page, next = page + 1)
                )
            }
            10 -> {
                return Paginated(
                    List(20) { EmptyModel(it + page * 20) },
                    PageInfo(100, page)
                )
            }
            else -> {
                return Paginated(
                    List(20) { EmptyModel(it + page * 20) },
                    PageInfo(100, page, next = page + 1, prev = page - 1)
                )
            }
        }
    }
}