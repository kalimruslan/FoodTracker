package com.ruslan.foodtracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FoodTrackerDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun foodEntryDao(): FoodEntryDao
}

/**
 * Миграция с версии 1 на версию 2
 * Добавляет новые поля в таблицу foods: fiber, barcode, brand, imageUrl
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE foods ADD COLUMN fiber REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE foods ADD COLUMN barcode TEXT")
        db.execSQL("ALTER TABLE foods ADD COLUMN brand TEXT")
        db.execSQL("ALTER TABLE foods ADD COLUMN imageUrl TEXT")
    }
}
