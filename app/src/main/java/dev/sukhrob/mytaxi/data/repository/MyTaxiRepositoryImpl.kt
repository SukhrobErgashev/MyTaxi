package dev.sukhrob.mytaxi.data.repository

import dev.sukhrob.mytaxi.data.room.MyTaxiDao
import dev.sukhrob.mytaxi.data.mapper.Mappers.toEntity
import dev.sukhrob.mytaxi.data.mapper.Mappers.toModel
import dev.sukhrob.mytaxi.domen.model.UserLocation
import dev.sukhrob.mytaxi.domen.repository.MyTaxiRepository
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