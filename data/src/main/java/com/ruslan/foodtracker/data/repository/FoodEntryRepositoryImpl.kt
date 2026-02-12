package com.ruslan.foodtracker.data.repository

import com.ruslan.foodtracker.data.local.dao.FoodEntryDao
import com.ruslan.foodtracker.data.mapper.toDomain
import com.ruslan.foodtracker.data.mapper.toEntity
import com.ruslan.foodtracker.domain.model.FoodEntry
import com.ruslan.foodtracker.domain.model.NetworkResult
import com.ruslan.foodtracker.domain.repository.FoodEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.time.LocalDate
import javax.inject.Inject

class FoodEntryRepositoryImpl @Inject constructor(
    private val foodEntryDao: FoodEntryDao
) : FoodEntryRepository {

    override fun getAllEntries(): Flow<NetworkResult<List<FoodEntry>>> =
        foodEntryDao.getAllEntries()
            .map<List<com.ruslan.foodtracker.data.local.entity.FoodEntryEntity>, NetworkResult<List<FoodEntry>>> { entities ->
                NetworkResult.Success(entities.map { it.toDomain() })
            }
            .onStart { emit(NetworkResult.Loading) }
            .catch { e ->
                emit(NetworkResult.Error(
                    message = e.message ?: "Ошибка при загрузке записей",
                    exception = e
                ))
            }

    override fun getEntriesByDate(date: LocalDate): Flow<NetworkResult<List<FoodEntry>>> =
        foodEntryDao.getAllEntries()
            .map<List<com.ruslan.foodtracker.data.local.entity.FoodEntryEntity>, NetworkResult<List<FoodEntry>>> { entities ->
                NetworkResult.Success(
                    entities.filter { entry ->
                        entry.timestamp.toLocalDate() == date
                    }.map { it.toDomain() }
                )
            }
            .onStart { emit(NetworkResult.Loading) }
            .catch { e ->
                emit(NetworkResult.Error(
                    message = e.message ?: "Ошибка при загрузке записей за дату",
                    exception = e
                ))
            }

    override suspend fun getEntryById(id: Long): NetworkResult<FoodEntry> = try {
        val entry = foodEntryDao.getEntryById(id)?.toDomain()
        if (entry != null) {
            NetworkResult.Success(entry)
        } else {
            NetworkResult.Error("Запись не найдена")
        }
    } catch (e: Exception) {
        NetworkResult.Error(
            message = e.message ?: "Ошибка при загрузке записи",
            exception = e
        )
    }

    override suspend fun insertEntry(entry: FoodEntry): NetworkResult<Long> = try {
        val id = foodEntryDao.insertEntry(entry.toEntity())
        NetworkResult.Success(id)
    } catch (e: Exception) {
        NetworkResult.Error(
            message = e.message ?: "Ошибка при добавлении записи",
            exception = e
        )
    }

    override suspend fun updateEntry(entry: FoodEntry): NetworkResult<Unit> = try {
        foodEntryDao.updateEntry(entry.toEntity())
        NetworkResult.Success(Unit)
    } catch (e: Exception) {
        NetworkResult.Error(
            message = e.message ?: "Ошибка при обновлении записи",
            exception = e
        )
    }

    override suspend fun deleteEntry(entry: FoodEntry): NetworkResult<Unit> = try {
        foodEntryDao.deleteEntry(entry.toEntity())
        NetworkResult.Success(Unit)
    } catch (e: Exception) {
        NetworkResult.Error(
            message = e.message ?: "Ошибка при удалении записи",
            exception = e
        )
    }
}
