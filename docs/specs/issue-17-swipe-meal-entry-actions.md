# Spec: Свайп-жест на элементах продуктов в MealCard (Issue #17)

## Цель

Добавить свайп влево на элементах продуктов внутри `MealCard` на главном экране.
При свайпе появляются две иконки действий: **Удалить (🗑️)** и **Редактировать (✏️)**.

## Acceptance Criteria (из issue)

- [ ] При свайпе влево на элементе продукта появляются 2 иконки: 🗑️ Удалить и ✏️ Редактировать
- [ ] Удалить — продукт удаляется из списка + Snackbar с кнопкой "Отменить"
- [ ] Редактировать — открывается bottom sheet для изменения граммовки
- [ ] После изменения граммовки калории и макросы пересчитываются автоматически
- [ ] Свайп работает корректно в LazyColumn
- [ ] Анимация плавная

---

## Архитектурные решения

### 1. Расширение `FoodItemData` (core:ui)

`FoodItemData` нужно расширить полем `entryId: Long = 0L`, чтобы при свайпе
знать, какую запись удалить/редактировать.

```kotlin
// core/ui/src/main/.../components/MealCard.kt
data class FoodItemData(
    val name: String,
    val weight: String,
    val calories: Int,
    val entryId: Long = 0L       // ← добавить
)
```

### 2. Изменение `MealCard` — поддержка свайпа (core:ui)

Добавить необязательные колбэки и завернуть каждый `FoodItemRow` в `SwipeToDismissBox`.

```kotlin
@Composable
fun MealCard(
    // ... существующие параметры ...
    onDeleteItem: ((Long) -> Unit)? = null,   // ← добавить
    onEditItem: ((Long) -> Unit)? = null,     // ← добавить
    modifier: Modifier = Modifier
)
```

**Реализация свайпа внутри `MealCard`:**

```kotlin
foodItems.forEach { item ->
    if (onDeleteItem != null || onEditItem != null) {
        SwipeableFoodItemRow(
            item = item,
            onDelete = { onDeleteItem?.invoke(item.entryId) },
            onEdit = { onEditItem?.invoke(item.entryId) }
        )
    } else {
        FoodItemRow(item.name, item.weight, item.calories)
    }
}
```

**`SwipeableFoodItemRow`** — приватный composable:
- `SwipeToDismissBox` с `startToEnd = false`, `endToStart = true`
- `backgroundContent`: красный фон (🗑️) слева и синий/primary (✏️) правее
- При `DismissValue.DismissedToStart` → вызвать `onDelete` И сбросить состояние (без dismiss)
- Кнопка Edit — `IconButton` поверх фона справа

**Важно**: свайп не должен физически "убирать" строку (это задача ViewModel).
Используем `confirmValueChange` для перехвата и вызова колбэка:

```kotlin
val dismissState = rememberSwipeToDismissBoxState(
    confirmValueChange = { value ->
        if (value == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
            false  // не убирать строку — это сделает ViewModel после удаления из БД
        } else false
    }
)
```

Edit-кнопка — отдельный `IconButton` в `backgroundContent`, не через dismiss.

### 3. Изменение `HomeViewModel` (feature:home)

**Добавить зависимости:**
```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getEntriesByDateUseCase: GetEntriesByDateUseCase,
    private val deleteFoodEntryUseCase: DeleteFoodEntryUseCase,
    private val insertFoodEntryUseCase: InsertFoodEntryUseCase,   // ← для undo
    private val updateFoodEntryUseCase: UpdateFoodEntryUseCase    // ← для edit
) : ViewModel()
```

**Добавить в `HomeUiState`:**
```kotlin
data class HomeUiState(
    // ... существующие поля ...
    val editingEntry: FoodEntry? = null,          // запись на редактирование
    val pendingDeleteEntry: FoodEntry? = null,    // для Undo
    val showDeleteSnackbar: Boolean = false,
)
```

**Добавить вспомогательное состояние (не в UiState — приватно в VM):**
```kotlin
private var allEntries: List<FoodEntry> = emptyList()
```
Обновляется при каждой загрузке данных, используется для поиска по `entryId`.

**Новые методы:**

```kotlin
fun onDeleteEntry(entryId: Long) {
    val entry = allEntries.find { it.id == entryId } ?: return
    viewModelScope.launch {
        deleteFoodEntryUseCase(entry).doActionIfSuccess {
            _uiState.value = _uiState.value.copy(
                pendingDeleteEntry = entry,
                showDeleteSnackbar = true
            )
            loadEntriesForSelectedDate()
        }
    }
}

fun onUndoDelete() {
    val entry = _uiState.value.pendingDeleteEntry ?: return
    viewModelScope.launch {
        // Восстановить с id=0 чтобы Room создал новую запись
        insertFoodEntryUseCase(entry.copy(id = 0L)).doActionIfSuccess {
            _uiState.value = _uiState.value.copy(
                pendingDeleteEntry = null,
                showDeleteSnackbar = false
            )
            loadEntriesForSelectedDate()
        }
    }
}

fun onDeleteSnackbarDismissed() {
    _uiState.value = _uiState.value.copy(
        pendingDeleteEntry = null,
        showDeleteSnackbar = false
    )
}

fun onEditEntry(entryId: Long) {
    val entry = allEntries.find { it.id == entryId } ?: return
    _uiState.value = _uiState.value.copy(editingEntry = entry)
}

fun onEditDismiss() {
    _uiState.value = _uiState.value.copy(editingEntry = null)
}

fun onUpdateEntryAmount(entry: FoodEntry, newAmountGrams: Double) {
    if (newAmountGrams <= 0) return
    val ratio = newAmountGrams / entry.amountGrams
    val updated = entry.copy(
        amountGrams = newAmountGrams,
        calories = (entry.calories * ratio).roundToInt(),
        protein = entry.protein * ratio,
        carbs = entry.carbs * ratio,
        fat = entry.fat * ratio
    )
    viewModelScope.launch {
        updateFoodEntryUseCase(updated).doActionIfSuccess {
            _uiState.value = _uiState.value.copy(editingEntry = null)
            loadEntriesForSelectedDate()
        }
    }
}
```

