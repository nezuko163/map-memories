package com.nezuko.ui.components

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.nezuko.common.Paginated

class CustomPagingSource <T : Any> (
    private val sourceQuery: suspend (page: Int) -> Paginated<T>
) : PagingSource<Int, T>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val page = params.key ?: 1
        return try {
            val response = sourceQuery(page)
            LoadResult.Page(
                data = response.items,
                prevKey = response.info.prev,
                nextKey = response.info.next
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }


    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        return state.anchorPosition?.let { anchorPos ->
            state.closestPageToPosition(anchorPos)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPos)?.nextKey?.minus(1)
        }
    }
}