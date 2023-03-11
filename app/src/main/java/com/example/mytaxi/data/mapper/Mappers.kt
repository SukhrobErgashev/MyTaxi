package com.example.mytaxi.data.mapper

import com.example.mytaxi.data.room.entity.UserLocationEntity
import com.example.mytaxi.domen.model.UserLocation

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