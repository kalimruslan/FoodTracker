package com.ruslan.foodtracker.feature.home.presenter

import androidx.lifecycle.ViewModel
import com.ruslan.foodtracker.core.ui.components.FoodItemData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel для главного экрана (Дневник питания)
 *
 * TODO: Интеграция с Use Cases после их создания:
 * - GetEntriesByDateUseCase
 * - GetDailyTotalsUseCase
 * - GetTargetCaloriesUseCase
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    // TODO: Inject use cases
    // private val getEntriesByDateUseCase: GetEntriesByDateUseCase,
    // private val getDailyTotalsUseCase: GetDailyTotalsUseCase,
    // private val getTargetCaloriesUseCase: GetTargetCaloriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Пока используем моковые данные
        loadMockData()
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        // TODO: Load data for selected date
    }

    fun onDaySelected(dayIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedDayIndex = dayIndex)
        // TODO: Calculate date from dayIndex and load data
    }

    fun onAddWaterGlass() {
        val currentWater = _uiState.value.waterGlasses
        _uiState.value = _uiState.value.copy(waterGlasses = currentWater + 1)
        // TODO: Save to repository
    }

    private fun loadMockData() {
        _uiState.value = HomeUiState(
            selectedDate = LocalDate.now(),
            selectedDayIndex = 3, // Четверг
            consumedCalories = 684f,
            targetCalories = 2200f,
            protein = MacroData(consumed = 56f, target = 140f),
            fat = MacroData(consumed = 22f, target = 73f),
            carbs = MacroData(consumed = 88f, target = 275f),
            fiber = MacroData(consumed = 8f, target = 30f),
            meals = listOf(
                MealData(
                    id = 1,
                    emoji = "🌅",
                    name = "Завтрак",
                    time = "08:00",
                    totalCalories = 303,
                    foodItems = listOf(
                        FoodItemData("Овсяная каша", "200г", 150),
                        FoodItemData("Банан", "1 шт", 89),
                        FoodItemData("Мёд", "1 ст.л.", 64)
                    )
                ),
                MealData(
                    id = 2,
                    emoji = "☀️",
                    name = "Обед",
                    time = "13:00",
                    totalCalories = 381,
                    foodItems = listOf(
                        FoodItemData("Куриная грудка", "150г", 165),
                        FoodItemData("Рис бурый", "180г", 216)
                    )
                ),
                MealData(
                    id = 3,
                    emoji = "🌙",
                    name = "Ужин",
                    time = "19:00",
                    totalCalories = 0,
                    foodItems = emptyList()
                ),
                MealData(
                    id = 4,
                    emoji = "🍎",
                    name = "Перекус",
                    time = null,
                    totalCalories = 0,
                    foodItems = emptyList()
                )
            ),
            waterGlasses = 4,
            waterTarget = 8,
            isLoading = false,
            error = null
        )
    }
}

/**
 * UI состояние для главного экрана
 */
data class HomeUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedDayIndex: Int = 0,
    val consumedCalories: Float = 0f,
    val targetCalories: Float = 2200f,
    val protein: MacroData = MacroData(0f, 140f),
    val fat: MacroData = MacroData(0f, 73f),
    val carbs: MacroData = MacroData(0f, 275f),
    val fiber: MacroData = MacroData(0f, 30f),
    val meals: List<MealData> = emptyList(),
    val waterGlasses: Int = 0,
    val waterTarget: Int = 8,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Данные макронутриента
 */
data class MacroData(
    val consumed: Float,
    val target: Float
)

/**
 * Данные приёма пищи
 */
data class MealData(
    val id: Long,
    val emoji: String,
    val name: String,
    val time: String?,
    val totalCalories: Int,
    val foodItems: List<FoodItemData>
)