**Обновить `createMealData`** — передавать `entryId`:
```kotlin
val foodItems = entries.map { entry ->
    FoodItemData(
        name = entry.foodName,
        weight = "${entry.amountGrams.toInt()}г",
        calories = entry.calories,
        entryId = entry.id   // ← добавить
    )
}
```

### 4. Новый компонент `EditEntryBottomSheet` (feature:home)

**Файл:** `feature/home/src/main/java/com/ruslan/foodtracker/feature/home/presenter/EditEntryBottomSheet.kt`

Аналог `AmountInputStep` из `QuickAddBottomSheet`:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntryBottomSheet(
    entry: FoodEntry,
    onSave: (newAmountGrams: Double) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
)
```

**UI структура:**
- Заголовок: "Редактировать граммовку" + кнопка закрытия (X)
- Название продукта (bold)
- `OutlinedTextField` с предзаполненным `entry.amountGrams.toInt()` (суффикс "г")
- Клавиатура: `KeyboardType.Number`
- Live preview: пересчёт калорий на базе коэффициентов:
  ```
  newCalories = round((entry.calories / entry.amountGrams) * newGrams)
  ```
- Строка макросов: Б / Ж / У
- Кнопка "Сохранить" (активна если `newGrams > 0`)

**Состояние:** локальный `var amountText by remember { mutableStateOf(entry.amountGrams.toInt().toString()) }`

### 5. Изменение `HomeScreen` (feature:home)

**Передача колбэков в MealsSection → MealCard:**
```kotlin
MealsSection(
    meals = uiState.meals,
    onAddClick = onMealAddClick,
    onDeleteItem = viewModel::onDeleteEntry,
    onEditItem = viewModel::onEditEntry,
)
```

**Snackbar + Undo:**
```kotlin
LaunchedEffect(uiState.showDeleteSnackbar) {
    if (uiState.showDeleteSnackbar) {
        val result = snackbarHostState.showSnackbar(
            message = "Продукт удалён",
            actionLabel = "Отменить",
            duration = SnackbarDuration.Short
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.onUndoDelete()
        } else {
            viewModel.onDeleteSnackbarDismissed()
        }
    }
}
```

**EditEntryBottomSheet:**
```kotlin
uiState.editingEntry?.let { entry ->
    EditEntryBottomSheet(
        entry = entry,
        onSave = { newGrams -> viewModel.onUpdateEntryAmount(entry, newGrams) },
        onDismiss = viewModel::onEditDismiss
    )
}
```

---

## Файлы для изменения/создания

| Файл | Действие | Слой |
|------|----------|------|
| `core/ui/.../components/MealCard.kt` | Изменить | core:ui |
| `feature/home/.../HomeViewModel.kt` | Изменить | feature:home |
| `feature/home/.../HomeScreen.kt` | Изменить | feature:home |
| `feature/home/.../EditEntryBottomSheet.kt` | Создать | feature:home |

---

## Тесты (Unit)

**Файл:** `feature/home/src/test/.../HomeViewModelTest.kt`

| Тест | Описание |
|------|----------|
| `onDeleteEntry_callsDeleteUseCase` | Проверяет вызов `DeleteFoodEntryUseCase` с правильной записью |
| `onDeleteEntry_updatesShowSnackbarState` | После успешного удаления `showDeleteSnackbar = true` |
| `onUndoDelete_restoresEntry` | `InsertFoodEntryUseCase` вызывается с `entry.copy(id=0)` |
| `onUpdateEntryAmount_recalculatesMacros` | Проверяет правильный пересчёт калорий через ratio |
| `onUpdateEntryAmount_callsUpdateUseCase` | `UpdateFoodEntryUseCase` вызывается с правильными данными |

---

## Технические ограничения

- `SwipeToDismissBox` — из `Material3` (уже в проекте)
- `confirmValueChange = { false }` для свайпа — строка остаётся, удаление через ViewModel
- Кнопка Edit в `backgroundContent` — рядом с иконкой удаления, но отдельный `IconButton`
- Анимация: стандартная Material3 (нет кастомной необходимости)

## Out of Scope

- Свайп вправо
- Диалог подтверждения удаления
- Поиск `Food` по `foodId` для точного пересчёта (используем ratio от существующей записи)