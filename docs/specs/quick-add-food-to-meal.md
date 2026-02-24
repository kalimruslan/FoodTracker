# Быстрое добавление продуктов в приём пищи

**GitHub Issue:** #7
**Статус:** Draft
**Автор:** Android Architect
**Дата:** 2026-02-24

---

## 1. Описание фичи и пользовательские истории

### Описание
Функциональность позволяет пользователю добавлять продукты из избранного
или недавно использованных в приём пищи (завтрак/обед/ужин/перекус) без
обязательного прохождения через полный поиск. Быстрый ввод граммажа
происходит прямо в bottom sheet с главного экрана.

### Пользовательские истории

**US-1 (Основная):** Как пользователь, я хочу нажать "+" в карточке
завтрака и выбрать продукт из избранных без поиска, чтобы добавить его
в 2 клика.

**US-2:** Как пользователь, я хочу видеть список недавно добавленных
продуктов при нажатии "+" в карточке приёма пищи, чтобы повторять
привычный рацион быстро.

**US-3:** Как пользователь, я хочу вводить вес в граммах прямо в
bottom sheet, не переходя на отдельный экран.

**US-4:** Как пользователь, я хочу видеть мгновенный расчёт калорий при
вводе веса (live preview), чтобы контролировать порцию.

**US-5:** Как пользователь, я хочу при необходимости перейти к полному
поиску из bottom sheet, если нужный продукт отсутствует в быстром списке.

---

## 2. Архитектурный план

### Принципы

- Фича размещается в модуле `feature:home` (не создаётся новый модуль).
- Domain слой: добавляются 2 новых use case + 1 метод в интерфейс репозитория.
- Data слой: добавляется один новый DAO-запрос для "недавних" продуктов.
- Presentation: новый bottom sheet внутри `HomeScreen` + `QuickAddViewModel`.

### Что создаётся (по слоям)

```
domain/usecase/entry/
  └── GetRecentFoodEntriesUseCase.kt   [NEW]

domain/usecase/food/
  └── GetFavoriteFoodsUseCase.kt       [NEW]

data/local/dao/
  └── FoodEntryDao.kt                  [MODIFY - новый запрос getRecentFoods]

feature/home/presenter/
  ├── QuickAddBottomSheet.kt           [NEW]
  └── QuickAddViewModel.kt             [NEW]

feature/home/presenter/
  └── HomeScreen.kt                    [MODIFY - интеграция bottom sheet]
```

---

## 3. Domain Layer

### 3.1 Изменение FoodEntryRepository

**Файл:** `domain/src/main/java/com/ruslan/foodtracker/domain/repository/FoodEntryRepository.kt`

Добавить метод:

```kotlin
/**
 * Последние уникальные продукты (дедупликация по foodId),
 * отсортированные по убыванию timestamp.
 * @param limit количество записей
 */
fun getRecentEntries(limit: Int): Flow<NetworkResult<List<FoodEntry>>>
```

### 3.2 Новый UseCase: GetRecentFoodEntriesUseCase

**Файл:** `domain/src/main/java/com/ruslan/foodtracker/domain/usecase/entry/GetRecentFoodEntriesUseCase.kt`

```kotlin
class GetRecentFoodEntriesUseCase @Inject constructor(
    private val repository: FoodEntryRepository
) {
    /**
     * @param limit Максимальное количество продуктов (default: 20)
     */
    operator fun invoke(limit: Int = 20): Flow<NetworkResult<List<FoodEntry>>> =
        repository.getRecentEntries(limit)
}
```

### 3.3 Новый UseCase: GetFavoriteFoodsUseCase

**Файл:** `domain/src/main/java/com/ruslan/foodtracker/domain/usecase/food/GetFavoriteFoodsUseCase.kt`

```kotlin
class GetFavoriteFoodsUseCase @Inject constructor(
    private val repository: FoodRepository
) {
    operator fun invoke(): Flow<NetworkResult<List<Food>>> =
        repository.getFavoriteFoods()
}
```

> Примечание: `FoodRepository.getFavoriteFoods()` уже реализован.
> UseCase нужен для соответствия архитектурному контракту.

---

## 4. Data Layer

### 4.1 Изменение FoodEntryDao

**Файл:** `data/src/main/java/com/ruslan/foodtracker/data/local/dao/FoodEntryDao.kt`

Добавить запрос:

