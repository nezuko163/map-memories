package com.nezuko.domain.model

import com.nezuko.common.AuditEntity
import com.nezuko.common.EntityWithId

data class Memory(
    override val id: Int,
    val photoUrl: String,
    val name: String,
    val location: Location,
    val description: String,
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis()
) : EntityWithId, AuditEntity