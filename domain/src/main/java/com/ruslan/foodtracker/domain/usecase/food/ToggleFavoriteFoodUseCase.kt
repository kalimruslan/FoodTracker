package com.ruslan.foodtracker.domain.usecase.food

import com.ruslan.foodtracker.domain.model.NetworkResult
import com.ruslan.foodtracker.domain.repository.FoodRepository
import javax.inject.Inject

class ToggleFavoriteFoodUseCase
    @Inject
    constructor(
        private val repository: FoodRepository
    ) {
        suspend operator fun invoke(
            foodId: Long,
            isFavorite: Boolean
        ): NetworkResult<Unit> = repository.toggleFavorite(foodId, isFavorite)
    }
