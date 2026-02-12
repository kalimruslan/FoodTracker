package com.ruslan.foodtracker.domain.model

import java.time.LocalDateTime

data class FoodEntry(
    val id: Long = 0,
    val foodId: Long,
    val foodName: String,
    val servings: Double,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val timestamp: LocalDateTime,
    val mealType: MealType
)
