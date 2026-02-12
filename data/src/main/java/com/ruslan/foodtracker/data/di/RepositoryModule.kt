package com.ruslan.foodtracker.data.di

import com.ruslan.foodtracker.data.repository.FoodEntryRepositoryImpl
import com.ruslan.foodtracker.data.repository.FoodRepositoryImpl
import com.ruslan.foodtracker.domain.repository.FoodEntryRepository
import com.ruslan.foodtracker.domain.repository.FoodRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFoodRepository(
        impl: FoodRepositoryImpl
    ): FoodRepository

    @Binds
    @Singleton
    abstract fun bindFoodEntryRepository(
        impl: FoodEntryRepositoryImpl
    ): FoodEntryRepository
}
