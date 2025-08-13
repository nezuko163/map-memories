package com.nezuko.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.nezuko.common.EntityWithId
import com.nezuko.domain.exception.EmptyResultException
import com.nezuko.ui.theme.Spacing


sealed class LayoutType {
    object Column: LayoutType()
    object Grid: LayoutType()
    data class StaggeredGrid(val state: LazyStaggeredGridState) : LayoutType()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun <T : EntityWithId> PagingList(
    modifier: Modifier = Modifier,
    items: LazyPagingItems<T>,
    state: PullToRefreshState = rememberPullToRefreshState(),
    countColumns: Int = 1,
    layoutType: LayoutType = LayoutType.Column,
    isRefreshing: Boolean = items.loadState.refresh is LoadState.Loading,
    onRefresh: () -> Unit = items::refresh,
    paddingValues: PaddingValues = PaddingValues(Spacing.medium),
    indicator: @Composable BoxScope.(state: PullToRefreshState, isRefreshing: Boolean) -> Unit = { state, isRefreshing ->
        Indicator(
            modifier = Modifier.align(Alignment.TopCenter),
            state = state,
            isRefreshing = isRefreshing,
            color = MaterialTheme.colorScheme.primary,
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    },
    emptyState: @Composable () -> Unit = { DefaultEmptyState() },
    contentCard: @Composable (T) -> Unit
) {

    val stateRefresh = items.loadState.refresh

    val maxOffset = 80.dp
    val offsetPx = with(LocalDensity.current) {
        (maxOffset * state.distanceFraction).roundToPx()
    }


    PullToRefreshBox(
        isRefreshing = isRefreshing,
        modifier = modifier,
        state = state,
        onRefresh = onRefresh,
        indicator = {
            indicator(state, isRefreshing)
        }
    ) {
        if (stateRefresh is LoadState.Error && stateRefresh.error is EmptyResultException) {
            emptyState()
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(x = 0, y = offsetPx) }
                .fillMaxSize()
        ) {

            when (layoutType) {
                LayoutType.Column -> ColumnList(
                    items,
                    contentPadding = paddingValues,
                    loadState = items.loadState.append,
                    contentCard = contentCard
                )

                LayoutType.Grid -> GridList(
                    items = items,
                    columns = countColumns,
                    contentPadding = paddingValues,
                    loadState = items.loadState.append,
                    contentCard = contentCard
                )

                is LayoutType.StaggeredGrid -> StaggeredList(
                    items = items,
                    columns = countColumns,
                    contentPadding = paddingValues,
                    loadState = items.loadState.append,
                    contentCard = contentCard,
                    state = layoutType.state
                )
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun <T : EntityWithId> ColumnList(
    items: LazyPagingItems<T>,
    contentPadding: PaddingValues,
    loadState: LoadState,
    contentCard: @Composable (item: T) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = contentPadding) {
        items(count = items.itemCount, key = items.itemKey { it.id }) { index ->
            items[index]?.let { item -> contentCard(item) }
        }

        item {
            FooterByLoadState(loadState = loadState) { items.retry() }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun <T : EntityWithId> GridList(
    items: LazyPagingItems<T>,
    columns: Int,
    contentPadding: PaddingValues,
    loadState: LoadState,
    contentCard: @Composable (item: T) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding
    ) {
        items(count = items.itemCount, key = items.itemKey { it.id }) { index ->
            items[index]?.let { item -> contentCard(item) }
        }

        item {
            FooterByLoadState(loadState = loadState) { items.retry() }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun <T : EntityWithId> StaggeredList(
    items: LazyPagingItems<T>,
    state: LazyStaggeredGridState,
    columns: Int,
    contentPadding: PaddingValues,
    loadState: LoadState,
    contentCard: @Composable (item: T) -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(columns),
        modifier = Modifier.fillMaxSize(),
        state = state,
        contentPadding = contentPadding
    ) {
        items(count = items.itemCount, key = items.itemKey { it.id }) { index ->
            items[index]?.let { item -> contentCard(item) }
        }


        item {
            FooterByLoadState(loadState = loadState) { items.retry() }
        }
    }
}

@Composable
private fun FooterByLoadState(loadState: LoadState, retry: () -> Unit) {
    when (loadState) {
        is LoadState.Loading -> DefaultAppendLoadingState()
        is LoadState.Error -> DefaultAppendErrorState { retry() }
        is LoadState.NotLoading -> Unit
    }
}


@Composable
fun DefaultEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.large),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Ничего не найдено",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DefaultAppendLoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.medium),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            strokeWidth = 2.dp,
            modifier = Modifier.size(24.dp)
        )

    }
}

@Composable
fun DefaultAppendErrorState(retry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ошибка загрузки",
            color = Color.Red
        )
        Spacer(modifier = Modifier.height(Spacing.small))
        Button(onClick = retry) {
            Text("Повторить")
        }
    }
}
