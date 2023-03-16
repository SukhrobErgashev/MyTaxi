package dev.sukhrob.mytaxi.utils

import com.mapbox.mapboxsdk.geometry.LatLng
import dev.sukhrob.mytaxi.domen.model.UserLocation

fun UserLocation.toLatLng() = LatLng(this.latitude, this.longitude)