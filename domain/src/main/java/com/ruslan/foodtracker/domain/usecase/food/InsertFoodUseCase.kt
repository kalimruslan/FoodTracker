package com.ruslan.foodtracker.domain.usecase.food

import com.ruslan.foodtracker.domain.model.Food
import com.ruslan.foodtracker.domain.model.NetworkResult
import com.ruslan.foodtracker.domain.repository.FoodRepository
import javax.inject.Inject

class InsertFoodUseCase
    @Inject
    constructor(
        private val repository: FoodRepository
    ) {
        suspend operator fun invoke(food: Food): NetworkResult<Long> = repository.insertFood(food)
    }
