package com.ruslan.foodtracker.data.di

import android.content.Context
import androidx.room.Room
import com.ruslan.foodtracker.core.common.util.Constants
import com.ruslan.foodtracker.data.local.FoodTrackerDatabase
import com.ruslan.foodtracker.data.local.MIGRATION_1_2
import com.ruslan.foodtracker.data.local.MIGRATION_2_3
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
    ): FoodTrackerDatabase {
        return Room.databaseBuilder(
            context,
            FoodTrackerDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
    }

    @Provides
    fun provideFoodDao(database: FoodTrackerDatabase): FoodDao {
        return database.foodDao()
    }

    @Provides
    fun provideFoodEntryDao(database: FoodTrackerDatabase): FoodEntryDao {
        return database.foodEntryDao()
    }
}
