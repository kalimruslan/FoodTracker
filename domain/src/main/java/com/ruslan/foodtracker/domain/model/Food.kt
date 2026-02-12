package com.ruslan.foodtracker.domain.model

data class Food(
    val id: Long = 0,
    val name: String,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val servingSize: Double,
    val servingUnit: String
)
