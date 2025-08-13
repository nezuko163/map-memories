package com.nezuko.data

import com.nezuko.common.PageInfo
import com.nezuko.common.Paginated
import com.nezuko.domain.exception.EmptyResultException
import com.nezuko.domain.model.Location
import com.nezuko.domain.model.Memory
import javax.inject.Inject

class BackendClient @Inject constructor() {
    fun generateMemory(id: Int): Memory {
        return Memory(
            id, if (id % 2 == 0) {
                "https://static.wikia.nocookie.net/naruto/images/2/23/Naruto_kyuubi.png/revision/latest?cb=20140619205704"
            } else {
                "https://i.pinimg.com/736x/ef/5e/0e/ef5e0e4d1a8ee76aeec555fa75cdd159.jpg"
            },
            "asd$id",
            location = Location(),
            "asd"
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