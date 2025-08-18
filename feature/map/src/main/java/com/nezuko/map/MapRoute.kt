package com.nezuko.map

import android.Manifest
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.nezuko.map.components.yandexMapView
import com.nezuko.ui.utils.hasPermission
import com.nezuko.ui.utils.permissionLauncher

private const val TAG = "MapRoute"

@Composable
fun MapRoute(vm: MapViewModel = hiltViewModel()) {
    val context = LocalContext.current.applicationContext

    val selectedMemory by vm.selectedMemory.collectAsState()
    val mapView = yandexMapView()
    Log.i(TAG, "MapRoute: vm - ${vm.hashCode()}")

    DisposableEffect(Unit) {
        vm.init(mapView)
        onDispose {
            vm.detach()
        }
    }

    var hasPermission by rememberSaveable {
        hasPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ).let {
            val res = if (it) true else null
            mutableStateOf(res)
        }
    }

    val launcher = permissionLauncher(
        onPermissionGranted = { hasPermission = true },
        onFailure = { hasPermission = false }
    )

    LaunchedEffect(Unit) {
        if (hasPermission == null) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission == null) return@LaunchedEffect

        if (vm.isFirstLoad) {
            Log.i(TAG, "MapRoute: permission - $hasPermission")
            if (hasPermission!!) {
                vm.loadUserLocation()
            }
            vm.isFirstLoad = false
        }
    }

    MapScreen(
        mapView = mapView,
        selectedMemory = selectedMemory,
        onSheetClosed = vm::clearSelectedMemory
    )
}