package com.ruslan.foodtracker.domain.usecase.entry

import com.ruslan.foodtracker.domain.model.FoodEntry
import com.ruslan.foodtracker.domain.model.NetworkResult
import com.ruslan.foodtracker.domain.repository.FoodEntryRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetEntriesByDateUseCase
    @Inject
    constructor(
        private val repository: FoodEntryRepository
    ) {
        operator fun invoke(date: LocalDate): Flow<NetworkResult<List<FoodEntry>>> = repository.getEntriesByDate(date)
    }