```kotlin
/**
 * Последние уникальные записи по foodId, отсортированные по убыванию timestamp.
 * Для каждого foodId берётся только самая свежая запись (GROUP BY + MAX).
 */
@Query("""
    SELECT * FROM food_entries
    WHERE id IN (
        SELECT id FROM food_entries
        GROUP BY foodId
        ORDER BY MAX(timestamp) DESC
        LIMIT :limit
    )
    ORDER BY timestamp DESC
""")
fun getRecentEntries(limit: Int): Flow<List<FoodEntryEntity>>
```

### 4.2 Изменение FoodEntryRepositoryImpl

**Файл:** `data/src/main/java/com/ruslan/foodtracker/data/repository/FoodEntryRepositoryImpl.kt`

```kotlin
override fun getRecentEntries(limit: Int): Flow<NetworkResult<List<FoodEntry>>> =
    foodEntryDao
        .getRecentEntries(limit)
        .map<List<FoodEntryEntity>, NetworkResult<List<FoodEntry>>> { entities ->
            NetworkResult.Success(entities.map { it.toDomain() })
        }
        .onStart { emit(NetworkResult.Loading) }
        .catch { e ->
            emit(NetworkResult.Error(e.message ?: "Error", exception = e))
        }
```

### 4.3 Mapper (без изменений)

Существующие маппинги в `Mappers.kt` полностью покрывают новую функциональность.

---

## 5. Presentation Layer

### 5.1 QuickAddUiState и вспомогательные типы

```kotlin
enum class QuickAddTab(val label: String) {
    FAVORITES("Избранное"),
    RECENT("Недавние")
}

enum class QuickAddStep {
    FOOD_SELECTION,  // Пользователь выбирает продукт из списка
    AMOUNT_INPUT     // Пользователь вводит граммаж
}

data class QuickAddUiState(
    val isVisible: Boolean = false,
    val selectedMealType: MealType = MealType.BREAKFAST,
    val date: LocalDate = LocalDate.now(),
    val selectedTab: QuickAddTab = QuickAddTab.FAVORITES,
    val step: QuickAddStep = QuickAddStep.FOOD_SELECTION,

    // Данные списков
    val favoriteFoods: List<Food> = emptyList(),
    val recentEntries: List<FoodEntry> = emptyList(),
    val isLoadingRecent: Boolean = false,

    // Выбранный продукт
    val selectedFood: Food? = null,
    val amountText: String = "",

    // Расчётные нутриенты (live preview)
    val effectiveAmountGrams: Double = 0.0,
    val calculatedCalories: Int = 0,
    val calculatedProtein: Double = 0.0,
    val calculatedCarbs: Double = 0.0,
    val calculatedFat: Double = 0.0,

    // Состояние
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
) {
    val isAmountValid: Boolean
        get() = amountText.toDoubleOrNull()?.let { it > 0 } ?: false
}
```

### 5.2 QuickAddViewModel

**Файл:** `feature/home/src/main/java/com/ruslan/foodtracker/feature/home/presenter/QuickAddViewModel.kt`

```kotlin
@HiltViewModel
class QuickAddViewModel @Inject constructor(
    private val getFavoriteFoodsUseCase: GetFavoriteFoodsUseCase,
    private val getRecentFoodEntriesUseCase: GetRecentFoodEntriesUseCase,
    private val insertFoodEntryUseCase: InsertFoodEntryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuickAddUiState())
    val uiState: StateFlow<QuickAddUiState> = _uiState.asStateFlow()

    fun open(mealType: MealType, date: LocalDate) {
        _uiState.value = QuickAddUiState(
            selectedMealType = mealType,
            date = date,
            isVisible = true
        )
        loadFavorites()
        loadRecent()
    }

    fun close() { _uiState.value = QuickAddUiState() }

    fun onFoodSelected(food: Food) {
        _uiState.value = _uiState.value.copy(
            selectedFood = food,
            amountText = food.servingSize.toInt().toString(),
            step = QuickAddStep.AMOUNT_INPUT
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
            servingUnit = "г"
        )
        _uiState.value = _uiState.value.copy(
            selectedFood = syntheticFood,
            amountText = entry.amountGrams.toInt().toString(),
            step = QuickAddStep.AMOUNT_INPUT
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
            error = null
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
            mealType = state.selectedMealType
        )

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            when (val result = insertFoodEntryUseCase(entry)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSaved = true,
                        isVisible = false
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Ошибка сохранения"
                    )
                }
                else -> Unit
            }
        }
    }

    fun onSavedHandled() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            getFavoriteFoodsUseCase().collect { result ->
                if (result is NetworkResult.Success) {
                    _uiState.value = _uiState.value.copy(favoriteFoods = result.data)
                }
            }
        }
    }

    private fun loadRecent() {
        viewModelScope.launch {
            getRecentFoodEntriesUseCase(limit = 20).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.value = _uiState.value.copy(isLoadingRecent = true)
                    is NetworkResult.Success -> _uiState.value = _uiState.value.copy(
                        recentEntries = result.data,
                        isLoadingRecent = false
                    )
                    is NetworkResult.Error -> _uiState.value = _uiState.value.copy(isLoadingRecent = false)
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
            effectiveAmountGrams = grams
        )
    }
}
```

