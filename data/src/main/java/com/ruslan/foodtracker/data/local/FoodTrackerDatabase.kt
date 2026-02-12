package com.ruslan.foodtracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ruslan.foodtracker.data.local.converter.Converters
import com.ruslan.foodtracker.data.local.dao.FoodDao
import com.ruslan.foodtracker.data.local.dao.FoodEntryDao
import com.ruslan.foodtracker.data.local.entity.FoodEntity
import com.ruslan.foodtracker.data.local.entity.FoodEntryEntity

@Database(
    entities = [
        FoodEntity::class,
        FoodEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FoodTrackerDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun foodEntryDao(): FoodEntryDao
}
