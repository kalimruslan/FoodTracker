package com.ruslan.foodtracker.feature.home.presenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruslan.foodtracker.domain.model.Food
import com.ruslan.foodtracker.domain.model.FoodEntry
import com.ruslan.foodtracker.domain.model.MealType
import com.ruslan.foodtracker.domain.model.NetworkResult
import com.ruslan.foodtracker.domain.model.doActionIfSuccess
import com.ruslan.foodtracker.domain.usecase.entry.GetRecentFoodEntriesUseCase
import com.ruslan.foodtracker.domain.usecase.entry.InsertFoodEntryUseCase
import com.ruslan.foodtracker.domain.usecase.food.GetFavoriteFoodsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import kotlin.math.roundToInt

enum class QuickAddTab(val label: String) {
    FAVORITES("Избранное"),
    RECENT("Недавние"),
}

enum class QuickAddStep {
    FOOD_SELECTION,
    AMOUNT_INPUT,
}

data class QuickAddUiState(
    val isVisible: Boolean = false,
    val selectedMealType: MealType = MealType.BREAKFAST,
    val date: LocalDate = LocalDate.now(),
    val selectedTab: QuickAddTab = QuickAddTab.FAVORITES,
    val step: QuickAddStep = QuickAddStep.FOOD_SELECTION,
    val favoriteFoods: List<Food> = emptyList(),
    val recentEntries: List<FoodEntry> = emptyList(),
    val isLoadingRecent: Boolean = false,
    val selectedFood: Food? = null,
    val amountText: String = "",
    val effectiveAmountGrams: Double = 0.0,
    val calculatedCalories: Int = 0,
    val calculatedProtein: Double = 0.0,
    val calculatedCarbs: Double = 0.0,
    val calculatedFat: Double = 0.0,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
) {
    val isAmountValid: Boolean
        get() = amountText.toDoubleOrNull()?.let { it > 0 } ?: false
}

@HiltViewModel
class QuickAddViewModel
    @Inject
    constructor(
        private val getFavoriteFoodsUseCase: GetFavoriteFoodsUseCase,
        private val getRecentFoodEntriesUseCase: GetRecentFoodEntriesUseCase,
        private val insertFoodEntryUseCase: InsertFoodEntryUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(QuickAddUiState())
        val uiState: StateFlow<QuickAddUiState> = _uiState.asStateFlow()

        private var favoritesJob: Job? = null
        private var recentJob: Job? = null

        fun open(
            mealType: MealType,
            date: LocalDate,
        ) {
            favoritesJob?.cancel()
            recentJob?.cancel()
            _uiState.value = QuickAddUiState(
                selectedMealType = mealType,
                date = date,
                isVisible = true,
            )
            loadFavorites()
            loadRecent()
        }

        fun close() {
            _uiState.value = QuickAddUiState()
        }

        fun onFoodSelected(food: Food) {
            _uiState.value = _uiState.value.copy(
                selectedFood = food,
                amountText = food.servingSize.toInt().toString(),
                step = QuickAddStep.AMOUNT_INPUT,
            )
            recalculate(food.servingSize)
        }

        fun onRecentEntrySelected(entry: FoodEntry) {
            // Нормализуем нутриенты к 100г для корректного recalculate
            val factor = if (entry.amountGrams > 0) 100.0 / entry.amountGrams else 1.0
            val syntheticFood = Food(
                id = entry.foodId,
                name = entry.foodName,
                calories = (entry.calories * factor).roundToInt(),
                protein = entry.protein * factor,
                carbs = entry.carbs * factor,
                fat = entry.fat * factor,
                servingSize = 100.0,
                servingUnit = "г",
            )
            _uiState.value = _uiState.value.copy(
                selectedFood = syntheticFood,
                amountText = entry.amountGrams.toInt().toString(),
                step = QuickAddStep.AMOUNT_INPUT,
            )
            recalculate(entry.amountGrams)
        }

        fun onAmountChanged(text: String) {
            _uiState.value = _uiState.value.copy(amountText = text)
            val grams = text.toDoubleOrNull() ?: return
            recalculate(grams)
        }

        fun onMealTypeChanged(mealType: MealType) {
            _uiState.value = _uiState.value.copy(selectedMealType = mealType)
        }

        fun onTabSelected(tab: QuickAddTab) {
            _uiState.value = _uiState.value.copy(selectedTab = tab)
        }

        fun onBackToSelection() {
            _uiState.value = _uiState.value.copy(
                selectedFood = null,
                step = QuickAddStep.FOOD_SELECTION,
                amountText = "",
                error = null,
            )
        }

        fun saveEntry() {
            val state = _uiState.value
            val food = state.selectedFood ?: return
            val grams = state.amountText.toDoubleOrNull() ?: return
            if (grams <= 0) {
                _uiState.value = state.copy(error = "Введите корректное количество")
                return
            }
            val servingSize = food.servingSize.coerceAtLeast(1.0)
            val servings = grams / servingSize

            val entry = FoodEntry(
                foodId = food.id,
                foodName = food.name,
                servings = servings,
                amountGrams = grams,
                calories = state.calculatedCalories,
                protein = state.calculatedProtein,
                carbs = state.calculatedCarbs,
                fat = state.calculatedFat,
                timestamp = LocalDateTime.of(state.date, LocalTime.now()),
                mealType = state.selectedMealType,
            )

            viewModelScope.launch {
                _uiState.value = state.copy(isLoading = true, error = null)
                val result = insertFoodEntryUseCase(entry)
                result.doActionIfSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSaved = true,
                        isVisible = false,
                    )
                }
                if (result is NetworkResult.Error) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Ошибка сохранения",
                    )
                }
            }
        }

        fun onSavedHandled() {
            _uiState.value = _uiState.value.copy(isSaved = false)
        }

        private fun loadFavorites() {
            favoritesJob = viewModelScope.launch {
                getFavoriteFoodsUseCase().collect { result ->
                    if (result is NetworkResult.Success) {
                        _uiState.update { it.copy(favoriteFoods = result.data) }
                    }
                }
            }
        }

        private fun loadRecent() {
            recentJob = viewModelScope.launch {
                getRecentFoodEntriesUseCase(limit = 20).collect { result ->
                    when (result) {
                        is NetworkResult.Loading -> _uiState.update { it.copy(isLoadingRecent = true) }
                        is NetworkResult.Success -> _uiState.update { it.copy(
                            recentEntries = result.data,
                            isLoadingRecent = false,
                        ) }
                        is NetworkResult.Error -> _uiState.update { it.copy(isLoadingRecent = false) }
                        else -> Unit
                    }
                }
            }
        }

        private fun recalculate(grams: Double) {
            val food = _uiState.value.selectedFood ?: return
            val servingSize = food.servingSize.coerceAtLeast(1.0)
            val servings = grams / servingSize
            _uiState.value = _uiState.value.copy(
                calculatedCalories = (food.calories * servings).roundToInt(),
                calculatedProtein = food.protein * servings,
                calculatedCarbs = food.carbs * servings,
                calculatedFat = food.fat * servings,
                effectiveAmountGrams = grams,
            )
        }
    }
