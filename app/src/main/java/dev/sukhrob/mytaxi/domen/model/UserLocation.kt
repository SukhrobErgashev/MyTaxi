package dev.sukhrob.mytaxi.domen.model

data class UserLocation(
    val storedTime: Long,
    val longitude: Double,
    val latitude: Double
) {
    companion object {
        fun default() = UserLocation(-1, -1.0, -1.0)
    }
}