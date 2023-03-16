package dev.sukhrob.mytaxi.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sukhrob.mytaxi.domen.model.UserLocation
import dev.sukhrob.mytaxi.domen.repository.MyTaxiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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

    private var _iconSize = MutableStateFlow(0.5)
    val iconSize: StateFlow<Double> get() = _iconSize

    private var _storedLocationList = MutableStateFlow<List<UserLocation>>(emptyList())
    //val storedLocationList: StateFlow<List<UserLocation>> get() = _storedLocationList

    private var _latestLocation = MutableStateFlow(UserLocation.default())
    val latestLocation: StateFlow<UserLocation> get() = _latestLocation

    private var _beforeLatest = MutableStateFlow(UserLocation.default())
    //val beforeLatest: StateFlow<UserLocation> get() = _beforeLatest

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
        viewModelScope.launch(Dispatchers.Default) {
            with(_storedLocationList.value) {
                if (this.size > 2) {
                   val iconRotation = bearing(this[1], this[0])
                    _iconRotation.value = iconRotation
                }
            }
        }
    }

    private fun bearing(point1: UserLocation, point2: UserLocation): Double {
        val dLon = Math.toRadians(point2.longitude - point1.longitude)
        val y = sin(dLon) * cos(Math.toRadians(point2.latitude))
        val x = cos(Math.toRadians(point1.latitude)) * sin(Math.toRadians(point2.latitude)) -
                sin(Math.toRadians(point1.latitude)) * cos(Math.toRadians(point2.latitude)) * cos(dLon)
        var bearing = Math.toDegrees(atan2(y, x))
        bearing = (bearing + 360) % 360
        return bearing
    }

    fun increaseZoomLevel() {
        if (zoomLevel.value < 17) {
            _zoomLevel.value = _zoomLevel.value + 1.0
        }
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