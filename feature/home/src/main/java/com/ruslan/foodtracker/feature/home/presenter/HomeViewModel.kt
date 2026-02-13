package com.ruslan.foodtracker.feature.home.presenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruslan.foodtracker.core.ui.components.FoodItemData
import com.ruslan.foodtracker.domain.model.FoodEntry
import com.ruslan.foodtracker.domain.model.MealType
import com.ruslan.foodtracker.domain.model.doActionIfError
import com.ruslan.foodtracker.domain.model.doActionIfLoading
import com.ruslan.foodtracker.domain.model.doActionIfSuccess
import com.ruslan.foodtracker.domain.usecase.entry.DeleteFoodEntryUseCase
import com.ruslan.foodtracker.domain.usecase.entry.GetEntriesByDateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * ViewModel для главного экрана (Дневник питания)
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getEntriesByDateUseCase: GetEntriesByDateUseCase,
    private val deleteFoodEntryUseCase: DeleteFoodEntryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadEntriesForSelectedDate()
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        loadEntriesForSelectedDate()
    }

    fun onDaySelected(dayIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedDayIndex = dayIndex)
        // TODO: Calculate date from dayIndex and load data
        // Пока просто обновляем индекс
    }

    fun onAddWaterGlass() {
        val currentWater = _uiState.value.waterGlasses
        _uiState.value = _uiState.value.copy(waterGlasses = currentWater + 1)
        // TODO: Save to repository
    }

    fun onDeleteEntry(entry: FoodEntry) {
        viewModelScope.launch {
            deleteFoodEntryUseCase(entry).doActionIfSuccess {
                // Перезагружаем данные
                loadEntriesForSelectedDate()
            }
        }
    }

    private fun loadEntriesForSelectedDate() {
        viewModelScope.launch {
            val selectedDate = _uiState.value.selectedDate
            getEntriesByDateUseCase(selectedDate).collect { result ->
                result.doActionIfLoading {
                    _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                }

                result.doActionIfSuccess { entries ->
                    // Группируем записи по типу приема пищи
                    val groupedEntries = entries.groupBy { it.mealType }

                    // Вычисляем итоговые калории и макросы
                    val totalCalories = entries.sumOf { it.calories }.toFloat()
                    val totalProtein = entries.sumOf { it.protein }.toFloat()
                    val totalFat = entries.sumOf { it.fat }.toFloat()
                    val totalCarbs = entries.sumOf { it.carbs }.toFloat()

                    // Целевые значения (пока дефолтные)
                    val targetCalories = 2200f
                    val targetProtein = 140f
                    val targetFat = 73f
                    val targetCarbs = 275f
                    val targetFiber = 30f

                    // Создаем данные приемов пищи
                    val meals = listOf(
                        createMealData(MealType.BREAKFAST, "🌅", "Завтрак", groupedEntries),
                        createMealData(MealType.LUNCH, "☀️", "Обед", groupedEntries),
                        createMealData(MealType.DINNER, "🌙", "Ужин", groupedEntries),
                        createMealData(MealType.SNACK, "🍎", "Перекус", groupedEntries)
                    )

                    _uiState.value = _uiState.value.copy(
                        consumedCalories = totalCalories,
                        targetCalories = targetCalories,
                        protein = MacroData(consumed = totalProtein, target = targetProtein),
                        fat = MacroData(consumed = totalFat, target = targetFat),
                        carbs = MacroData(consumed = totalCarbs, target = targetCarbs),
                        fiber = MacroData(consumed = 0f, target = targetFiber), // TODO: добавить fiber в FoodEntry
                        meals = meals,
                        isLoading = false,
                        error = null
                    )
                }

                result.doActionIfError { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Ошибка загрузки данных"
                    )
                }
            }
        }
    }

    private fun createMealData(
        mealType: MealType,
        emoji: String,
        name: String,
        groupedEntries: Map<MealType, List<FoodEntry>>
    ): MealData {
        val entries = groupedEntries[mealType] ?: emptyList()
        val totalCalories = entries.sumOf { it.calories }
        val time = entries.firstOrNull()?.timestamp?.format(DateTimeFormatter.ofPattern("HH:mm"))

        val foodItems = entries.map { entry ->
            FoodItemData(
                name = entry.foodName,
                weight = "${entry.servings}x",
                calories = entry.calories
            )
        }

        return MealData(
            id = mealType.ordinal.toLong(),
            emoji = emoji,
            name = name,
            time = time,
            totalCalories = totalCalories,
            foodItems = foodItems
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
