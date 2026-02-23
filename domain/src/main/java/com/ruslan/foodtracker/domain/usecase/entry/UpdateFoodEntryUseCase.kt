package com.ruslan.foodtracker.domain.usecase.entry

import com.ruslan.foodtracker.domain.model.FoodEntry
import com.ruslan.foodtracker.domain.model.NetworkResult
import com.ruslan.foodtracker.domain.repository.FoodEntryRepository
import javax.inject.Inject

class UpdateFoodEntryUseCase
    @Inject
    constructor(
        private val repository: FoodEntryRepository
    ) {
        suspend operator fun invoke(entry: FoodEntry): NetworkResult<Unit> = repository.updateEntry(entry)
    }
