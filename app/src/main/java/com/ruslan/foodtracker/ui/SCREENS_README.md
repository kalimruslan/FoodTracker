# Screens - Food Tracker

Все экраны приложения Food Tracker, реализованные на Jetpack Compose с MVVM архитектурой.

## Архитектура экранов

Каждый экран состоит из:
1. **Screen.kt** - Composable функция с UI
2. **ViewModel.kt** - @HiltViewModel с бизнес-логикой и состоянием
3. **UiState** - Data class с состоянием экрана

## Список экранов

### 1. HomeScreen (Главная / Дневник питания)

**Файлы:**
- `home/HomeScreen.kt`
- `home/HomeViewModel.kt`

**Компоненты:**
- Header с gradient (Primary → Primary Dark):
  - Дата "Сегодня" + текущая дата
  - Иконки календаря и уведомлений
  - Выбор дня недели (7 дней горизонтально)
  - Кольцевая диаграмма калорий (130dp)
  - Прогресс-бары макронутриентов (Б/Ж/У/Клетчатка)
- Приёмы пищи:
  - Заголовок секции + кнопка "Добавить приём"
  - MealCard для каждого приёма (Завтрак 🌅, Обед ☀️, Ужин 🌙, Перекус 🍎)
- Трекер воды

**UiState:**
```kotlin
data class HomeUiState(
    val selectedDate: LocalDate,
    val selectedDayIndex: Int,
    val consumedCalories: Float,
    val targetCalories: Float,
    val protein: MacroData,
    val fat: MacroData,
    val carbs: MacroData,
    val fiber: MacroData,
    val meals: List<MealData>,
    val waterGlasses: Int,
    val waterTarget: Int
)
```

**Навигация:**
- Кнопка "+" на MealCard → SearchScreen

---

### 2. SearchScreen (Поиск продуктов)

**Файлы:**
- `search/SearchScreen.kt`
- `search/SearchViewModel.kt`

**Компоненты:**
- Header:
  - Кнопка назад ←
  - Заголовок "Добавить продукт"
- Строка поиска:
  - Иконка 🔍
  - Input field
  - Кнопка сканера 📷 (gradient Primary)
- Табы (горизонтальный скролл):
  - 🔍 Поиск | 🕐 Недавние | ⭐ Избранное | 📋 Рецепты
- Список продуктов (LazyColumn):
  - ProductCard для каждого продукта
  - Поддержка избранного (⭐/☆)

**UiState:**
```kotlin
data class SearchUiState(
    val searchQuery: String,
    val selectedTab: SearchTab,
    val products: List<ProductData>
)

enum class SearchTab { SEARCH, RECENT, FAVORITES, RECIPES }
```

**Навигация:**
- Кнопка назад → navigateUp()
- Клик на ProductCard → ProductDetailScreen(productId)

---

### 3. ProductDetailScreen (Карточка продукта)

**Файлы:**
- `product/ProductDetailScreen.kt`
- `product/ProductDetailViewModel.kt`

**Компоненты:**
- Header с gradient (Primary → Primary Dark):
  - Кнопка назад ←
  - Название продукта
  - Бренд
  - Кольцевая диаграмма калорий (150dp)
- Выбор порции:
  - Input для веса (60dp ширина)
  - Переключатель единиц: г / шт / порц
- Макронутриенты (NutrientGrid):
  - Калории (на всю ширину)
  - Белки, Жиры (в ряд)
  - Углеводы, Клетчатка (в ряд)
- Sticky кнопка "Добавить в Завтрак"

**UiState:**
```kotlin
data class ProductDetailUiState(
    val productId: String,
    val name: String,
    val brand: String,
    val caloriesPer100g: Float,
    val proteinPer100g: Float,
    val fatPer100g: Float,
    val carbsPer100g: Float,
    val fiberPer100g: Float,
    val weight: String,
    val selectedUnit: PortionUnit,
    val multiplier: Float
)
```

**Динамический пересчёт:**
- При изменении веса автоматически пересчитываются все нутриенты
- `calories = caloriesPer100g * multiplier`

**Навигация:**
- Кнопка назад → navigateUp()
- Кнопка "Добавить" → navigateUp() (TODO: добавить в приём пищи)

---

### 4. StatsScreen (Статистика)

**Файлы:**
- `stats/StatsScreen.kt`
- `stats/StatsViewModel.kt`

**Компоненты:**
- Header с gradient (Primary):
  - Заголовок "Статистика"
  - Переключатель периода: Неделя / Месяц / 3 мес (TODO)
  - Столбчатая диаграмма калорий (BarChart):
    - 7 столбцов (Пн-Вс)
    - В норме: белые столбцы
    - Превышение: красные столбцы
- Средние показатели (Grid 2x2):
  - Калории 🔥
  - Белки 🥩
  - Жиры 🥑
  - Углеводы 🍞

**UiState:**
```kotlin
data class StatsUiState(
    val weekCalories: List<BarData>,
    val avgCalories: Float,
    val avgProtein: Float,
    val avgFat: Float,
    val avgCarbs: Float
)
```

---

### 5. ProfileScreen (Профиль)

**Файлы:**
- `profile/ProfileScreen.kt`
- `profile/ProfileViewModel.kt`

**Компоненты:**
- Header с gradient (Primary):
  - Заголовок "Профиль"
  - Аватар (круг 80dp, иконка 👤)
  - Имя пользователя
  - Цель: X ккал/день
