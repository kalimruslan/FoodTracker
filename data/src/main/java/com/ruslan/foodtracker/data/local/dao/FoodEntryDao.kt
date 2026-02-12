package com.ruslan.foodtracker.data.local.dao

import androidx.room.*
import com.ruslan.foodtracker.data.local.entity.FoodEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodEntryDao {
    @Query("SELECT * FROM food_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<FoodEntryEntity>>

    @Query("SELECT * FROM food_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): FoodEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: FoodEntryEntity): Long

    @Update
    suspend fun updateEntry(entry: FoodEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: FoodEntryEntity)
}
