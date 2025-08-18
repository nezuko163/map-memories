package com.nezuko.map.components

import android.Manifest
import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.nezuko.domain.model.Location
import com.nezuko.map.R
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.VisibleRegion
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.coroutines.resume

private const val TAG = "YandexMap"

class YandexMapController @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private var _mapView: MapView? = mapView
    val mapView get() = _mapView

    private val _cameraPosition = MutableStateFlow<CameraPosition?>(null)
    val cameraPosition: StateFlow<CameraPosition?> = _cameraPosition

    private val _visibleRegion = MutableStateFlow<VisibleRegion?>(null)
    val visibleRegion: StateFlow<VisibleRegion?> = _visibleRegion

    private val cameraListener = CameraListener { map, cameraPosition, reason, isFinished ->
        if (isFinished) {
            _cameraPosition.value = cameraPosition
            try {
                _visibleRegion.value = map.visibleRegion
            } catch (e: Throwable) {
                Log.e(TAG, "camera ex: ", e)
            }
        }
    }

    /**
     * Установить "стартовую" точку — контроллер запомнит её и:
     * - если mapView уже прикреплён — добавит плейсмарк и (опционально) переместит камеру;
     * - если mapView ещё не прикреплён — добавит/переместит при attach().
     */
    fun setInitialPoint(
        loc: Location,
        shouldSetPlacemark: Boolean = true,
        animate: Boolean = true,
        iconResId: Int = R.drawable.my_location
    ) {
        Log.i(TAG, "setInitialPoint: map - $_mapView")
        val point = Point(loc.lat, loc.lon)
        if (shouldSetPlacemark) {
            addPlacemark(point, iconResId)
        }
        moveCameraTo(point, animate = animate)
    }

    // --- public API ---
    fun moveCameraTo(loc: Location, zoom: Float = 15f, azimuth: Float = 0f, animate: Boolean = true) {
        val point = Point(loc.lat, loc.lon)
        moveCameraTo(point, zoom, azimuth, animate)
    }

    fun moveCameraTo(point: Point, zoom: Float = 10f, azimuth: Float = 0f, animate: Boolean = true) {
        val mv = _mapView!!
        try {
            val animation = Animation(Animation.Type.SMOOTH, 0.6f)
            mv.mapWindow.map.move(CameraPosition(point, zoom, azimuth, 0f), animation, null)
        } catch (e: Throwable) {
            Log.e(TAG, "moveCameraTo error", e)
        }
    }

    fun moveCameraTo(cameraPosition: CameraPosition, animate: Boolean = true) {
        val mv = _mapView!!
        try {
            val animation = Animation(Animation.Type.SMOOTH, 0.6f)
            mv.mapWindow.map.move(cameraPosition, animation, null)
        } catch (e: Throwable) {
            Log.e(TAG, "moveCameraTo error", e)
        }
    }

    suspend fun getLocation(
        timeoutMs: Long = 5_000L // опционально: таймаут
    ): Location? = withContext(Dispatchers.Main) {
        // Проверка разрешения
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!granted) return@withContext null

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)

        withTimeoutOrNull(timeoutMs) {
            suspendCancellableCoroutine<Location?> { cont ->
                val resumed = AtomicBoolean(false)
                fun safeResume(value: Location?) {
                    if (resumed.compareAndSet(false, true)) {
                        try {
                            cont.resume(value)
                        } catch (_: Throwable) {
                        }
                    }
                }

                fun requestSingleUpdate() {
                    val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
                        .setMaxUpdates(1)
                        .build()

                    val callback = object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult) {
                            fusedClient.removeLocationUpdates(this)
                            val last = result.lastLocation
                            safeResume(last?.let { Location(it.latitude, it.longitude) })
                        }

                        override fun onLocationAvailability(availability: LocationAvailability) {}
                    }

                    fusedClient.requestLocationUpdates(req, callback, Looper.getMainLooper())

                    cont.invokeOnCancellation {
                        try {
                            fusedClient.removeLocationUpdates(callback)
                        } catch (_: Throwable) {
                        }
                    }
                }

                val task = fusedClient.lastLocation
                task.addOnSuccessListener { loc ->
                    if (loc != null) {
                        safeResume(Location(loc.latitude, loc.longitude))
                    } else {
                        requestSingleUpdate()
                    }
                }.addOnFailureListener {
                    requestSingleUpdate()
                }

                cont.invokeOnCancellation {
                    Log.e(TAG, "getLocation: excepton", it)
                }
            }
        }
    }

    /**
     * Присоединить MapView к контроллеру.
     */
    fun attach(newMapView: MapView) {
        if (_mapView === newMapView) return

        detach()

        _mapView = newMapView
        newMapView.mapWindow.map.addCameraListener(cameraListener)

        _cameraPosition.value = newMapView.mapWindow.map.cameraPosition
        try {
            _visibleRegion.value = newMapView.mapWindow.map.visibleRegion
        } catch (e: Throwable) {
            Log.e(TAG, "attach: ошибка при создании visible region", e)
        }
    }

    fun detach() {
        try {
            _mapView?.mapWindow?.map?.removeCameraListener(cameraListener)
        } catch (e: Throwable) {
            Log.e(TAG, "detach: ", e)
        }
        clearPlacemarks()
        _mapView = null
    }

    fun addPlacemark(
        loc: Location,
        iconResId: Int = R.drawable.map_icon,
        tapListenerAction: (() -> Unit)? = null
    ): PlacemarkMapObject? {
        val point = Point(loc.lat, loc.lon)
        return addPlacemark(point, iconResId, tapListenerAction)
    }

    fun addPlacemark(
        point: Point,
        iconResId: Int = R.drawable.map_icon,
        tapListenerAction: (() -> Unit)? = null
    ): PlacemarkMapObject? {
        if (_mapView == null) return null
        val collection = _mapView!!.mapWindow.map.mapObjects
        val placemark = collection.addPlacemark().apply {
            geometry = point
            setIcon(ImageProvider.fromResource(context, iconResId))
        }
        tapListenerAction?.let { action ->
            val listener = MapObjectTapListener { _, _ ->
                action()
                true
            }
            placemark.addTapListener(listener)
        }
        return placemark
    }

    fun removePlacemark(placemark: PlacemarkMapObject) {
        try {
            placemark.parent.remove(placemark)
        } catch (e: Throwable) {
            Log.e(TAG, "removePlacemark: ", e)
        }
    }

    fun clearPlacemarks() {
        _mapView?.mapWindow?.map?.mapObjects?.clear()
    }

    companion object {
        private const val TAG = "YandexMapController"
    }
}
