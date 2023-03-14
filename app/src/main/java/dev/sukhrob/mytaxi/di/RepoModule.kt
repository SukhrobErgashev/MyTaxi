package dev.sukhrob.mytaxi.di

import dev.sukhrob.mytaxi.data.room.MyTaxiDao
import dev.sukhrob.mytaxi.data.repository.MyTaxiRepositoryImpl
import dev.sukhrob.mytaxi.domen.repository.MyTaxiRepository
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