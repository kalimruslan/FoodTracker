# NetworkResult - Руководство по использованию

## Обзор

`NetworkResult` - это sealed class для обработки результатов асинхронных операций (сеть, база данных) с автоматической обработкой состояний Loading/Success/Error.

## Структура

```kotlin
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(
        val message: String,
        val code: Int? = null,
        val exception: Throwable? = null
    ) : NetworkResult<Nothing>()
    data object Loading : NetworkResult<Nothing>()
    data object Empty : NetworkResult<Nothing>()
}
```

## Использование в Repository

### Пример: FoodRepositoryImpl

```kotlin
override fun getAllFoods(): Flow<NetworkResult<List<Food>>> =
    foodDao.getAllFoods()
        .map { entities ->
            NetworkResult.Success(entities.map { it.toDomain() })
        }
        .onStart { emit(NetworkResult.Loading) }
        .catch { e ->
            emit(NetworkResult.Error(
                message = e.message ?: "Ошибка при загрузке продуктов",
                exception = e
            ))
        }

override suspend fun insertFood(food: Food): NetworkResult<Long> = try {
    val id = foodDao.insertFood(food.toEntity())
    NetworkResult.Success(id)
} catch (e: Exception) {
    NetworkResult.Error(
        message = e.message ?: "Ошибка при добавлении продукта",
        exception = e
    )
}
```

## Использование в ViewModel

### Пример: Загрузка списка продуктов

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

### Пример: Добавление продукта

```kotlin
fun addFood(food: Food) {
    viewModelScope.launch {
        when (val result = foodRepository.insertFood(food)) {
            is NetworkResult.Success -> {
                // Продукт успешно добавлен
                _snackbarMessage.value = "Продукт добавлен"
            }
            is NetworkResult.Error -> {
                // Обработка ошибки
                _errorMessage.value = result.message
            }
            is NetworkResult.Loading -> {
                // Показать индикатор загрузки
            }
            is NetworkResult.Empty -> {
                // Пустой результат
            }
        }
    }
}
```

## Использование в Composable

### Пример: Отображение списка с обработкой состояний

```kotlin
@Composable
fun FoodListScreen(
    viewModel: FoodListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is UiState.Loading -> {
            LoadingIndicator()
        }
        is UiState.Success -> {
            val foods = (uiState as UiState.Success<List<Food>>).data
            LazyColumn {
                items(foods) { food ->
                    FoodItem(food = food)
                }
            }
        }
        is UiState.Error -> {
            ErrorMessage(
                message = (uiState as UiState.Error).message,
                onRetry = { viewModel.loadFoods() }
            )
        }
        is UiState.Empty -> {
            EmptyState(message = "Нет продуктов")
        }
        is UiState.Idle -> {
            // Начальное состояние
        }
    }
}
```

## Extension функции

### onSuccess, onError, onLoading

```kotlin
viewModelScope.launch {
    foodRepository.insertFood(food)
        .onSuccess { id ->
            Log.d("Food", "Inserted with id: $id")
        }
        .onError { message, code, exception ->
            Log.e("Food", "Error: $message, code: $code", exception)
        }
        .onLoading {
            Log.d("Food", "Loading...")
        }
}
```

### map - трансформация данных

```kotlin
val foodNames: Flow<NetworkResult<List<String>>> =
    getAllFoodsUseCase()
        .map { result ->
            result.map { foods ->
                foods.map { it.name }
            }
        }
```

### getOrNull - получение данных или null

```kotlin
val result = foodRepository.getFoodById(1)
val food: Food? = result.getOrNull()
```

## Утилиты для работы с NetworkResult

### safeApiCall - оборачивает синхронный вызов

```kotlin
val result: NetworkResult<String> = safeApiCall {
    // Любой код, который может выбросить исключение
    someApiCall()
}
```

### safeSuspendApiCall - оборачивает suspend вызов

```kotlin
val result: NetworkResult<User> = safeSuspendApiCall {
    api.getUser(userId)
}
```

### asNetworkResult - конвертация Flow в Flow<NetworkResult>

```kotlin
fun getUsers(): Flow<NetworkResult<List<User>>> {
    return userDao.getAllUsers()
        .map { it.toDomain() }
        .asNetworkResult()
}
```

## Интеграция с UiState

`UiState` - это UI-слой обертка над `NetworkResult` для использования в Composable:

```kotlin
// Конвертация NetworkResult в UiState
val uiState: UiState<List<Food>> = networkResult.toUiState()
```

## Best Practices

1. **Repository всегда возвращает NetworkResult** - не бросайте исключения наружу
2. **UseCase пробрасывает NetworkResult** - минимальная логика
3. **ViewModel конвертирует в UiState** - для удобства UI
4. **Composable работает с UiState** - простая обработка состояний

## Примеры из проекта

Смотрите реализацию:
- `domain/model/NetworkResult.kt` - основной класс
- `domain/util/NetworkUtils.kt` - утилиты
- `data/repository/FoodRepositoryImpl.kt` - пример использования в репозитории
- `core/ui/state/UiState.kt` - UI State wrapper
