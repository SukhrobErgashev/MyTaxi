package com.example.mytaxi.domen.repository

import com.example.mytaxi.domen.model.UserLocation
import kotlinx.coroutines.flow.Flow

interface MyTaxiRepository {

    fun getStoredLocationList(): Flow<List<UserLocation>>
    suspend fun insertUserLocation(item: UserLocation)
}