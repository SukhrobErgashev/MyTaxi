package dev.sukhrob.mytaxi.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.sukhrob.mytaxi.data.room.entity.UserLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MyTaxiDao {

    @Insert
    suspend fun insert(userLocation: UserLocationEntity)

    @Query("SELECT * FROM UserLocationEntity ORDER BY storedTime DESC")
    fun getStoredLocationList(): Flow<List<UserLocationEntity>>
    
}