### 5.3 QuickAddBottomSheet (UI)

**Файл:** `feature/home/src/main/java/com/ruslan/foodtracker/feature/home/presenter/QuickAddBottomSheet.kt`

**Шаг 1 — FOOD_SELECTION:**
```
┌──────────────────────────────────────┐
│  Добавить в [Завтрак]         [×]    │
│                                      │
│  [⭐ Избранное]  [🕐 Недавние]      │  <- TabRow
│                                      │
│  Список продуктов (LazyColumn):      │
│  ┌────────────────────────────────┐  │
│  │ Овсяная каша    88 ккал  [→]  │  │
│  ├────────────────────────────────┤  │
│  │ Куриная грудка  110 ккал [→]  │  │
│  └────────────────────────────────┘  │
│                                      │
│  [Открыть полный поиск →]            │
└──────────────────────────────────────┘
```

**Шаг 2 — AMOUNT_INPUT:**
```
┌──────────────────────────────────────┐
│  [←] Куриная грудка           [×]   │
│                                      │
│  Количество (г):                     │
│  ┌─────────────────────────────┐    │
│  │  150                    г   │    │
│  └─────────────────────────────┘    │
│                                      │
│         ┌───────┐                   │
│         │  165  │  ккал             │
│         └───────┘                   │
│    Б 38.0г  Ж 2.0г  У 0.0г         │
│                                      │
│  Приём пищи:                         │
│  [🌅 Завт] [☀️ Обед] [🌙 Ужин] [🍎]  │
│                                      │
│  [   Добавить в дневник   ]          │
└──────────────────────────────────────┘
```

Использует `ModalBottomSheet` из Material3 с `skipPartiallyExpanded = true`.

### 5.4 Изменение HomeScreen

**Файл:** `feature/home/src/main/java/com/ruslan/foodtracker/feature/home/presenter/HomeScreen.kt`

Ключевые изменения:
1. Добавить `QuickAddViewModel` как второй ViewModel
2. Изменить `onAddClick` в `MealsSection` на открытие bottom sheet вместо навигации
3. Добавить `QuickAddBottomSheet` внутри Scaffold
4. Добавить `SnackbarHost` и `LaunchedEffect` для успешного сохранения

```kotlin
@Composable
fun HomeScreen(
    onNavigateToSearch: (mealType: String, date: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    quickAddViewModel: QuickAddViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val quickAddUiState by quickAddViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(quickAddUiState.isSaved) {
        if (quickAddUiState.isSaved) {
            snackbarHostState.showSnackbar("Добавлено в дневник")
            quickAddViewModel.onSavedHandled()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        HomeScreenContent(
            uiState = uiState,
            onMealAddClick = { meal ->
                quickAddViewModel.open(meal.mealType, uiState.selectedDate)
            },
            // ... остальные коллбэки
            modifier = modifier.padding(padding)
        )

        QuickAddBottomSheet(
            uiState = quickAddUiState,
            onFoodSelected = quickAddViewModel::onFoodSelected,
            onRecentEntrySelected = quickAddViewModel::onRecentEntrySelected,
            onAmountChanged = quickAddViewModel::onAmountChanged,
            onMealTypeChanged = quickAddViewModel::onMealTypeChanged,
            onTabSelected = quickAddViewModel::onTabSelected,
            onBackToSelection = quickAddViewModel::onBackToSelection,
            onSave = quickAddViewModel::saveEntry,
            onDismiss = quickAddViewModel::close,
            onNavigateToSearch = {
                quickAddViewModel.close()
                onNavigateToSearch(
                    quickAddUiState.selectedMealType.name,
                    uiState.selectedDate.toString()
                )
            }
        )
    }
}
```

