package com.nezuko.map.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.nezuko.domain.model.Memory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.MemoryBottomSheetContent(
    memory: Memory
) {
    Text(text = memory.name)
    Text(text = memory.id.toString())
}