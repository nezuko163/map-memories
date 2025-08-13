package com.nezuko.common


data class PageInfo(
    val count: Int,
    val page: Int,
    val next: Int? = null,
    val prev: Int? = null
)

data class Paginated<T>(
    val items: List<T>,
    val info: PageInfo
)