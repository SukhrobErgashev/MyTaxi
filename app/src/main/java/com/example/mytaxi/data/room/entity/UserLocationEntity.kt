package com.example.mytaxi.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserLocationEntity(
    @PrimaryKey
    val storedTime: Long,
    val longitude: Double,
    val latitude: Double
)
