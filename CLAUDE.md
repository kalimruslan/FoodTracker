# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

FoodTracker - Android приложение на Kotlin с модульной Clean Architecture для отслеживания питания.

**Package:** `com.ruslan.foodtracker`
**Min SDK:** 26
**Target SDK:** 36
**Kotlin Version:** 2.1.0
**AGP Version:** 8.7.3
**Gradle Version:** 9.1.0

## Build System

Проект использует Gradle 9.1.0 с Kotlin DSL. Все зависимости управляются через `gradle/libs.versions.toml` (Version Catalog).

### Основные команды сборки

```bash
# Сборка проекта
./gradlew build

# Очистка build директории
./gradlew clean

# Сборка debug APK
./gradlew assembleDebug

# Сборка release APK
./gradlew assembleRelease

# Установка debug версии на подключенное устройство/эмулятор
./gradlew installDebug
```

## Testing

### Unit тесты
```bash
# Запуск всех unit тестов
./gradlew test

# Запуск unit тестов для debug варианта
./gradlew testDebugUnitTest
```

### Instrumentation тесты
```bash
# Запуск всех инструментальных тестов на подключенном устройстве
./gradlew connectedDebugAndroidTest

# Запуск всех инструментальных тестов (все способы)
./gradlew connectedAndroidTest
```

## Code Quality

```bash
# Lint проверка
./gradlew lint

# Lint с автоматическим исправлением
./gradlew lintFix

# Проверка необходимости Jetifier
./gradlew checkJetifier
```

## Architecture

Проект использует **Clean Architecture** с модульной структурой, разделением на слои Domain/Data/Presentation и паттерном Repository.

### Принципы архитектуры

1. **Разделение ответственности** - каждый модуль имеет четкую роль
2. **Независимость от фреймворков** - domain слой не зависит от Android
3. **Тестируемость** - все слои легко покрываются тестами
4. **Dependency Rule** - зависимости направлены только внутрь (к domain)

### Модульная структура

```
FoodTracker/
├── app/                    # Главный модуль приложения (Android Application)
├── domain/                 # Бизнес-логика (Pure Kotlin/Java Library)
├── data/                   # Источники данных (Android Library)
├── core:common/            # Общие утилиты (Pure Kotlin/Java Library)
└── core:ui/                # Общие UI компоненты (Android Library)
```

### Зависимости между модулями

```
app → domain, data, core:common, core:ui
data → domain, core:common (+ Room, Hilt)
core:ui → domain, core:common (+ Compose)
domain → (никаких зависимостей)
core:common → (никаких зависимостей)
```

## Module Details

### app - Главный модуль приложения

**Тип:** Android Application
**Namespace:** `com.ruslan.foodtracker`
**Технологии:** Hilt, Navigation Compose, Kotlin Serialization

**Структура:**
```
app/src/main/java/com/ruslan/foodtracker/
├── FoodTrackerApp.kt        # Application class с @HiltAndroidApp
├── MainActivity.kt          # Единственная Activity с @AndroidEntryPoint
└── ui/
    └── navigation/          # Navigation setup
        ├── NavRoutes.kt     # Type-safe routes (Kotlin Serialization)
        └── FoodNavHost.kt   # NavHost композабл
```

**Ответственность:**
- DI setup (Hilt)
- Navigation setup
- Activity/Fragment (только MainActivity)
- Интеграция всех модулей

### domain - Бизнес-логика

**Тип:** Pure Kotlin (Java Library)
**Namespace:** `com.ruslan.foodtracker.domain`
**Технологии:** Coroutines, javax.inject

**Структура:**
```
domain/src/main/java/com/ruslan/foodtracker/domain/
├── model/                   # Domain модели
│   ├── Food.kt
│   ├── FoodEntry.kt
│   ├── MealType.kt
│   └── NetworkResult.kt     # Sealed class для результатов операций
├── repository/              # Интерфейсы репозиториев
│   ├── FoodRepository.kt
│   └── FoodEntryRepository.kt
├── usecase/                 # Use cases (бизнес-логика)
│   ├── food/
│   │   └── GetAllFoodsUseCase.kt
│   └── entry/
│       └── GetEntriesByDateUseCase.kt
└── util/                    # Утилиты для domain
    └── NetworkUtils.kt
```

**Ответственность:**
- Domain модели (чистые data классы)
- Интерфейсы репозиториев
- Use Cases (бизнес-логика)
- NetworkResult для обработки результатов

**Правила:**
- НЕТ зависимостей на Android SDK
- НЕТ зависимостей на другие модули
- Только чистый Kotlin + Coroutines
- Все публичные методы репозиториев возвращают `NetworkResult<T>` или `Flow<NetworkResult<T>>`

### data - Источники данных

**Тип:** Android Library
**Namespace:** `com.ruslan.foodtracker.data`
**Технологии:** Room, Hilt, Coroutines

