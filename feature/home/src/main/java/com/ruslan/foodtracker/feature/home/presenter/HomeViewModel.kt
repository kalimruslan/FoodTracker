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
import com.ruslan.foodtracker.domain.usecase.entry.InsertFoodEntryUseCase
import com.ruslan.foodtracker.domain.usecase.entry.UpdateFoodEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * ViewModel для главного экрана (Дневник питания)
 */
@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val getEntriesByDateUseCase: GetEntriesByDateUseCase,
        private val deleteFoodEntryUseCase: DeleteFoodEntryUseCase,
        private val insertFoodEntryUseCase: InsertFoodEntryUseCase,
        private val updateFoodEntryUseCase: UpdateFoodEntryUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(HomeUiState())
        val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

        private var loadJob: Job? = null

        init {
            loadEntriesForSelectedDate()
        }

        fun onDateSelected(date: LocalDate) {
            _uiState.value = _uiState.value.copy(selectedDate = date)
            loadEntriesForSelectedDate()
        }

        fun onDaySelected(dayIndex: Int) {
            _uiState.value = _uiState.value.copy(selectedDayIndex = dayIndex)
        }

        fun onAddWaterGlass() {
            val currentWater = _uiState.value.waterGlasses
            _uiState.value = _uiState.value.copy(waterGlasses = currentWater + 1)
        }

        // ─── Удаление записи ────────────────────────────────────────────────

        fun onDeleteEntry(entryId: Long) {
            val entry = _uiState.value.allEntries.find { it.id == entryId } ?: return
            viewModelScope.launch {
                deleteFoodEntryUseCase(entry).doActionIfSuccess {
                    _uiState.value = _uiState.value.copy(
                        pendingDeleteEntry = entry,
                        showDeleteSnackbar = true,
                    )
                    loadEntriesForSelectedDate()
                }
            }
        }

        fun onUndoDelete() {
            val entry = _uiState.value.pendingDeleteEntry ?: return
            viewModelScope.launch {
                // Восстанавливаем с id=0 — Room сгенерирует новый первичный ключ
                insertFoodEntryUseCase(entry.copy(id = 0L)).doActionIfSuccess {
                    _uiState.value = _uiState.value.copy(
                        pendingDeleteEntry = null,
                        showDeleteSnackbar = false,
                    )
                    loadEntriesForSelectedDate()
                }
            }
        }

        fun onDeleteSnackbarDismissed() {
            _uiState.value = _uiState.value.copy(
                pendingDeleteEntry = null,
                showDeleteSnackbar = false,
            )
        }

        // ─── Редактирование граммовки ────────────────────────────────────────

        fun onEditEntry(entryId: Long) {
            val entry = _uiState.value.allEntries.find { it.id == entryId } ?: return
            _uiState.value = _uiState.value.copy(editingEntry = entry)
        }

        fun onEditDismiss() {
            _uiState.value = _uiState.value.copy(editingEntry = null)
        }

        /**
         * Обновляет граммовку записи и пересчитывает макросы через коэффициент.
         * Защита от деления на ноль: если [entry.amountGrams] <= 0 — операция отменяется.
         */
        fun onUpdateEntryAmount(
            entry: FoodEntry,
            newAmountGrams: Double
        ) {
            if (newAmountGrams <= 0) return
            if (entry.amountGrams <= 0) {
                // Нельзя пересчитать макросы без исходного значения граммов
                _uiState.value = _uiState.value.copy(editingEntry = null)
                return
            }
            val ratio = newAmountGrams / entry.amountGrams
            val updated = entry.copy(
                amountGrams = newAmountGrams,
                calories = (entry.calories * ratio).roundToInt(),
                protein = entry.protein * ratio,
                carbs = entry.carbs * ratio,
                fat = entry.fat * ratio,
            )
            viewModelScope.launch {
                updateFoodEntryUseCase(updated).doActionIfSuccess {
                    _uiState.value = _uiState.value.copy(editingEntry = null)
                    loadEntriesForSelectedDate()
                }
            }
        }

        // ─── Загрузка данных ─────────────────────────────────────────────────

        private fun loadEntriesForSelectedDate() {
            loadJob?.cancel()
            loadJob = viewModelScope.launch {
                val selectedDate = _uiState.value.selectedDate
                getEntriesByDateUseCase(selectedDate).collect { result ->
                    result.doActionIfLoading {
                        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                    }

                    result.doActionIfSuccess { entries ->
                        val groupedEntries = entries.groupBy { it.mealType }

                        val totalCalories = entries.sumOf { it.calories }.toFloat()
                        val totalProtein = entries.sumOf { it.protein }.toFloat()
                        val totalFat = entries.sumOf { it.fat }.toFloat()
                        val totalCarbs = entries.sumOf { it.carbs }.toFloat()

                        val targetCalories = 2200f
                        val targetProtein = 140f
                        val targetFat = 73f
                        val targetCarbs = 275f
                        val targetFiber = 30f

                        val meals = listOf(
                            createMealData(MealType.BREAKFAST, "🌅", "Завтрак", groupedEntries),
                            createMealData(MealType.LUNCH, "☀️", "Обед", groupedEntries),
                            createMealData(MealType.DINNER, "🌙", "Ужин", groupedEntries),
                            createMealData(MealType.SNACK, "🍎", "Перекус", groupedEntries),
                        )

                        _uiState.value = _uiState.value.copy(
                            allEntries = entries,
                            consumedCalories = totalCalories,
                            targetCalories = targetCalories,
                            protein = MacroData(consumed = totalProtein, target = targetProtein),
                            fat = MacroData(consumed = totalFat, target = targetFat),
                            carbs = MacroData(consumed = totalCarbs, target = targetCarbs),
                            fiber = MacroData(consumed = 0f, target = targetFiber),
                            meals = meals,
                            isLoading = false,
                            error = null,
                        )
                    }

                    result.doActionIfError {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Ошибка загрузки данных",
                        )
                    }
                }
            }
        }

        private fun createMealData(
            mealType: MealType,
            emoji: String,
            name: String,
            groupedEntries: Map<MealType, List<FoodEntry>>,
        ): MealData {
            val entries = groupedEntries[mealType] ?: emptyList()
            val totalCalories = entries.sumOf { it.calories }
            val time = entries.firstOrNull()?.timestamp?.format(DateTimeFormatter.ofPattern("HH:mm"))

            val foodItems = entries.map { entry ->
                FoodItemData(
                    name = entry.foodName,
                    weight = "${entry.amountGrams.toInt()}г",
                    calories = entry.calories,
                    entryId = entry.id,
                )
            }

            return MealData(
                id = mealType.ordinal.toLong(),
                mealType = mealType,
                emoji = emoji,
                name = name,
                time = time,
                totalCalories = totalCalories,
                foodItems = foodItems,
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
    val error: String? = null,
    // Все записи для текущей даты (используются для delete/edit по entryId)
    val allEntries: List<FoodEntry> = emptyList(),
    // Запись, открытая для редактирования граммовки
    val editingEntry: FoodEntry? = null,
    // Запись, удалённая последней (для Undo)
    val pendingDeleteEntry: FoodEntry? = null,
    val showDeleteSnackbar: Boolean = false,
)

/**
 * Данные макронутриента
 */
data class MacroData(
    val consumed: Float,
    val target: Float,
)

/**
 * Данные приёма пищи
 */
data class MealData(
    val id: Long,
    val mealType: MealType,
    val emoji: String,
    val name: String,
    val time: String?,
    val totalCalories: Int,
    val foodItems: List<FoodItemData>,
)
