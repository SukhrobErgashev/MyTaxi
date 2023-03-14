package dev.sukhrob.mytaxi.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.sukhrob.mytaxi.data.room.entity.UserLocationEntity

@Database(
    entities = [UserLocationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MyTaxiDatabase : RoomDatabase() {

    abstract fun myTaxiDao(): MyTaxiDao

}