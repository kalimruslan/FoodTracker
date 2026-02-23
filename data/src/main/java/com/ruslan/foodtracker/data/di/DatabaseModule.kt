package com.ruslan.foodtracker.data.di

import android.content.Context
import androidx.room.Room
import com.ruslan.foodtracker.core.common.util.Constants
import com.ruslan.foodtracker.data.local.FoodTrackerDatabase
import com.ruslan.foodtracker.data.local.dao.FoodDao
import com.ruslan.foodtracker.data.local.dao.FoodEntryDao
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
    fun provideFoodTrackerDatabase(
        @ApplicationContext context: Context
    ): FoodTrackerDatabase =
        Room
            .databaseBuilder(
                context,
                FoodTrackerDatabase::class.java,
                Constants.DATABASE_NAME
            ).fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideFoodDao(database: FoodTrackerDatabase): FoodDao = database.foodDao()

    @Provides
    fun provideFoodEntryDao(database: FoodTrackerDatabase): FoodEntryDao = database.foodEntryDao()
}
