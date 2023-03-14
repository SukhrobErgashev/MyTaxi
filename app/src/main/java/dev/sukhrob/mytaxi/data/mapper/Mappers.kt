package dev.sukhrob.mytaxi.data.mapper

import dev.sukhrob.mytaxi.data.room.entity.UserLocationEntity
import dev.sukhrob.mytaxi.domen.model.UserLocation

object Mappers {
    fun UserLocationEntity.toModel(): UserLocation {
        return UserLocation(
            this.storedTime,
            this.longitude,
            this.latitude
        )
    }

    fun UserLocation.toEntity(): UserLocationEntity {
        return UserLocationEntity(
            this.storedTime,
            this.longitude,
            this.latitude
        )
    }
}