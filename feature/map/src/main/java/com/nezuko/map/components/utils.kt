package com.nezuko.map.components

import com.nezuko.domain.model.Location
import com.yandex.mapkit.geometry.Point

fun Point.toLocation(): Location {
    return Location(this.latitude, this.longitude)
}