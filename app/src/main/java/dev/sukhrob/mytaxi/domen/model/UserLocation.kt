package dev.sukhrob.mytaxi.domen.model

import java.util.*

data class UserLocation(
    val storedTime: Long,
    val longitude: Double,
    val latitude: Double,
    val bearing: Double
) {
    companion object {
        fun default() = UserLocation(
            Calendar.getInstance().timeInMillis,
            66.96490896909137,
            39.650444803190815,
            0.0
        )
    }
}