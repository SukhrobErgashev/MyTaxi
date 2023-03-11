package com.example.mytaxi.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mytaxi.data.room.entity.UserLocationEntity

@Database(
    entities = [UserLocationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MyTaxiDatabase : RoomDatabase() {

    abstract fun myTaxiDao(): MyTaxiDao

}