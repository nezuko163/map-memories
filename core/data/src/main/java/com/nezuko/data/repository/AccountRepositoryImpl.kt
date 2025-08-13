package com.nezuko.data.repository

import com.nezuko.domain.repository.AccountRepository
import java.util.UUID
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor() : AccountRepository {
    private val uid = UUID.randomUUID()

    override fun getUUID(): UUID {
        return uid
    }
}