package com.nezuko.domain.model

import com.nezuko.common.EntityWithId

data class EmptyModel(
    override val id: Int,
    val a: String = "asd"
): EntityWithId