- Динамика веса:
  - Столбчатая диаграмма (BarChart)
  - Столбцы по месяцам
  - Итого: "↓ -5 кг за 7 месяцев"
- Напоминания:
  - Завтрак — 08:00
  - Обед — 13:00
  - Ужин — 19:00
  - Toggle для каждого

**UiState:**
```kotlin
data class ProfileUiState(
    val name: String,
    val targetCalories: Int,
    val weight: Float,
    val weightHistory: List<BarData>
)
```

---

## Навигация между экранами

### Граф навигации

```
Home ←→ Bottom Nav ←→ Search ←→ Bottom Nav ←→ Stats ←→ Bottom Nav ←→ Profile
  ↓                      ↓
  ↓                   ProductDetail
  └──────────────────────┘
```

### Bottom Navigation

Показывается на:
- ✅ HomeScreen
- ✅ SearchScreen
- ✅ StatsScreen
- ✅ ProfileScreen

Скрывается на:
- ❌ ProductDetailScreen

---

## Моковые данные

Все экраны используют моковые данные в ViewModels:

### HomeScreen
- Калории: 684 / 2200 ккал
- Белки: 56 / 140 г
- Жиры: 22 / 73 г
- Углеводы: 88 / 275 г
- Клетчатка: 8 / 30 г
- 4 приёма пищи (Завтрак с продуктами, Обед с продуктами, пустые Ужин и Перекус)
- Вода: 4 / 8 стаканов

### SearchScreen
- 6 продуктов (Куриная грудка, Рис, Гречка, Творог, Яйцо, Овсянка)
- Творог в избранном

### ProductDetailScreen
- Продукт: Куриная грудка (110 ккал, 23.1г Б, 1.2г Ж, 0г У)
- Вес по умолчанию: 100г

### StatsScreen
- Калории за неделю: [1850, 2100, 1950, 2250, 1780, 2050, 1680]
- Средние: 1951 ккал, 98г Б, 62г Ж, 215г У

### ProfileScreen
- Имя: "Пользователь"
- Цель: 2200 ккал/день
- Вес: 130 кг
- История: [135, 133.5, 132, 131.5, 130.8, 130.2, 130] (Янв-Июл)

---

## TODO: Интеграция с Use Cases

После создания Use Cases в domain слое:

### HomeViewModel
```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getEntriesByDateUseCase: GetEntriesByDateUseCase,
    private val getDailyTotalsUseCase: GetDailyTotalsUseCase,
    private val getTargetCaloriesUseCase: GetTargetCaloriesUseCase
) : ViewModel()
```

### SearchViewModel
```kotlin
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchProductsUseCase: SearchProductsUseCase,
    private val getRecentProductsUseCase: GetRecentProductsUseCase,
    private val getFavoriteProductsUseCase: GetFavoriteProductsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel()
```

### ProductDetailViewModel
```kotlin
@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val getProductByIdUseCase: GetProductByIdUseCase,
    private val addFoodEntryUseCase: AddFoodEntryUseCase
) : ViewModel()
```

### StatsViewModel
```kotlin
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val getStatsForPeriodUseCase: GetStatsForPeriodUseCase,
    private val calculateAveragesUseCase: CalculateAveragesUseCase
) : ViewModel()
```

### ProfileViewModel
```kotlin
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getWeightHistoryUseCase: GetWeightHistoryUseCase,
    private val calculateBMRUseCase: CalculateBMRUseCase
) : ViewModel()
```

---

## Файлы

**Созданные файлы (10):**

### Home
1. `app/ui/home/HomeScreen.kt` - 300+ строк
2. `app/ui/home/HomeViewModel.kt` - 100+ строк

### Search
3. `app/ui/search/SearchScreen.kt` - 200+ строк
4. `app/ui/search/SearchViewModel.kt` - 70+ строк

### Product Detail
5. `app/ui/product/ProductDetailScreen.kt` - 200+ строк
6. `app/ui/product/ProductDetailViewModel.kt` - 80+ строк

### Stats
7. `app/ui/stats/StatsScreen.kt` - 120+ строк
8. `app/ui/stats/StatsViewModel.kt` - 50+ строк

### Profile
9. `app/ui/profile/ProfileScreen.kt` - 120+ строк
10. `app/ui/profile/ProfileViewModel.kt` - 50+ строк

**Обновлённые файлы (1):**
- `app/ui/navigation/FoodNavHost.kt` - интеграция всех экранов

**Итого:** ~1400+ строк кода для UI экранов

---

## Тестирование

**Компиляция:**
- ✅ `./gradlew assembleDebug` - BUILD SUCCESSFUL
- ✅ Все экраны скомпилированы без ошибок

**Проверка в приложении:**
1. Запустите приложение
2. Главная страница отображается с моковыми данными
3. Переключайтесь между табами (Главная, Поиск, Статистика, Профиль)
4. На экране Поиск нажмите на продукт → открывается ProductDetailScreen
5. Bottom Navigation скрывается на ProductDetailScreen
6. Кнопка "Назад" возвращает на SearchScreen

---

## Следующие шаги

1. **Создать Use Cases в domain слое**
2. **Интегрировать ViewModels с Use Cases**
3. **Добавить обработку Loading/Error состояний**
4. **Реализовать функциональность:**
   - Добавление продукта в приём пищи
   - Сохранение избранного
   - Поиск продуктов (локально и API)
   - Сканер штрихкодов
   - Напоминания
5. **Добавить анимации переходов**
6. **Покрыть тестами**
