package com.ruslan.foodtracker.data.repository

import com.ruslan.foodtracker.data.local.dao.FoodEntryDao
import com.ruslan.foodtracker.data.local.entity.FoodEntryEntity
import com.ruslan.foodtracker.data.mapper.toDomain
import com.ruslan.foodtracker.data.mapper.toEntity
import com.ruslan.foodtracker.domain.error.DomainError
import com.ruslan.foodtracker.domain.model.FoodEntry
import com.ruslan.foodtracker.domain.model.NetworkResult
import com.ruslan.foodtracker.domain.repository.FoodEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.time.LocalDate
import javax.inject.Inject

class FoodEntryRepositoryImpl
    @Inject
    constructor(
        private val foodEntryDao: FoodEntryDao
    ) : FoodEntryRepository {
        override fun getAllEntries(): Flow<NetworkResult<List<FoodEntry>>> =
            foodEntryDao
                .getAllEntries()
                .map<List<FoodEntryEntity>, NetworkResult<List<FoodEntry>>> { entities ->
                    NetworkResult.Success(entities.map { it.toDomain() })
                }.onStart { emit(NetworkResult.Loading) }
                .catch { e ->
                    emit(
                        NetworkResult.Error(
                            error = DomainError.Database.FetchFailed,
                            exception = e
                        )
                    )
                }

        override fun getEntriesByDate(date: LocalDate): Flow<NetworkResult<List<FoodEntry>>> =
            foodEntryDao
                .getEntriesByDate(date.toString())
                .map<List<FoodEntryEntity>, NetworkResult<List<FoodEntry>>> { entities ->
                    NetworkResult.Success(entities.map { it.toDomain() })
                }.onStart { emit(NetworkResult.Loading) }
                .catch { e ->
                    emit(
                        NetworkResult.Error(
                            error = DomainError.Database.FetchFailed,
                            exception = e
                        )
                    )
                }

        override suspend fun getEntryById(id: Long): NetworkResult<FoodEntry> =
            try {
                val entry = foodEntryDao.getEntryById(id)?.toDomain()
                if (entry != null) {
                    NetworkResult.Success(entry)
                } else {
                    NetworkResult.Error(DomainError.Database.NotFound)
                }
            } catch (e: Exception) {
                NetworkResult.Error(
                    error = DomainError.Database.FetchFailed,
                    exception = e
                )
            }

        override suspend fun insertEntry(entry: FoodEntry): NetworkResult<Long> =
            try {
                val id = foodEntryDao.insertEntry(entry.toEntity())
                NetworkResult.Success(id)
            } catch (e: Exception) {
                NetworkResult.Error(
                    error = DomainError.Database.InsertFailed,
                    exception = e
                )
            }

        override suspend fun updateEntry(entry: FoodEntry): NetworkResult<Unit> =
            try {
                foodEntryDao.updateEntry(entry.toEntity())
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                NetworkResult.Error(
                    error = DomainError.Database.UpdateFailed,
                    exception = e
                )
            }

        override suspend fun deleteEntry(entry: FoodEntry): NetworkResult<Unit> =
            try {
                foodEntryDao.deleteEntry(entry.toEntity())
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                NetworkResult.Error(
                    error = DomainError.Database.DeleteFailed,
                    exception = e
                )
            }
    }
