package com.nezuko.domain.repository

import java.util.UUID

interface AccountRepository {
    fun getUUID(): UUID
}