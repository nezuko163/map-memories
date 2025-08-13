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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.nezuko.ui.components.CollapsingTopBarByFraction
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

    val expandedHeight = remember { 160.dp }
    val collapsedHeight = remember { 56.dp }

    val topBarState = rememberCollapsingTopBarState(
        expandedHeight = expandedHeight,
        collapsedHeight = collapsedHeight
    )


    Column(modifier = Modifier.padding(horizontal = Spacing.medium)) {
        CollapsingTopBarByFraction(
            collapseFraction = topBarState.collapseFraction,
            expandedHeight = expandedHeight,
            collapsedHeight = collapsedHeight,
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