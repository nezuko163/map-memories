package com.nezuko.domain.model

import com.nezuko.common.EntityWithId

data class User(
    override val id: Int,
    val name: String,
    val photoUrl: String
) : EntityWithId