**Структура:**
```
data/src/main/java/com/ruslan/foodtracker/data/
├── local/
│   ├── entity/              # Room entities
│   │   ├── FoodEntity.kt
│   │   └── FoodEntryEntity.kt
│   ├── dao/                 # DAO интерфейсы
│   │   ├── FoodDao.kt
│   │   └── FoodEntryDao.kt
│   ├── converter/
│   │   └── Converters.kt    # TypeConverters для Room
│   └── FoodTrackerDatabase.kt
├── repository/              # Реализации репозиториев
│   ├── FoodRepositoryImpl.kt
│   └── FoodEntryRepositoryImpl.kt
├── mapper/
│   └── Mappers.kt           # Entity ↔ Domain маппинг
└── di/                      # Hilt модули
    ├── DatabaseModule.kt
    └── RepositoryModule.kt
```

**Ответственность:**
- Реализация репозиториев из domain
- Room Database (DAO, Entities, TypeConverters)
- Маппинг Entity ↔ Domain
- DI модули для Database и Repository

**Правила:**
- Реализует интерфейсы из domain модуля
- Использует NetworkResult для обработки ошибок
- Все suspend функции оборачивают try-catch
- Flow методы используют .onStart {} .catch {}

### core:common - Общие утилиты

**Тип:** Pure Kotlin (Java Library)
**Namespace:** `com.ruslan.foodtracker.core.common`

**Структура:**
```
core/common/src/main/java/com/ruslan/foodtracker/core/common/
└── util/
    ├── Constants.kt         # Константы
    └── DateTimeUtils.kt     # Утилиты для работы с датами
```

**Ответственность:**
- Константы приложения
- Утилиты общего назначения
- Расширения для стандартных типов

**Правила:**
- НЕТ зависимостей на Android SDK
- НЕТ зависимостей на другие модули
- Только чистый Kotlin

### core:ui - Общие UI компоненты

**Тип:** Android Library
**Namespace:** `com.ruslan.foodtracker.core.ui`
**Технологии:** Compose, Material3

**Структура:**
```
core/ui/src/main/java/com/ruslan/foodtracker/core/ui/
├── theme/                   # Тема приложения
│   ├── Color.kt
│   ├── Theme.kt
│   └── Type.kt
├── components/              # Переиспользуемые UI компоненты
└── state/                   # UI State классы
    └── UiState.kt
```

**Ответственность:**
- Тема приложения (FoodTrackerTheme)
- Общие Composable компоненты
- UI State классы
- Extension функции для Compose

**Правила:**
- Все компоненты должны быть Composable функциями
- Все компоненты принимают `Modifier` параметр
- Используй `@Preview` для всех компонентов
- Зависит от domain для моделей данных

## Key Technologies

### Dependency Injection - Hilt

```kotlin
// Application class
@HiltAndroidApp
class FoodTrackerApp : Application()

// ViewModel
@HiltViewModel
class FoodListViewModel @Inject constructor(
    private val getAllFoodsUseCase: GetAllFoodsUseCase
) : ViewModel()

// Repository injection
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindFoodRepository(
        impl: FoodRepositoryImpl
    ): FoodRepository
}
```

### Database - Room

```kotlin
@Database(
    entities = [FoodEntity::class, FoodEntryEntity::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class FoodTrackerDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun foodEntryDao(): FoodEntryDao
}
```

**База данных:** `food_tracker_database`
**Таблицы:** `foods`, `food_entries`

### Navigation - Navigation Compose

Type-safe navigation с Kotlin Serialization:

```kotlin
sealed interface NavRoutes {
    @Serializable
    data object Home : NavRoutes

    @Serializable
    data class EditFood(val foodId: Long) : NavRoutes
}
```

### NetworkResult Pattern

Все асинхронные операции используют `NetworkResult<T>`:

```kotlin
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(message, code?, exception?) : NetworkResult<Nothing>()
    data object Loading : NetworkResult<Nothing>()
    data object Empty : NetworkResult<Nothing>()
}
```

**Использование в Repository:**
```kotlin
override fun getAllFoods(): Flow<NetworkResult<List<Food>>> =
    foodDao.getAllFoods()
        .map { entities -> NetworkResult.Success(entities.map { it.toDomain() }) }
        .onStart { emit(NetworkResult.Loading) }
        .catch { e -> emit(NetworkResult.Error(e.message ?: "Error")) }
```

**Использование в ViewModel:**
```kotlin
init {
    getAllFoodsUseCase()
        .collect { result ->
            _uiState.value = result.toUiState()
        }
}
```

**Подробнее:** См. `NETWORK_RESULT_EXAMPLE.md`

## Development Guidelines

### Clean Architecture Rules

1. **Domain Layer**
   - НЕТ зависимостей на Android
   - НЕТ зависимостей на другие модули
   - Только интерфейсы репозиториев, модели и use cases

