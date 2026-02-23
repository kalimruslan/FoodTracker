package com.ruslan.foodtracker.data.di

import com.ruslan.foodtracker.data.local.dao.FoodDao
import com.ruslan.foodtracker.data.remote.api.OpenFoodFactsApi
import com.ruslan.foodtracker.data.source.local.FoodLocalDataSource
import com.ruslan.foodtracker.data.source.remote.FoodRemoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt модуль для предоставления DataSource зависимостей
 */
@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {
    @Provides
    @Singleton
    fun provideFoodLocalDataSource(foodDao: FoodDao): FoodLocalDataSource = FoodLocalDataSource(foodDao)

    @Provides
    @Singleton
    fun provideFoodRemoteDataSource(api: OpenFoodFactsApi): FoodRemoteDataSource = FoodRemoteDataSource(api)
}
