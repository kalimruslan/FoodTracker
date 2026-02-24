package com.ruslan.foodtracker.domain.usecase.food

import com.ruslan.foodtracker.domain.model.Food
import com.ruslan.foodtracker.domain.model.NetworkResult
import com.ruslan.foodtracker.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoriteFoodsUseCase
    @Inject
    constructor(
        private val repository: FoodRepository
    ) {
        operator fun invoke(): Flow<NetworkResult<List<Food>>> = repository.getFavoriteFoods()
    }
