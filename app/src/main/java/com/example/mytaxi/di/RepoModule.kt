package com.example.mytaxi.di

import com.example.mytaxi.data.room.MyTaxiDao
import com.example.mytaxi.data.repository.MyTaxiRepositoryImpl
import com.example.mytaxi.domen.repository.MyTaxiRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepoModule {

    @Provides
    @Singleton
    fun provideRepository(dao: MyTaxiDao): MyTaxiRepository {
        return MyTaxiRepositoryImpl(dao)
    }
}