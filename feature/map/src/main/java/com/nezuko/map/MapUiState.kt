package com.nezuko.map

import com.nezuko.domain.model.Location
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.VisibleRegion

data class YandexMapData(
    val userLocation: Location? = null,
    val placemarks: List<Int> = emptyList(),
    val cameraPosition: CameraPosition? = null,
    val visibleRegion: VisibleRegion? = null
)