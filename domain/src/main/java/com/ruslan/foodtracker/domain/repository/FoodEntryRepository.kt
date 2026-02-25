package com.ruslan.foodtracker.domain.repository

import com.ruslan.foodtracker.domain.model.FoodEntry
import com.ruslan.foodtracker.domain.model.NetworkResult
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface FoodEntryRepository {
    fun getAllEntries(): Flow<NetworkResult<List<FoodEntry>>>

    fun getEntriesByDate(date: LocalDate): Flow<NetworkResult<List<FoodEntry>>>

    /**
     * Последние уникальные продукты (дедупликация по foodId),
     * отсортированные по убыванию timestamp.
     * @param limit количество записей
     */
    fun getRecentEntries(limit: Int): Flow<NetworkResult<List<FoodEntry>>>

    suspend fun getEntryById(id: Long): NetworkResult<FoodEntry>

    suspend fun insertEntry(entry: FoodEntry): NetworkResult<Long>

    suspend fun updateEntry(entry: FoodEntry): NetworkResult<Unit>

    suspend fun deleteEntry(entry: FoodEntry): NetworkResult<Unit>
}
