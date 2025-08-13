package com.nezuko.data.di

import com.nezuko.data.repository.AccountRepositoryImpl
import com.nezuko.data.repository.EmptyRepositoryImpl
import com.nezuko.data.repository.MemoriesRepositoryImpl
import com.nezuko.data.repository.SearchRepositoryImpl
import com.nezuko.domain.repository.AccountRepository
import com.nezuko.domain.repository.EmptyRepository
import com.nezuko.domain.repository.MemoriesRepository
import com.nezuko.domain.repository.SearchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun empty(impl: EmptyRepositoryImpl): EmptyRepository

    @Binds
    @Singleton
    abstract fun memoryRepo(impl: MemoriesRepositoryImpl): MemoriesRepository
    @Binds
    @Singleton
    abstract fun accountRepo(impl: AccountRepositoryImpl): AccountRepository

    @Binds
    @Singleton
    abstract fun searchRepo(impl: SearchRepositoryImpl): SearchRepository
}