package com.example.mytaxi.di

import android.content.Context
import androidx.room.Room
import com.example.mytaxi.data.room.MyTaxiDao
import com.example.mytaxi.data.room.MyTaxiDatabase
import com.example.mytaxi.utils.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providesDatabase(@ApplicationContext context: Context): MyTaxiDatabase {
        return Room.databaseBuilder(
            context,
            MyTaxiDatabase::class.java,
            DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideDao(db: MyTaxiDatabase): MyTaxiDao {
        return db.myTaxiDao()
    }

}