package com.nezuko.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nezuko.data.di.ArgumentHolder
import com.nezuko.domain.model.Memory
import com.nezuko.domain.repository.AccountRepository
import com.nezuko.domain.repository.MemoriesRepository
import com.nezuko.ui.components.CustomPagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

private const val TAG = "MainViewModel"

@HiltViewModel
class MainViewModel @Inject constructor(
    private val memoriesRepository: MemoriesRepository,
    private val accountRepository: AccountRepository,
    private val argumentHolder: ArgumentHolder
) : ViewModel() {
    val memories: Flow<PagingData<Memory>> = Pager(
        config = PagingConfig(pageSize = 20),
        pagingSourceFactory = {
            CustomPagingSource { page ->
                Log.i(TAG, "source: page - $page")
                memoriesRepository.getMemoriesFYP(accountRepository.getUUID(), page)
            }
        }
    )
        .flow
        .cachedIn(viewModelScope)
    fun getQuery() = argumentHolder.query
}