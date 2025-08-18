package com.nezuko.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nezuko.domain.model.Location
import com.nezuko.domain.model.Memory
import com.nezuko.domain.repository.MemoriesRepository
import com.nezuko.map.components.YandexMapController
import com.nezuko.map.components.toLocation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.VisibleRegion
import com.yandex.mapkit.mapview.MapView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class MapViewModel @Inject constructor(
    private val yandexMapController: YandexMapController,
    private val memoryRepository: MemoriesRepository
) : ViewModel() {
    private var yandexMapData = YandexMapData()
    private val _selectedMemory = MutableStateFlow<Memory?>(null)
    val selectedMemory = _selectedMemory.asStateFlow()
    var isFirstLoad = true

    init {
        viewModelScope.launch(Dispatchers.Default) {
            yandexMapController.cameraPosition.debounce(200).collectLatest {
                yandexMapData = yandexMapData.copy(cameraPosition = it)
            }
        }
        viewModelScope.launch(Dispatchers.Default) {
            yandexMapController.visibleRegion.debounce(200).collectLatest {
                yandexMapData = yandexMapData.copy(visibleRegion = it)

                it?.let {
                    loadMemoriesByVisibleRegion(it)
                }
            }
        }
    }

    fun init(mapView: MapView) {
        yandexMapController.attach(mapView)

        yandexMapData.cameraPosition.let {
            if (it == null) {
                Log.i(TAG, "init: asd")
                yandexMapController.moveCameraTo(Point(59.936046, 30.326869), animate = false)
            } else {
                Log.i(TAG, "init: zxc")
                yandexMapController.moveCameraTo(it, animate = false)
            }
        }

        if (yandexMapData.placemarks.isEmpty()) return

        viewModelScope.launch {
            memoryRepository.loadMemories(yandexMapData.placemarks)
            for (id in yandexMapData.placemarks) {
                val memory = memoryRepository.getMemoryById(id)
                yandexMapController.addPlacemark(memory.location) {
                    _selectedMemory.value = memory
                }
            }
        }
    }

    fun clearSelectedMemory() {
        _selectedMemory.value = null
    }

    fun detach() {
        yandexMapController.detach()
    }

    fun loadMemoriesByVisibleRegion(reg: VisibleRegion) {
        viewModelScope.launch {
            val memories = memoryRepository.getMemoriesByBounds(
                topLeft = reg.topLeft.toLocation(),
                topRight = reg.topRight.toLocation(),
                bottomLeft = reg.bottomLeft.toLocation(),
                bottomRight = reg.bottomRight.toLocation()
            )

            yandexMapData =
                yandexMapData.copy(placemarks = yandexMapData.placemarks + memories.map { it.id })
            memories.forEach { memory ->
                yandexMapController.addPlacemark(memory.location) {
                    _selectedMemory.value = memory
                }
            }
        }
    }

    fun loadUserLocation(shouldUseFakeLocation: Boolean = false) {
        viewModelScope.launch {
            var flag = true
            val userLocation: Location
            if (shouldUseFakeLocation) {
                userLocation = Location(55.751225, 37.62954)
            } else {
                userLocation = yandexMapController.getLocation().let {
                    if (it == null) {
                        Log.e(TAG, "setUserLocation: ошибка при нахождении локации пользователя")
                        flag = false
                        Location(55.751225, 37.62954)
                    } else {
                        it
                    }
                }
            }
            yandexMapData = yandexMapData.copy(userLocation = userLocation)
            Log.i(TAG, "loadUserLocation: $userLocation")
            yandexMapController.setInitialPoint(
                userLocation
            )
        }
    }

    companion object {
        private const val TAG = "MapViewModel"
    }
}