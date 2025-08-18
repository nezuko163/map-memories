package com.nezuko.domain.model

import com.nezuko.common.AuditEntity
import com.nezuko.common.EntityWithId

data class Memory(
    override val id: Int,
    val name: String,
    val author: User,
    val photoUrl: String,
    val photosUrls: List<String>,
    val location: Location,
    val description: String,
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis()
) : EntityWithId, AuditEntity