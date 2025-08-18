package com.nezuko.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.nezuko.ui.components.CollapsingTopBarWithExpandedContent
import com.nezuko.ui.components.LayoutType
import com.nezuko.ui.components.MemoryCard
import com.nezuko.ui.components.PagingList
import com.nezuko.ui.components.SearchTextField
import com.nezuko.ui.components.UnderlinedTextWithGap
import com.nezuko.ui.components.rememberCollapsingTopBarState
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
    val topBarState = rememberCollapsingTopBarState()

    Column(modifier = Modifier.padding(horizontal = Spacing.medium)) {
        CollapsingTopBarWithExpandedContent(
            state = topBarState,
            expandedContent = {
                Column {
                    SearchTextField(
                        modifier = Modifier
                            .heightIn(50.dp, 50.dp)
                            .align(Alignment.CenterHorizontally),
                        value = query,
                        enabled = false,
                        onClick = { navigateToSearch(query) }
                    )

                    Text("Жопа")
                    Text("Жопа")
                    Text("Жопа")
                }
            },
            collapsedContent = {
                Column {
                    UnderlinedTextWithGap(
                        "Для тебя",
                        modifier = Modifier.padding(vertical = Spacing.small, horizontal = Spacing.medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        )

        PagingList(
            items = items,
            layoutType = LayoutType.Column,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(topBarState.connection)
        ) { memory ->
            MemoryCard(memory = memory, onCardClick = { navigateToDetails(memory.id) })
        }
    }
}