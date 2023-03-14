package dev.sukhrob.mytaxi.domen.repository

import dev.sukhrob.mytaxi.domen.model.UserLocation
import kotlinx.coroutines.flow.Flow

interface MyTaxiRepository {

    fun getStoredLocationList(): Flow<List<UserLocation>>
    suspend fun insertUserLocation(item: UserLocation)
}