---

## 6. Детальный план реализации (последовательность)

1. **Domain** — добавить `getRecentEntries()` в `FoodEntryRepository`, создать 2 UseCase
2. **Data** — добавить SQL-запрос в `FoodEntryDao`, реализовать в `FoodEntryRepositoryImpl`
3. **ViewModel** — создать `QuickAddUiState`, `QuickAddViewModel` со всей логикой
4. **UI** — создать `QuickAddBottomSheet.kt` с двумя шагами и всеми вспомогательными Composable
5. **Интеграция** — изменить `HomeScreen.kt`, подключить `QuickAddViewModel` и bottom sheet
6. **Тесты** — unit тесты для ViewModel и UseCase, DAO instrumentation тест

---

## 7. Критерии приёмки

### Функциональные
- [ ] Нажатие "+" на `MealCard` открывает bottom sheet (не переходит в `SearchScreen`)
- [ ] Bottom sheet показывает вкладки "Избранное" и "Недавние"
- [ ] Вкладка "Избранное" отображает продукты с `isFavorite == true`
- [ ] Вкладка "Недавние" отображает последние 20 уникальных продуктов
- [ ] Клик по продукту переходит к шагу ввода граммажа
- [ ] Поле граммажа заполнено дефолтным значением (servingSize или последний amountGrams)
- [ ] При вводе граммажа live preview обновляется в реальном времени
- [ ] Можно сменить MealType на шаге ввода
- [ ] Кнопка "Назад" возвращает к списку без потери выбранного MealType
- [ ] Кнопка "Добавить в дневник" сохраняет запись и закрывает bottom sheet
- [ ] После сохранения HomeScreen обновляется (новая запись появляется в MealCard)
- [ ] После сохранения показывается Snackbar "Добавлено в дневник"
- [ ] Кнопка "Открыть полный поиск" закрывает bottom sheet и открывает SearchScreen

### Нефункциональные
- [ ] Открытие bottom sheet < 100мс (данные загружаются фоново)
- [ ] ktlint и detekt без нарушений
- [ ] Все новые Composable имеют `@Preview` аннотации
- [ ] Все новые Composable принимают `Modifier` параметр
- [ ] Код покрыт unit тестами (ViewModel, UseCase)

### Edge Cases
- [ ] Пустое "Избранное" — показывается Empty state с текстом
- [ ] Пустые "Недавние" — показывается Empty state с текстом
- [ ] Ввод 0 или пустое поле — кнопка "Добавить" задизейблена
- [ ] Ошибка сохранения — показывается сообщение, кнопка активна для повтора

---

## 8. Файлы с изменениями

### Новые файлы (4):
| Файл | Слой |
|------|------|
| `domain/.../usecase/entry/GetRecentFoodEntriesUseCase.kt` | Domain |
| `domain/.../usecase/food/GetFavoriteFoodsUseCase.kt` | Domain |
| `feature/home/.../presenter/QuickAddViewModel.kt` | Presentation |
| `feature/home/.../presenter/QuickAddBottomSheet.kt` | Presentation |

### Изменяемые файлы (4):
| Файл | Изменение |
|------|-----------|
| `domain/.../repository/FoodEntryRepository.kt` | +1 метод `getRecentEntries()` |
| `data/.../dao/FoodEntryDao.kt` | +1 SQL запрос `getRecentEntries()` |
| `data/.../repository/FoodEntryRepositoryImpl.kt` | +реализация `getRecentEntries()` |
| `feature/home/.../presenter/HomeScreen.kt` | Интеграция bottom sheet + Snackbar |

---

## 9. Риски

1. **SQL GROUP BY subquery** может быть медленным при большой БД. При необходимости добавить индекс:
   ```kotlin
   @Entity(tableName = "food_entries", indices = [Index(value = ["foodId", "timestamp"])])
   ```
   Потребует Room migration (новая версия БД).

2. **Нормализация нутриентов для "Недавних"** — `FoodEntry` хранит нутриенты за конкретное количество. При обратном расчёте к 100г возможны ошибки округления при малых граммажах.

3. **Два ViewModel на одном экране** — поддерживается Hilt+Compose Navigation, оба привязаны к одному `NavBackStackEntry`.
