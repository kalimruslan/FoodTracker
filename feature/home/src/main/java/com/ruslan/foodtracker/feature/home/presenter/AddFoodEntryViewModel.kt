package com.ruslan.foodtracker.feature.home.presenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruslan.foodtracker.core.navigation.NavRoutes
import com.ruslan.foodtracker.domain.model.FoodEntry
import com.ruslan.foodtracker.domain.model.MealType
import com.ruslan.foodtracker.domain.model.doActionIfError
import com.ruslan.foodtracker.domain.model.doActionIfSuccess
import com.ruslan.foodtracker.domain.usecase.entry.InsertFoodEntryUseCase
import com.ruslan.foodtracker.domain.usecase.food.ToggleFavoriteFoodUseCase
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
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.math.roundToInt

enum class InputMode(
    val suffix: String
) {
    GRAMS("г"),
    KCAL("ккал"),
    SERVING("порц")
}

fun InputMode.getLabel(portionGrams: Double): String =
    when (this) {
        InputMode.GRAMS -> "Граммы"
        InputMode.KCAL -> "Ккал"
        InputMode.SERVING -> "Порция (${portionGrams.toInt()}г)"
    }

@HiltViewModel
class AddFoodEntryViewModel
    @Inject
    constructor(
        private val insertFoodEntryUseCase: InsertFoodEntryUseCase,
        private val toggleFavoriteFoodUseCase: ToggleFavoriteFoodUseCase
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(AddFoodEntryUiState())
        val uiState: StateFlow<AddFoodEntryUiState> = _uiState.asStateFlow()

        private val isInitialized = AtomicBoolean(false)
        private var toggleFavoriteJob: Job? = null

        /**
         * Инициализация данных продукта из навигационного route.
         * Вызывается только один раз (атомарная защита).
         */
        fun init(route: NavRoutes.AddFoodEntry) {
            if (!isInitialized.compareAndSet(false, true)) return
            val mealType = runCatching { MealType.valueOf(route.mealType) }
                .getOrDefault(MealType.BREAKFAST)
            _uiState.value = _uiState.value.copy(
                foodId = route.foodId,
                foodName = route.foodName,
                brand = route.brand.takeIf { it.isNotEmpty() },
                caloriesPerServing = route.caloriesPerServing,
                proteinPerServing = route.proteinPerServing,
                carbsPerServing = route.carbsPerServing,
                fatPerServing = route.fatPerServing,
                servingSize = route.servingSize,
                servingUnit = route.servingUnit,
                selectedMealType = mealType,
                date = LocalDate.parse(route.date),
                amountText = route.servingSize.toInt().toString(),
                isFavorite = route.isFavorite,
                selectedInputMode = InputMode.GRAMS,
                portionGrams = route.servingSize, // порция = размер одной порции из route
                effectiveAmountGrams = route.servingSize
            )
            recalculate(route.servingSize)
        }

        /**
         * Изменение количества (значение в текущей единице измерения)
         */
        fun onAmountChanged(text: String) {
            _uiState.value = _uiState.value.copy(amountText = text)
            val amount = text.toDoubleOrNull() ?: return
            recalculate(amount)
        }

        fun onMealTypeChanged(mealType: MealType) {
            _uiState.value = _uiState.value.copy(selectedMealType = mealType)
        }

        /**
         * Смена режима ввода с конвертацией текущего значения в новый режим.
         * Конвертация происходит через effectiveAmountGrams для точности.
         */
        fun onInputModeChanged(mode: InputMode) {
            val state = _uiState.value
            val newAmountText = when (mode) {
                InputMode.GRAMS -> String.format(Locale.US, "%.0f", state.effectiveAmountGrams.coerceAtLeast(1.0))
                InputMode.KCAL -> {
                    // Конвертируем из effectiveAmountGrams, не из Int-калорий (избегаем потери точности)
                    if (state.servingSize > 0) {
                        val servings = state.effectiveAmountGrams / state.servingSize
                        val exactKcal = state.caloriesPerServing * servings
                        String.format(Locale.US, "%.1f", exactKcal.coerceAtLeast(0.1))
                    } else {
                        state.calculatedCalories.toString()
                    }
                }
                InputMode.SERVING -> {
                    val portions = if (state.portionGrams > 0) state.effectiveAmountGrams / state.portionGrams else 1.0
                    String.format(Locale.US, "%.1f", portions.coerceAtLeast(0.1))
                }
            }
            _uiState.value = _uiState.value.copy(
                selectedInputMode = mode,
                amountText = newAmountText
            )
            recalculate(newAmountText.toDoubleOrNull() ?: 1.0)
        }

        /**
         * Переключение избранного с защитой от двойного нажатия.
         * Если foodId == 0 (продукт не сохранён локально), ничего не делаем.
         */
        fun onToggleFavorite() {
            val state = _uiState.value
            if (state.foodId == 0L) return
            val newFavorite = !state.isFavorite
            _uiState.value = _uiState.value.copy(isFavorite = newFavorite) // Optimistic update
            toggleFavoriteJob?.cancel()
            toggleFavoriteJob = viewModelScope.launch {
                val result = toggleFavoriteFoodUseCase(state.foodId, newFavorite)
                result.doActionIfError {
                    // Откатываем при ошибке
                    _uiState.value = _uiState.value.copy(isFavorite = !newFavorite)
                }
            }
        }

        /**
         * Сохранение записи в БД.
         * По завершении выставляет [AddFoodEntryUiState.isSaved] = true,
         * на который реагирует Screen через LaunchedEffect.
         */
        fun saveEntry() {
            val state = _uiState.value
            val rawInput = state.amountText.toDoubleOrNull() ?: return
            if (rawInput <= 0) return
            val amountGrams = state.effectiveAmountGrams
            if (amountGrams <= 0) {
                _uiState.value = _uiState.value.copy(error = "Некорректное количество")
                return
            }

            val servingSize = state.servingSize.coerceAtLeast(1.0)
            val servings = amountGrams / servingSize

            val entry = FoodEntry(
                foodId = state.foodId,
                foodName = state.foodName,
                servings = servings,
                amountGrams = amountGrams,
                calories = state.calculatedCalories,
                protein = state.calculatedProtein,
                carbs = state.calculatedCarbs,
                fat = state.calculatedFat,
                timestamp = LocalDateTime.of(state.date, LocalTime.now()),
                mealType = state.selectedMealType
            )

            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val result = insertFoodEntryUseCase(entry)
                result.doActionIfSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
                }
                result.doActionIfError {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Не удалось сохранить запись"
                    )
                }
            }
        }

        private fun recalculate(rawInput: Double) {
            _uiState.update { state ->
                val servingSize = state.servingSize
                if (servingSize <= 0) return@update state

                val amountGrams = when (state.selectedInputMode) {
                    InputMode.GRAMS -> rawInput
                    InputMode.KCAL -> {
                        if (state.caloriesPerServing <= 0) return@update state
                        (rawInput / state.caloriesPerServing) * servingSize
                    }
                    InputMode.SERVING -> rawInput * state.portionGrams
                }

                val servings = amountGrams / servingSize
                state.copy(
                    calculatedCalories = (state.caloriesPerServing * servings).roundToInt(),
                    calculatedProtein = state.proteinPerServing * servings,
                    calculatedCarbs = state.carbsPerServing * servings,
                    calculatedFat = state.fatPerServing * servings,
                    effectiveAmountGrams = amountGrams
                )
            }
        }
    }

data class AddFoodEntryUiState(
    val foodId: Long = 0L,
    val foodName: String = "",
    val brand: String? = null,
    val caloriesPerServing: Int = 0,
    val proteinPerServing: Double = 0.0,
    val carbsPerServing: Double = 0.0,
    val fatPerServing: Double = 0.0,
    val servingSize: Double = 100.0,
    val servingUnit: String = "g",
    val selectedMealType: MealType = MealType.BREAKFAST,
    val date: LocalDate = LocalDate.now(),
    val amountText: String = "100",
    val selectedInputMode: InputMode = InputMode.GRAMS,
    val portionGrams: Double = 100.0,
    val effectiveAmountGrams: Double = 100.0,
    val calculatedCalories: Int = 0,
    val calculatedProtein: Double = 0.0,
    val calculatedCarbs: Double = 0.0,
    val calculatedFat: Double = 0.0,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
) {
    val isAmountValid: Boolean
        get() = amountText.toDoubleOrNull()?.let { it > 0 } ?: false
}
