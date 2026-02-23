package com.ruslan.foodtracker.data.mapper

import com.ruslan.foodtracker.data.local.entity.FoodEntity
import com.ruslan.foodtracker.data.local.entity.FoodEntryEntity
import com.ruslan.foodtracker.domain.model.Food
import com.ruslan.foodtracker.domain.model.FoodEntry
import com.ruslan.foodtracker.domain.model.MealType

// Food mappings
fun FoodEntity.toDomain(): Food =
    Food(
        id = id,
        name = name,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        fiber = fiber,
        servingSize = servingSize,
        servingUnit = servingUnit,
        barcode = barcode,
        brand = brand,
        imageUrl = imageUrl,
        isFavorite = isFavorite
    )

fun Food.toEntity(): FoodEntity =
    FoodEntity(
        id = id,
        name = name,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        fiber = fiber,
        servingSize = servingSize,
        servingUnit = servingUnit,
        barcode = barcode,
        brand = brand,
        imageUrl = imageUrl,
        isFavorite = isFavorite
    )

// FoodEntry mappings
fun FoodEntryEntity.toDomain(): FoodEntry =
    FoodEntry(
        id = id,
        foodId = foodId,
        foodName = foodName,
        servings = servings,
        amountGrams = amountGrams,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        timestamp = timestamp,
        mealType = MealType.valueOf(mealType)
    )

fun FoodEntry.toEntity(): FoodEntryEntity =
    FoodEntryEntity(
        id = id,
        foodId = foodId,
        foodName = foodName,
        servings = servings,
        amountGrams = amountGrams,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        timestamp = timestamp,
        mealType = mealType.name
    )
