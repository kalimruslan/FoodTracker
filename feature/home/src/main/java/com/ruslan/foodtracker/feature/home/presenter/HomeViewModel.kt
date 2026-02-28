package com.ruslan.foodtracker.feature.home.presenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruslan.foodtracker.core.ui.components.FoodItemData
import com.ruslan.foodtracker.domain.model.FoodEntry
import com.ruslan.foodtracker.domain.model.MealType
import com.ruslan.foodtracker.core.common.util.DateTimeUtils
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

        // Кэш хранится в ViewModel, а не в UiState — исключает shallow copy при каждом copy().
        // Доступ безопасен без синхронизации: viewModelScope использует Dispatchers.Main,
        // который однопоточен — все корутины выполняются последовательно на главном потоке.
        private val entriesCache = mutableMapOf<LocalDate, List<FoodEntry>>()

        init {
            loadEntriesForSelectedDate()
        }

        fun onDateSelected(date: LocalDate) {
            val today = LocalDate.now()
            val newWeekStart = DateTimeUtils.weekStart(date)
            val todayWeekStart = DateTimeUtils.weekStart(today)
            _uiState.value = _uiState.value.copy(
                selectedDate = date,
                selectedDayIndex = date.dayOfWeek.value - 1,
                currentWeekStart = newWeekStart,
                showTodayButton = newWeekStart != todayWeekStart,
                canGoNextWeek = newWeekStart < todayWeekStart,
            )
            loadEntriesForSelectedDate()
        }

        /**
         * Пользователь нажал на конкретный день в полосе.
         * [dayIndex] — 0-based индекс дня текущей недели (0=Пн, 6=Вс).
         * Блокируем выбор будущих дней.
         */
        fun onDaySelected(dayIndex: Int) {
            val newDate = _uiState.value.currentWeekStart.plusDays(dayIndex.toLong())
            val today = LocalDate.now()
            if (newDate.isAfter(today)) return
            _uiState.value = _uiState.value.copy(
                selectedDate = newDate,
                selectedDayIndex = dayIndex,
            )
            loadEntriesForSelectedDate()
        }

        /**
         * Пользователь нажал стрелку «‹» — переход на прошлую неделю.
         * Выбранный день остаётся тем же индексом, но уже в контексте новой недели.
         * Если дата оказывается в будущем — выбираем последний доступный день недели.
         */
        fun onPreviousWeek() {
            val newWeekStart = _uiState.value.currentWeekStart.minusWeeks(1)
            val today = LocalDate.now()
            val targetDate = newWeekStart.plusDays(_uiState.value.selectedDayIndex.toLong())
            val clampedDate = if (targetDate.isAfter(today)) today else targetDate
            val clampedIndex = clampedDate.dayOfWeek.value - 1

            _uiState.value = _uiState.value.copy(
                currentWeekStart = newWeekStart,
                selectedDate = clampedDate,
                selectedDayIndex = clampedIndex,
                showTodayButton = newWeekStart != DateTimeUtils.weekStart(today),
                canGoNextWeek = true, // из прошлой недели всегда можно идти вперёд
            )
            loadEntriesForSelectedDate()
        }

        /**
         * Пользователь нажал стрелку «›» — переход на следующую неделю.
         * Переход запрещён, если текущая неделя == неделя сегодняшнего дня.
         */
        fun onNextWeek() {
            val today = LocalDate.now()
            val currentWeekStart = _uiState.value.currentWeekStart
            val todayWeekStart = DateTimeUtils.weekStart(today)

            if (currentWeekStart >= todayWeekStart) return

            val newWeekStart = currentWeekStart.plusWeeks(1)
            val targetDate = newWeekStart.plusDays(_uiState.value.selectedDayIndex.toLong())
            val clampedDate = if (targetDate.isAfter(today)) today else targetDate
            val clampedIndex = clampedDate.dayOfWeek.value - 1
            val isNowCurrentWeek = newWeekStart == todayWeekStart

            _uiState.value = _uiState.value.copy(
                currentWeekStart = newWeekStart,
                selectedDate = clampedDate,
                selectedDayIndex = clampedIndex,
                showTodayButton = !isNowCurrentWeek,
                canGoNextWeek = !isNowCurrentWeek, // нельзя идти вперёд с текущей недели
            )
            loadEntriesForSelectedDate()
        }

        /**
         * Пользователь нажал кнопку «Сегодня».
         * Возвращает навигатор на текущую неделю и выбирает сегодня.
         */
        fun onTodayClicked() {
            val today = LocalDate.now()
            _uiState.value = _uiState.value.copy(
                currentWeekStart = DateTimeUtils.weekStart(today),
                selectedDate = today,
                selectedDayIndex = today.dayOfWeek.value - 1,
                showTodayButton = false,
                canGoNextWeek = false, // на текущей неделе нельзя идти вперёд
            )
            loadEntriesForSelectedDate()
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
                    invalidateCacheForDate(_uiState.value.selectedDate)
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
                    invalidateCacheForDate(_uiState.value.selectedDate)
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
                    invalidateCacheForDate(_uiState.value.selectedDate)
                    loadEntriesForSelectedDate()
                }
            }
        }

        // ─── Загрузка данных ─────────────────────────────────────────────────

        private fun loadEntriesForSelectedDate() {
            loadJob?.cancel()
            loadJob = viewModelScope.launch {
                val selectedDate = _uiState.value.selectedDate

                // Проверяем приватный кэш: если данные есть — рендерим сразу без Loading
                val cached = entriesCache[selectedDate]
                if (cached != null) {
                    applyEntriesToState(cached)
                    return@launch
                }

                getEntriesByDateUseCase(selectedDate).collect { result ->
                    result.doActionIfLoading {
                        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                    }

                    result.doActionIfSuccess { entries ->
                        // Сохраняем в приватный кэш — нет race condition и лишних copy()
                        entriesCache[selectedDate] = entries
                        applyEntriesToState(entries)
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

        private fun applyEntriesToState(entries: List<FoodEntry>) {
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

        private fun invalidateCacheForDate(date: LocalDate) {
            entriesCache.remove(date)
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
    val selectedDayIndex: Int = LocalDate.now().dayOfWeek.value - 1, // 0=Пн … 6=Вс
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
    /**
     * Понедельник отображаемой недели.
     * По умолчанию — начало текущей недели.
     */
    val currentWeekStart: LocalDate = DateTimeUtils.weekStart(LocalDate.now()),
    /**
     * Показывать ли кнопку «Сегодня».
     * true, если отображаемая неделя != текущая неделя.
     */
    val showTodayButton: Boolean = false,
    /**
     * Можно ли перейти на следующую неделю.
     * false если уже на текущей неделе (нет будущих недель).
     * Вычисляется в ViewModel — не в Composable, избегая LocalDate.now() при рекомпозиции.
     */
    val canGoNextWeek: Boolean = false,
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
