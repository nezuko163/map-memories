package com.nezuko.search

import androidx.lifecycle.ViewModel
import com.nezuko.data.di.ArgumentHolder
import com.nezuko.domain.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val argumentHolder: ArgumentHolder
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val suggestions = _query
        .debounce(300)
        .distinctUntilChanged()
        .mapLatest { text ->
            if (text.isBlank()) {
                null
            } else {
                searchRepository.suggestionForText(text)
            }
        }

    fun changeText(text: String) {
        _query.value = text
    }

    fun setQueryToArgumentsHolder() {
        argumentHolder.query = _query.value
    }
}