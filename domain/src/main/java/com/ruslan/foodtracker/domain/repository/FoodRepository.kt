package com.ruslan.foodtracker.domain.repository

import com.ruslan.foodtracker.domain.model.Food
import com.ruslan.foodtracker.domain.model.NetworkResult
import com.ruslan.foodtracker.domain.model.PaginatedResult
import kotlinx.coroutines.flow.Flow

interface FoodRepository {
    // Local database operations
    fun getAllFoods(): Flow<NetworkResult<List<Food>>>

    suspend fun getFoodById(id: Long): NetworkResult<Food>

    suspend fun getFoodByBarcodeLocal(barcode: String): NetworkResult<Food>

    suspend fun insertFood(food: Food): NetworkResult<Long>

    suspend fun insertFoods(foods: List<Food>): NetworkResult<List<Long>>

    suspend fun updateFood(food: Food): NetworkResult<Unit>

    suspend fun deleteFood(food: Food): NetworkResult<Unit>

    fun searchFoods(query: String): Flow<NetworkResult<List<Food>>>

    // Favorites operations
    suspend fun toggleFavorite(
        foodId: Long,
        isFavorite: Boolean
    ): NetworkResult<Unit>

    fun getFavoriteFoods(): Flow<NetworkResult<List<Food>>>

    // Remote API operations (возвращают Flow для удобной обработки состояний)
    fun searchFoodsByNameRemote(
        query: String,
        page: Int = 1
    ): Flow<NetworkResult<PaginatedResult<Food>>>

    fun getFoodByBarcode(barcode: String): Flow<NetworkResult<Food>>
}
