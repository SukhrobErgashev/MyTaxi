package com.example.mytaxi.data.repository

import android.util.Log
import com.example.mytaxi.data.room.MyTaxiDao
import com.example.mytaxi.data.mapper.Mappers.toEntity
import com.example.mytaxi.data.mapper.Mappers.toModel
import com.example.mytaxi.domen.model.UserLocation
import com.example.mytaxi.domen.repository.MyTaxiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MyTaxiRepositoryImpl(private val dao: MyTaxiDao) : MyTaxiRepository {

    override fun getStoredLocationList(): Flow<List<UserLocation>> =
        dao.getStoredLocationList().map { list ->
            list.map { it.toModel() }
        }

    override suspend fun insertUserLocation(item: UserLocation) {
        dao.insert(item.toEntity())
    }
}