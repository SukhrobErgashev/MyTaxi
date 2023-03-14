package dev.sukhrob.mytaxi.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.common.location.Location
import com.mapbox.geojson.Point
import dev.sukhrob.mytaxi.domen.model.UserLocation
import dev.sukhrob.mytaxi.domen.repository.MyTaxiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: MyTaxiRepository) : ViewModel() {

    private var _zoomLevel = MutableStateFlow(17.0)
    val zoomLevel: StateFlow<Double> get() = _zoomLevel

    private var _cameraBearing = MutableStateFlow(0.0)
    val cameraBearing: StateFlow<Double> get() = _cameraBearing

    private var _iconRotation = MutableStateFlow(0.0)
    val iconRotation: StateFlow<Double> get() = _iconRotation

    private var _iconSize = MutableStateFlow(0.7)
    val iconSize: StateFlow<Double> get() = _iconSize

    private var _storedLocationList = MutableStateFlow<List<UserLocation>>(emptyList())
    //val storedLocationList: StateFlow<List<UserLocation>> get() = _storedLocationList

    private var _latestLocation = MutableStateFlow(UserLocation.default())
    val latestLocation: StateFlow<UserLocation> get() = _latestLocation

    private var _beforeLatest = MutableStateFlow(UserLocation.default())
    val beforeLatest: StateFlow<UserLocation> get() = _beforeLatest

    init {
        getStoredLocationList()
    }

    private fun getStoredLocationList() = viewModelScope.launch {
        repository.getStoredLocationList().collect {
            if (it.isNotEmpty()) {
                _storedLocationList.value = it
                _latestLocation.value = it.first()
            }
            if (it.size >= 2) {
                _beforeLatest.value = it[1]

                computeBearing()
            }
        }
    }

    private fun computeBearing() {
        // Define the two locations

        val iconRotation = bearing(
            beforeLatest.value.latitude,
            beforeLatest.value.longitude,
            latestLocation.value.latitude,
            latestLocation.value.longitude
        )

        _iconRotation.value = iconRotation
    }

    private fun bearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLon = Math.toRadians(lon2 - lon1)
        val y = sin(dLon) * cos(Math.toRadians(lat2))
        val x = cos(Math.toRadians(lat1)) * sin(Math.toRadians(lat2)) -
                sin(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * cos(dLon)
        var bearing = Math.toDegrees(atan2(y, x))
        bearing = (bearing + 360) % 360
        return bearing
    }

    fun increaseZoomLevel() {
        _zoomLevel.value = _zoomLevel.value + 1.0
    }

    fun decreaseZoomLevel() {
        _zoomLevel.value = _zoomLevel.value - 1.0
    }

    fun setZoomLevel(level: Double) {
        _zoomLevel.value = level
    }

    fun setCameraBearing(bearing: Double) {
        _cameraBearing.value = bearing
    }

}