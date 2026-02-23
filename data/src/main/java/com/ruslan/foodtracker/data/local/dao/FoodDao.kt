package com.ruslan.foodtracker.data.local.dao

import androidx.room.*
import com.ruslan.foodtracker.data.local.entity.FoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM foods ORDER BY name ASC")
    fun getAllFoods(): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE id = :id")
    suspend fun getFoodById(id: Long): FoodEntity?

    @Query("SELECT * FROM foods WHERE barcode = :barcode")
    suspend fun getFoodByBarcode(barcode: String): FoodEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoods(foods: List<FoodEntity>): List<Long>

    @Update
    suspend fun updateFood(food: FoodEntity)

    @Delete
    suspend fun deleteFood(food: FoodEntity)

    @Query(
        """
        SELECT * FROM foods
        WHERE name LIKE '%' || :query || '%'
           OR brand LIKE '%' || :query || '%'
           OR barcode LIKE '%' || :query || '%'
        ORDER BY name ASC
    """
    )
    fun searchFoods(query: String): Flow<List<FoodEntity>>

    @Query("UPDATE foods SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteById(
        id: Long,
        isFavorite: Boolean
    ): Int

    @Query("SELECT * FROM foods WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteFoods(): Flow<List<FoodEntity>>
}
