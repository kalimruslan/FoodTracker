package com.ruslan.foodtracker.domain.repository

import com.ruslan.foodtracker.domain.model.Food
import com.ruslan.foodtracker.domain.model.NetworkResult
import kotlinx.coroutines.flow.Flow

interface FoodRepository {
    fun getAllFoods(): Flow<NetworkResult<List<Food>>>
    suspend fun getFoodById(id: Long): NetworkResult<Food>
    suspend fun insertFood(food: Food): NetworkResult<Long>
    suspend fun updateFood(food: Food): NetworkResult<Unit>
    suspend fun deleteFood(food: Food): NetworkResult<Unit>
    fun searchFoods(query: String): Flow<NetworkResult<List<Food>>>
}
