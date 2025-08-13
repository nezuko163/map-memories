package com.nezuko.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.nezuko.ui.components.LayoutType
import com.nezuko.ui.components.MemoryCard
import com.nezuko.ui.components.PagingList
import com.nezuko.ui.components.SearchTextField
import com.nezuko.ui.theme.Spacing

private const val TAG = "MainRoute"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainRoute(
    navigateToSearch: (text: String) -> Unit,
    navigateToDetails: (memory: Int) -> Unit,
    vm: MainViewModel = hiltViewModel()
) {
    val items = vm.memories.collectAsLazyPagingItems()
    var query = remember { vm.getQuery() }

    LaunchedEffect(Unit) {
        query = vm.getQuery()
    }

    val gridState = rememberLazyStaggeredGridState()
    val staggeredLayout = remember(gridState) { LayoutType.StaggeredGrid(gridState) }

    Column(modifier = Modifier.padding(horizontal = Spacing.small)) {
        SearchTextField(
            value = query,
            enabled = false,
            onClick = { navigateToSearch(query) }
        )

        PagingList(
            items = items,
            countColumns = 2,
            layoutType = staggeredLayout
        ) { memory ->
            MemoryCard(memory = memory, onCardClick = { navigateToDetails(memory.id) })
        }
    }
}