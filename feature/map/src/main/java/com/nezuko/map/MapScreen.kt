package com.nezuko.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.nezuko.domain.model.Memory
import com.nezuko.map.components.MemoryBottomSheetContent
import com.yandex.mapkit.mapview.MapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    mapView: MapView,
    selectedMemory: Memory?,
    onSheetClosed: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    if (selectedMemory != null) {
        ModalBottomSheet(onDismissRequest = onSheetClosed, sheetState = sheetState) {
            MemoryBottomSheetContent(selectedMemory)
        }
    }

    Box {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
    }
}