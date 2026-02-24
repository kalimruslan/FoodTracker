package com.ruslan.foodtracker.data.local.dao

import androidx.room.*
import com.ruslan.foodtracker.data.local.entity.FoodEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodEntryDao {
    @Query("SELECT * FROM food_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<FoodEntryEntity>>

    @Query("SELECT * FROM food_entries WHERE timestamp LIKE :date || '%' ORDER BY timestamp ASC")
    fun getEntriesByDate(date: String): Flow<List<FoodEntryEntity>>

    @Query("SELECT * FROM food_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): FoodEntryEntity?

    /**
     * Последние уникальные записи по foodId, отсортированные по убыванию MAX(timestamp) группы.
     * INNER JOIN гарантирует детерминированный выбор ровно одной записи на foodId — самой свежей.
     */
    @Query(
        """
        SELECT fe.* FROM food_entries fe
        INNER JOIN (
            SELECT foodId, MAX(timestamp) AS maxTs
            FROM food_entries
            GROUP BY foodId
            ORDER BY maxTs DESC
            LIMIT :limit
        ) latest ON fe.foodId = latest.foodId AND fe.timestamp = latest.maxTs
        ORDER BY latest.maxTs DESC
    """
    )
    fun getRecentEntries(limit: Int): Flow<List<FoodEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: FoodEntryEntity): Long

    @Update
    suspend fun updateEntry(entry: FoodEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: FoodEntryEntity)
}
