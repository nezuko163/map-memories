package com.nezuko.data

import com.nezuko.common.PageInfo
import com.nezuko.common.Paginated
import com.nezuko.domain.exception.EmptyResultException
import com.nezuko.domain.model.Location
import com.nezuko.domain.model.Memory
import com.nezuko.domain.model.User
import javax.inject.Inject

class BackendClient @Inject constructor() {
    fun getByIds(ids: List<Int>): List<Memory> {
        return ids.map { generateMemory(it) }
    }

    fun generateByLocation(
        topLeft: Location,
        topRight: Location,
        bottomLeft: Location,
        bottomRight: Location
    ): List<Memory> {
        return List(20) { generateMemory(it) }
    }

    fun generateMemory(id: Int): Memory {
        return Memory(
            id,
            name = "asd$id",
            author = User(1, " ", " "),
            photoUrl = if (id % 2 == 0) {
                "https://static.wikia.nocookie.net/naruto/images/2/23/Naruto_kyuubi.png/revision/latest?cb=20140619205704"
            } else {
                "https://i.pinimg.com/736x/ef/5e/0e/ef5e0e4d1a8ee76aeec555fa75cdd159.jpg"
            },
            photosWithText = emptyList(),
            photosUrls = emptyList(),
            tags = listOf("#футджоб", "#эщкере", "#люблю Юлечку"),
            location = Location(),
            description = "asd"
        )
    }

    fun generateIdsPaginated(
        page: Int,
        pageSize: Int = 20,
        maxPages: Int = 5
    ): Paginated<Int> {
        if (page > maxPages) throw EmptyResultException()
        val pageNumber = page.coerceAtLeast(1)
        return Paginated(
            List(pageSize) { it + (pageNumber - 1) * pageSize },
            PageInfo(
                maxPages * pageSize,
                pageNumber,
                if (pageNumber != maxPages) pageNumber + 1 else null
            )
        )
    }

    fun generatePaginated(
        page: Int,
        pageSize: Int = 20,
        maxPages: Int = 5
    ): Paginated<Memory> {
        if (page > maxPages) throw EmptyResultException()
        val pageNumber = page.coerceAtLeast(1)
        return Paginated(
            List(pageSize) { generateMemory(it + (pageNumber - 1) * pageSize) },
            PageInfo(
                maxPages * pageSize,
                pageNumber,
                if (pageNumber != maxPages) pageNumber + 1 else null
            )
        )
    }

    fun generateSuggestions(text: String): List<String> {
        return List(8) { text + it.toString() }
    }

}