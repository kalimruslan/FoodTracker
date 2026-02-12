# Navigation Structure - Food Tracker

## Архитектура навигации

Food Tracker использует **Type-Safe Navigation** с Kotlin Serialization для навигации между экранами.

### Основные компоненты

1. **NavRoutes.kt** - Определяет все маршруты приложения
2. **BottomNavBar.kt** - Bottom Navigation Bar с 4 табами
3. **FoodNavHost.kt** - Главный навигационный граф

---

## Маршруты (NavRoutes)

### Основные экраны (с Bottom Navigation)

| Маршрут | Описание | Bottom Nav |
|---------|----------|------------|
| `Home` | Главная (Дневник питания) | ✅ Показывается |
| `Search` | Поиск продуктов | ✅ Показывается |
| `Stats` | Статистика за период | ✅ Показывается |
| `Profile` | Профиль пользователя | ✅ Показывается |

### Детальные экраны (без Bottom Navigation)

| Маршрут | Параметры | Bottom Nav |
|---------|-----------|------------|
| `ProductDetail` | `productId: String` | ❌ Скрывается |

---

## Bottom Navigation Bar

### Табы

1. **🏠 Главная** (Home)
   - Иконка: `Icons.Filled.Home`
   - Label: "Главная"
   - Цвет активного: Primary (#FF6B35)

2. **🔍 Поиск** (Search)
   - Иконка: `Icons.Filled.Search`
   - Label: "Поиск"
   - Цвет активного: Primary (#FF6B35)

3. **📊 Статистика** (Stats)
   - Иконка: `Icons.Filled.BarChart`
   - Label: "Статистика"
   - Цвет активного: Primary (#FF6B35)

4. **👤 Профиль** (Profile)
   - Иконка: `Icons.Filled.Person`
   - Label: "Профиль"
   - Цвет активного: Primary (#FF6B35)

### Особенности

- **Активный таб:** цвет Primary, жирный текст (Bold), точка-индикатор снизу
- **Неактивный таб:** серый цвет, пониженная непрозрачность (60%), обычный текст
- **Индикатор:** маленькая точка (4dp) Primary цвета под активной иконкой
- **Фон:** белый (light) / тёмный (dark), верхняя граница

---

## Навигация между экранами

### Home → Search
```kotlin
navController.navigate(NavRoutes.Search)
```

### Search → ProductDetail
```kotlin
navController.navigate(NavRoutes.ProductDetail(productId = "barcode-123"))
```

### ProductDetail → Back
```kotlin
navController.navigateUp()
```

### Bottom Nav Item → Any Screen
```kotlin
navController.navigate(route) {
    popUpTo(NavRoutes.Home) {
        saveState = true
    }
    launchSingleTop = true
    restoreState = true
}
```

---

## Функция shouldShowBottomBar()

Определяет, когда показывать Bottom Navigation Bar:

```kotlin
fun shouldShowBottomBar(currentRoute: NavRoutes?): Boolean {
    return when (currentRoute) {
        is NavRoutes.Home,
        is NavRoutes.Search,
        is NavRoutes.Stats,
        is NavRoutes.Profile -> true  // Показываем на основных экранах

        is NavRoutes.ProductDetail -> false  // Скрываем на детальной странице

        null -> true
    }
}
```

---

## FoodNavHost структура

```kotlin
@Composable
fun FoodNavHost(navController: NavHostController) {
    val currentRoute = /* текущий маршрут */

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar(currentRoute)) {
                FoodTrackerBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { navController.navigate(it) }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.Home,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<NavRoutes.Home> { /* HomeScreen */ }
            composable<NavRoutes.Search> { /* SearchScreen */ }
            composable<NavRoutes.ProductDetail> { /* ProductDetailScreen */ }
            composable<NavRoutes.Stats> { /* StatsScreen */ }
            composable<NavRoutes.Profile> { /* ProfileScreen */ }
        }
    }
}
```

---

## Placeholder экраны

На данный момент все экраны реализованы как placeholder'ы (заглушки) с демонстрацией:
- Названия экрана
- Описания того, что будет на экране
- Кнопка для демонстрации навигации (где применимо)

### Текущие placeholder'ы:
- ✅ `HomeScreenPlaceholder` - с кнопкой "Добавить продукт" → переход на Search
- ✅ `SearchScreenPlaceholder` - с кнопкой "Открыть продукт" → переход на ProductDetail
- ✅ `ProductDetailScreenPlaceholder` - с кнопкой "Назад" → navigateUp()
- ✅ `StatsScreenPlaceholder` - без кнопок
- ✅ `ProfileScreenPlaceholder` - без кнопок

---

## Тестирование навигации

1. **Запустите приложение** на эмуляторе или устройстве
2. **Проверьте Bottom Navigation:**
   - Переключение между табами (Главная, Поиск, Статистика, Профиль)
   - Активный таб выделен оранжевым (#FF6B35) с точкой-индикатором
   - Неактивные табы серые с пониженной непрозрачностью
3. **Проверьте детальный экран:**
   - На экране "Поиск" нажмите "Открыть продукт (demo)"
   - Bottom Navigation должен исчезнуть
   - Кнопка "Назад" возвращает на экран Поиск
   - Bottom Navigation появляется снова

---

## Следующие шаги

После реализации UI компонентов и экранов, placeholder'ы будут заменены на реальные экраны:
- HomeScreen с HomeViewModel
- SearchScreen с SearchViewModel
- ProductDetailScreen с ProductDetailViewModel
- StatsScreen с StatsViewModel
- ProfileScreen с ProfileViewModel

---

## Preview

Для просмотра Bottom Navigation Bar в Android Studio:
1. Откройте файл `BottomNavBar.kt`
2. Найдите `@Preview` функции:
   - `BottomNavBarPreviewLight()` - светлая тема
   - `BottomNavBarPreviewDark()` - тёмная тема
   - `BottomNavBarPreviewStats()` - с выбранным табом "Статистика"
3. Нажмите на иконку 👁️ для просмотра
