package com.ruslan.foodtracker.domain.usecase.entry

import com.ruslan.foodtracker.domain.model.FoodEntry
import com.ruslan.foodtracker.domain.model.NetworkResult
import com.ruslan.foodtracker.domain.repository.FoodEntryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecentFoodEntriesUseCase
    @Inject
    constructor(
        private val repository: FoodEntryRepository
    ) {
        /**
         * @param limit Максимальное количество продуктов (default: 20)
         */
        operator fun invoke(limit: Int = 20): Flow<NetworkResult<List<FoodEntry>>> =
            repository.getRecentEntries(limit)
    }