2. **Data Layer**
   - Реализует интерфейсы из domain
   - Содержит логику работы с данными (Room, Network)
   - Использует маппинг Entity ↔ Domain

3. **Presentation Layer (app)**
   - Зависит от domain через DI
   - Использует UiState для UI состояний
   - ViewModel → UseCase → Repository

### Repository Pattern

```kotlin
// 1. Интерфейс в domain
interface FoodRepository {
    fun getAllFoods(): Flow<NetworkResult<List<Food>>>
    suspend fun insertFood(food: Food): NetworkResult<Long>
}

// 2. Реализация в data
class FoodRepositoryImpl @Inject constructor(
    private val foodDao: FoodDao
) : FoodRepository {
    override fun getAllFoods() = foodDao.getAllFoods()
        .map { NetworkResult.Success(it.map { it.toDomain() }) }
        .catch { emit(NetworkResult.Error(it.message ?: "Error")) }
}

// 3. DI binding
@Binds
abstract fun bindFoodRepository(impl: FoodRepositoryImpl): FoodRepository
```

### Use Case Pattern

```kotlin
class GetAllFoodsUseCase @Inject constructor(
    private val repository: FoodRepository
) {
    operator fun invoke(): Flow<NetworkResult<List<Food>>> =
        repository.getAllFoods()
}
```

**Правила:**
- Один Use Case = одна бизнес-операция
- `operator fun invoke()` для вызова
- Минимальная логика, в основном делегирование

### ViewModel Pattern

```kotlin
@HiltViewModel
class FoodListViewModel @Inject constructor(
    private val getAllFoodsUseCase: GetAllFoodsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Food>>>(UiState.Idle)
    val uiState: StateFlow<UiState<List<Food>>> = _uiState.asStateFlow()

    init {
        loadFoods()
    }

    private fun loadFoods() {
        viewModelScope.launch {
            getAllFoodsUseCase()
                .collect { result ->
                    _uiState.value = result.toUiState()
                }
        }
    }
}
```

### Compose Best Practices

- Используй `@Preview` аннотацию для всех Composable функций
- Все Composable функции должны принимать `Modifier` параметр
- Используй `FoodTrackerTheme` для оборачивания UI компонентов
- State hoisting - состояние в ViewModel, UI только отображает
- Используй `hiltViewModel()` для получения ViewModel

```kotlin
@Composable
fun FoodListScreen(
    viewModel: FoodListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is UiState.Loading -> LoadingIndicator()
        is UiState.Success -> FoodList((uiState as UiState.Success).data)
        is UiState.Error -> ErrorMessage((uiState as UiState.Error).message)
    }
}
```

### Error Handling

1. **В Repository** - всегда оборачивать в NetworkResult
2. **В ViewModel** - конвертировать в UiState
3. **В Composable** - отображать через when(uiState)

### Naming Conventions

- **Entities:** `FoodEntity`, `FoodEntryEntity` (data слой)
- **Models:** `Food`, `FoodEntry` (domain слой)
- **Repositories:** `FoodRepository` (interface), `FoodRepositoryImpl` (implementation)
- **Use Cases:** `GetAllFoodsUseCase`, `InsertFoodUseCase`
- **ViewModels:** `FoodListViewModel`, `AddFoodViewModel`
- **Screens:** `FoodListScreen`, `AddFoodScreen`

### Configuration

- **Java compatibility:** Version 17
- **Kotlin code style:** Official
- **AndroidX:** Включен
- **Compose:** Включен через kotlin.compose plugin
- **ProGuard:** Настроен для release сборок (minify отключен)

## Adding New Features

При добавлении новой функциональности:

1. **Domain Layer**
   - Создай domain модель
   - Создай интерфейс репозитория
   - Создай use case(s)

2. **Data Layer**
   - Создай entity для Room
   - Создай DAO
   - Добавь в Database
   - Создай mapper
   - Реализуй repository
   - Добавь в DI

3. **Presentation Layer**
   - Создай ViewModel
   - Создай Composable screen
   - Добавь в navigation

4. **Testing**
   - Unit тесты для use cases
   - Unit тесты для repository
   - Integration тесты для database
   - UI тесты для screens

## Common Issues

### Lint ошибки при сборке
Используй `./gradlew assembleDebug` вместо `./gradlew build` чтобы пропустить lint проверки.

### Hilt ошибки компиляции
Убедись что:
- Application class аннотирован `@HiltAndroidApp`
- Activity аннотирована `@AndroidEntryPoint`
- AndroidManifest.xml содержит `android:name=".FoodTrackerApp"`

### Room схема не обновляется
```bash
./gradlew clean
./gradlew assembleDebug
```

## Resources

- [NetworkResult Guide](NETWORK_RESULT_EXAMPLE.md) - Подробное руководство по NetworkResult
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Hilt Documentation](https://developer.android.com/training/dependency-injection/hilt-android)
- [Room Documentation](https://developer.android.com/training/data-storage/room)