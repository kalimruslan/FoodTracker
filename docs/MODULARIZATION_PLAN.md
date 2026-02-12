# План модуляризации FoodTracker

## Общая информация

**Дата создания:** 2026-02-12
**Ветка:** `feature/modularization`
**Цель:** Разделение presentation слоя на независимые feature модули для улучшения изоляции, тестируемости и времени сборки

---

## Архитектурное решение: core:navigation подход

### Проблема

В текущей архитектуре все UI экраны находятся в `app` модуле:
- При изменении одного экрана пересобирается весь `app` модуль
- Невозможно работать над разными фичами параллельно без конфликтов
- Сложно тестировать фичи изолированно

### Решение

Создание отдельных **feature модулей** для каждой фичи с **core:navigation** как единым источником истины для навигации.

### Граф зависимостей

```
app
├── feature:home ──┐
├── feature:search ├─→ core:navigation (NavRoutes, FeatureApi)
├── feature:product├─→ domain (UseCases, Models)
├── feature:stats  ├─→ core:ui (Theme, Components)
└── feature:profile┘   core:common (Utils)

Ключевое правило: feature модули НЕ зависят друг от друга!
```

### Преимущества

✅ **Нет циклических зависимостей**
✅ **Минимальная пересборка** - изменение в feature:home → пересборка только home + app
✅ **Type-safe навигация** - Kotlin Serialization для маршрутов
✅ **Изоляция модулей** - features не знают друг о друге
✅ **Параллельная разработка** - разные команды могут работать над разными features
✅ **Легкое тестирование** - каждый feature можно тестировать отдельно

---

## Структура проекта ДО и ПОСЛЕ

### ДО модуляризации

```
FoodTracker/
├── app/
│   └── src/main/java/com/ruslan/foodtracker/
│       ├── FoodTrackerApp.kt
│       ├── MainActivity.kt
│       └── ui/
│           ├── home/          (HomeScreen, HomeViewModel)
│           ├── search/        (SearchScreen, SearchViewModel)
│           ├── product/       (ProductDetailScreen, ProductDetailViewModel)
│           ├── stats/         (StatsScreen, StatsViewModel)
│           ├── profile/       (ProfileScreen, ProfileViewModel)
│           └── navigation/    (NavRoutes, FoodNavHost, BottomNavBar)
├── domain/
├── data/
├── core/
│   ├── ui/
│   └── common/
```

### ПОСЛЕ модуляризации

```
FoodTracker/
├── app/
│   └── src/main/java/com/ruslan/foodtracker/
│       ├── FoodTrackerApp.kt
│       ├── MainActivity.kt
│       └── navigation/        (FoodNavHost, BottomNavBar) - интеграция
├── feature/                   ✨ НОВАЯ ПАПКА
│   ├── home/                  ✨ НОВЫЙ МОДУЛЬ
│   ├── search/                ✨ НОВЫЙ МОДУЛЬ
│   ├── product/               ✨ НОВЫЙ МОДУЛЬ
│   ├── stats/                 ✨ НОВЫЙ МОДУЛЬ
│   └── profile/               ✨ НОВЫЙ МОДУЛЬ
├── core/
│   ├── navigation/            ✨ НОВЫЙ МОДУЛЬ (FeatureApi, NavRoutes)
│   ├── ui/
│   └── common/
├── domain/
└── data/
```

---

## Детальный план реализации

### Фаза 0: Подготовка (создание ветки)

**Задачи:**

1. Создать ветку `feature/modularization` от `main`
2. Убедиться что проект собирается: `./gradlew clean assembleDebug`
3. Закоммитить текущее состояние как точку отката

**Команды:**

```bash
cd /Users/ruslankalimullin/AndroidStudioProjects/FoodTracker
git checkout main
git pull origin main
git checkout -b feature/modularization
./gradlew clean assembleDebug
git add -A
git commit -m "chore: Базовое состояние перед модуляризацией

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

**Критерий готовности:** ✅ Ветка создана, проект собирается без ошибок

---

### Фаза 1: Создание core:navigation модуля

**Цель:** Создать модуль с общими навигационными интерфейсами и маршрутами

#### Шаг 1.1: Создать структуру директорий

```bash
mkdir -p core/navigation/src/main/java/com/ruslan/foodtracker/core/navigation
```

#### Шаг 1.2: Создать build.gradle.kts

**Файл:** `core/navigation/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    // Navigation Compose (для NavGraphBuilder, NavHostController)
    implementation(libs.androidx.navigation.compose)

    // Kotlin Serialization (для @Serializable маршрутов)
    implementation(libs.kotlinx.serialization.json)

    // Javax Inject (для @Singleton, @Inject)
    implementation("javax.inject:javax.inject:1")
}
```

#### Шаг 1.3: Создать FeatureApi.kt

**Файл:** `core/navigation/src/main/java/com/ruslan/foodtracker/core/navigation/FeatureApi.kt`

```kotlin
package com.ruslan.foodtracker.core.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

/**
 * Базовый интерфейс для всех feature модулей.
 * Каждый feature модуль предоставляет свою реализацию для регистрации навигационного графа.
 */
interface FeatureApi {
    /**
     * Базовый маршрут для feature модуля (например, "home", "search")
     */
    val baseRoute: String

    /**
     * Регистрация навигационного графа feature модуля.
     *
     * @param navGraphBuilder NavGraphBuilder для добавления composable экранов
     * @param navController NavHostController для навигации между экранами
     */
    fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController
    )
}
```

#### Шаг 1.4: Переместить NavRoutes.kt

**Действие:** Переместить `app/src/.../ui/navigation/NavRoutes.kt` → `core/navigation/src/.../NavRoutes.kt`

**Файл:** `core/navigation/src/main/java/com/ruslan/foodtracker/core/navigation/NavRoutes.kt`

```kotlin
package com.ruslan.foodtracker.core.navigation

import kotlinx.serialization.Serializable

/**
 * Все маршруты навигации приложения.
 * Использует Type-Safe Navigation с Kotlin Serialization.
 */
sealed interface NavRoutes {

    /**
     * Главная страница (Дневник питания)
     */
    @Serializable
    data object Home : NavRoutes

    /**
     * Поиск продуктов
     */
    @Serializable
    data object Search : NavRoutes

    /**
     * Детальная информация о продукте
     * @param productId ID продукта для отображения
     */
    @Serializable
    data class ProductDetail(val productId: Long) : NavRoutes

    /**
     * Статистика за период
     */
    @Serializable
    data object Stats : NavRoutes

    /**
     * Профиль пользователя
     */
    @Serializable
    data object Profile : NavRoutes
}
```

#### Шаг 1.5: Обновить settings.gradle.kts

**Файл:** `settings.gradle.kts`

```kotlin
// ... существующий код ...

include(":app")
include(":domain")
include(":data")
include(":core:common")
include(":core:ui")
include(":core:navigation")  // ✨ ДОБАВИТЬ
```

#### Шаг 1.6: Тестирование

```bash
./gradlew :core:navigation:build
```

**Критерий готовности:** ✅ core:navigation модуль собирается без ошибок

---

### Фаза 2: Создание feature:home модуля (ЭТАЛОННЫЙ)

**Цель:** Создать первый feature модуль как эталон для остальных

#### Шаг 2.1: Создать структуру директорий

```bash
mkdir -p feature/home/src/main/java/com/ruslan/foodtracker/feature/home/navigation
mkdir -p feature/home/src/main/java/com/ruslan/foodtracker/feature/home/presenter
mkdir -p feature/home/src/main/java/com/ruslan/foodtracker/feature/home/di
```

#### Шаг 2.2: Создать build.gradle.kts

**Файл:** `feature/home/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.ruslan.foodtracker.feature.home"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Core modules
    implementation(project(":core:navigation"))  // NavRoutes, FeatureApi
    implementation(project(":core:ui"))          // Theme, Components
    implementation(project(":core:common"))      // Utils
    implementation(project(":domain"))           // UseCases, Models

    // НЕТ зависимостей на другие feature модули!
    // НЕТ зависимости на app модуль!

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Lifecycle & ViewModel
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.android.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Serialization
    implementation(libs.kotlinx.serialization.json)
}
```

#### Шаг 2.3: Переместить HomeScreen.kt и HomeViewModel.kt

**Действия:**

1. Скопировать `app/src/.../ui/home/HomeScreen.kt` → `feature/home/src/.../presenter/HomeScreen.kt`
2. Скопировать `app/src/.../ui/home/HomeViewModel.kt` → `feature/home/src/.../presenter/HomeViewModel.kt`
3. Обновить package в обоих файлах:

```kotlin
package com.ruslan.foodtracker.feature.home.presenter
```

4. Обновить imports для `NavRoutes`:

```kotlin
import com.ruslan.foodtracker.core.navigation.NavRoutes
```

#### Шаг 2.4: Создать HomeApi.kt

**Файл:** `feature/home/src/main/java/com/ruslan/foodtracker/feature/home/navigation/HomeApi.kt`

```kotlin
package com.ruslan.foodtracker.feature.home.navigation

import com.ruslan.foodtracker.core.navigation.FeatureApi

/**
 * API для feature:home модуля.
 * Предоставляет доступ к навигации для главного экрана.
 */
interface HomeApi : FeatureApi
```

#### Шаг 2.5: Создать HomeImpl.kt

**Файл:** `feature/home/src/main/java/com/ruslan/foodtracker/feature/home/navigation/HomeImpl.kt`

```kotlin
package com.ruslan.foodtracker.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.ruslan.foodtracker.core.navigation.FeatureApi
import com.ruslan.foodtracker.core.navigation.NavRoutes
import com.ruslan.foodtracker.feature.home.presenter.HomeScreen
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация навигации для feature:home модуля.
 */
@Singleton
class HomeImpl @Inject constructor() : HomeApi {

    override val baseRoute = "home"

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController
    ) {
        navGraphBuilder.composable<NavRoutes.Home> {
            HomeScreen(
                onNavigateToSearch = {
                    navController.navigate(NavRoutes.Search)
                }
            )
        }
    }
}
```

#### Шаг 2.6: Создать HomeNavigationModule.kt

**Файл:** `feature/home/src/main/java/com/ruslan/foodtracker/feature/home/di/HomeNavigationModule.kt`

```kotlin
package com.ruslan.foodtracker.feature.home.di

import com.ruslan.foodtracker.core.navigation.FeatureApi
import com.ruslan.foodtracker.feature.home.navigation.HomeApi
import com.ruslan.foodtracker.feature.home.navigation.HomeImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

/**
 * Hilt модуль для регистрации Home navigation API.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class HomeNavigationModule {

    /**
     * Добавляем HomeImpl в Set<FeatureApi> для автоматической регистрации в app модуле
     */
    @Binds
    @IntoSet
    abstract fun bindHomeApi(impl: HomeImpl): FeatureApi
}
```

#### Шаг 2.7: Обновить settings.gradle.kts

**Файл:** `settings.gradle.kts`

```kotlin
// ... существующие include ...
include(":core:navigation")
include(":feature:home")  // ✨ ДОБАВИТЬ
```

#### Шаг 2.8: Тестирование

```bash
./gradlew :feature:home:build
```

**Критерий готовности:** ✅ feature:home модуль собирается без ошибок

---

### Фаза 3: Создание feature:search модуля

**Цель:** Создать feature:search по аналогии с feature:home

#### Структура (краткая)

```
feature/search/
├── build.gradle.kts          (копия из home с заменой namespace)
└── src/main/java/com/ruslan/foodtracker/feature/search/
    ├── navigation/
    │   ├── SearchApi.kt      (interface SearchApi : FeatureApi)
    │   └── SearchImpl.kt     (composable<NavRoutes.Search>)
    ├── presenter/
    │   ├── SearchScreen.kt   (перенос из app)
    │   └── SearchViewModel.kt (перенос из app)
    └── di/
        └── SearchNavigationModule.kt (@Binds @IntoSet)
```

#### Ключевые отличия от home

**SearchImpl.kt:**
```kotlin
override fun registerGraph(
    navGraphBuilder: NavGraphBuilder,
    navController: NavHostController
) {
    navGraphBuilder.composable<NavRoutes.Search> {
        SearchScreen(
            onNavigateToProduct = { productId ->
                navController.navigate(NavRoutes.ProductDetail(productId))
            },
            onNavigateBack = {
                navController.navigateUp()
            }
        )
    }
}
```

#### Обновить settings.gradle.kts

```kotlin
include(":feature:search")  // ✨ ДОБАВИТЬ
```

**Критерий готовности:** ✅ feature:search модуль собирается без ошибок

---

### Фаза 4: Создание feature:product модуля

**Цель:** Создать feature:product с поддержкой параметра `productId`

#### Структура (краткая)

```
feature/product/
├── build.gradle.kts
└── src/main/java/com/ruslan/foodtracker/feature/product/
    ├── navigation/
    │   ├── ProductApi.kt
    │   └── ProductImpl.kt
    ├── presenter/
    │   ├── ProductDetailScreen.kt
    │   └── ProductDetailViewModel.kt
    └── di/
        └── ProductNavigationModule.kt
```

#### Ключевые отличия - ProductImpl.kt

```kotlin
override fun registerGraph(
    navGraphBuilder: NavGraphBuilder,
    navController: NavHostController
) {
    navGraphBuilder.composable<NavRoutes.ProductDetail> { backStackEntry ->
        // Type-safe извлечение аргумента
        val productDetail = backStackEntry.toRoute<NavRoutes.ProductDetail>()

        ProductDetailScreen(
            productId = productDetail.productId,
            onNavigateBack = {
                navController.navigateUp()
            }
        )
    }
}
```

#### Обновить settings.gradle.kts

```kotlin
include(":feature:product")  // ✨ ДОБАВИТЬ
```

**Критерий готовности:** ✅ feature:product модуль собирается без ошибок

---

### Фаза 5: Создание feature:stats модуля

**Цель:** Создать feature:stats (без параметров, простой экран)

#### Структура (краткая)

```
feature/stats/
├── build.gradle.kts
└── src/main/java/com/ruslan/foodtracker/feature/stats/
    ├── navigation/
    │   ├── StatsApi.kt
    │   └── StatsImpl.kt
    ├── presenter/
    │   ├── StatsScreen.kt
    │   └── StatsViewModel.kt
    └── di/
        └── StatsNavigationModule.kt
```

#### StatsImpl.kt

```kotlin
override fun registerGraph(
    navGraphBuilder: NavGraphBuilder,
    navController: NavHostController
) {
    navGraphBuilder.composable<NavRoutes.Stats> {
        StatsScreen()
    }
}
```

#### Обновить settings.gradle.kts

```kotlin
include(":feature:stats")  // ✨ ДОБАВИТЬ
```

**Критерий готовности:** ✅ feature:stats модуль собирается без ошибок

---

### Фаза 6: Создание feature:profile модуля

**Цель:** Создать feature:profile (без параметров, простой экран)

#### Структура (краткая)

```
feature/profile/
├── build.gradle.kts
└── src/main/java/com/ruslan/foodtracker/feature/profile/
    ├── navigation/
    │   ├── ProfileApi.kt
    │   └── ProfileImpl.kt
    ├── presenter/
    │   ├── ProfileScreen.kt
    │   └── ProfileViewModel.kt
    └── di/
        └── ProfileNavigationModule.kt
```

#### ProfileImpl.kt

```kotlin
override fun registerGraph(
    navGraphBuilder: NavGraphBuilder,
    navController: NavHostController
) {
    navGraphBuilder.composable<NavRoutes.Profile> {
        ProfileScreen()
    }
}
```

#### Обновить settings.gradle.kts

```kotlin
include(":feature:profile")  // ✨ ДОБАВИТЬ
```

**Критерий готовности:** ✅ feature:profile модуль собирается без ошибок

---

### Фаза 7: Рефакторинг app модуля

**Цель:** Интеграция всех feature модулей в app

#### Шаг 7.1: Обновить app/build.gradle.kts

**Добавить зависимости на все feature модули:**

```kotlin
dependencies {
    // Core modules
    implementation(project(":core:navigation"))  // ✨ ДОБАВИТЬ
    implementation(project(":core:ui"))
    implementation(project(":core:common"))
    implementation(project(":domain"))
    implementation(project(":data"))

    // Feature modules - только app зависит от них ✨ ДОБАВИТЬ
    implementation(project(":feature:home"))
    implementation(project(":feature:search"))
    implementation(project(":feature:product"))
    implementation(project(":feature:stats"))
    implementation(project(":feature:profile"))

    // ... остальные зависимости остаются без изменений
}
```

#### Шаг 7.2: Переместить navigation файлы

**Действия:**

1. Создать `app/src/main/java/com/ruslan/foodtracker/navigation/`
2. Переместить `app/src/.../ui/navigation/FoodNavHost.kt` → `app/src/.../navigation/FoodNavHost.kt`
3. Переместить `app/src/.../ui/navigation/BottomNavBar.kt` → `app/src/.../navigation/BottomNavBar.kt`
4. Обновить package в обоих файлах:

```kotlin
package com.ruslan.foodtracker.navigation
```

5. Обновить import для NavRoutes:

```kotlin
import com.ruslan.foodtracker.core.navigation.NavRoutes
```

#### Шаг 7.3: Рефакторинг FoodNavHost.kt

**Файл:** `app/src/main/java/com/ruslan/foodtracker/navigation/FoodNavHost.kt`

```kotlin
package com.ruslan.foodtracker.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ruslan.foodtracker.core.navigation.FeatureApi
import com.ruslan.foodtracker.core.navigation.NavRoutes

/**
 * Главный навигационный граф Food Tracker.
 * Автоматически регистрирует все feature модули через FeatureApi.
 */
@Composable
fun FoodNavHost(
    navController: NavHostController,
    featureApis: Set<FeatureApi>,  // Inject через Hilt в MainActivity
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route?.let { route ->
        when {
            route.contains("Home") -> NavRoutes.Home
            route.contains("Search") -> NavRoutes.Search
            route.contains("Stats") -> NavRoutes.Stats
            route.contains("Profile") -> NavRoutes.Profile
            route.contains("ProductDetail") -> null // Скрываем Bottom Bar
            else -> null
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (shouldShowBottomBar(currentRoute)) {
                FoodTrackerBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(NavRoutes.Home) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.Home,
            modifier = Modifier.padding(innerPadding)
        ) {
            // ✨ Автоматическая регистрация всех feature графов
            featureApis.forEach { api ->
                api.registerGraph(this, navController)
            }
        }
    }
}

/**
 * Определяет, когда показывать Bottom Navigation Bar
 */
private fun shouldShowBottomBar(currentRoute: NavRoutes?): Boolean {
    return when (currentRoute) {
        is NavRoutes.Home,
        is NavRoutes.Search,
        is NavRoutes.Stats,
        is NavRoutes.Profile -> true
        else -> false
    }
}
```

#### Шаг 7.4: Обновить BottomNavBar.kt imports

**Файл:** `app/src/main/java/com/ruslan/foodtracker/navigation/BottomNavBar.kt`

```kotlin
package com.ruslan.foodtracker.navigation

import com.ruslan.foodtracker.core.navigation.NavRoutes
// ... остальной код без изменений
```

#### Шаг 7.5: Обновить MainActivity.kt

**Файл:** `app/src/main/java/com/ruslan/foodtracker/MainActivity.kt`

```kotlin
package com.ruslan.foodtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.ruslan.foodtracker.core.navigation.FeatureApi
import com.ruslan.foodtracker.core.ui.theme.FoodTrackerTheme
import com.ruslan.foodtracker.navigation.FoodNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var featureApis: Set<@JvmSuppressWildcards FeatureApi>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FoodTrackerTheme {
                val navController = rememberNavController()

                FoodNavHost(
                    navController = navController,
                    featureApis = featureApis
                )
            }
        }
    }
}
```

#### Шаг 7.6: Удалить старые UI папки

**ВАЖНО: Удалять ТОЛЬКО после проверки что все файлы перенесены!**

```bash
# Проверить что папки пустые или содержат только navigation
ls -la app/src/main/java/com/ruslan/foodtracker/ui/

# Удалить старые папки
rm -rf app/src/main/java/com/ruslan/foodtracker/ui/home
rm -rf app/src/main/java/com/ruslan/foodtracker/ui/search
rm -rf app/src/main/java/com/ruslan/foodtracker/ui/product
rm -rf app/src/main/java/com/ruslan/foodtracker/ui/stats
rm -rf app/src/main/java/com/ruslan/foodtracker/ui/profile
rm -rf app/src/main/java/com/ruslan/foodtracker/ui/navigation

# Удалить папку ui (если пустая)
rmdir app/src/main/java/com/ruslan/foodtracker/ui
```

**Критерий готовности:** ✅ Все старые UI файлы удалены, остались только FoodTrackerApp, MainActivity, navigation/

---

### Фаза 8: Финальная сборка и тестирование

#### Шаг 8.1: Clean build

```bash
./gradlew clean
./gradlew build
```

**Ожидаемый результат:** Сборка завершается успешно без ошибок

#### Шаг 8.2: Собрать debug APK

```bash
./gradlew assembleDebug
```

#### Шаг 8.3: Установить на устройство/эмулятор

```bash
./gradlew installDebug
```

#### Шаг 8.4: Ручное тестирование

**Чек-лист:**

- [ ] Приложение запускается без краша
- [ ] Bottom Navigation Bar отображается на главном экране
- [ ] Переход на все 4 основных экрана (Home, Search, Stats, Profile) работает
- [ ] Переход на ProductDetail из Search работает
- [ ] Bottom Bar скрывается на ProductDetail экране
- [ ] Кнопка "Назад" на ProductDetail возвращает на Search
- [ ] Bottom Bar появляется снова при возврате
- [ ] Активный таб подсвечивается оранжевым цветом
- [ ] Состояние экранов сохраняется при переключении табов

#### Шаг 8.5: Проверка модульности (опционально)

**Тест изоляции модулей:**

1. Изменить что-то в `feature:home` (например, текст на экране)
2. Запустить сборку: `./gradlew assembleDebug`
3. Проверить вывод Gradle: должны пересобраться только `feature:home` + `app`
4. Модули `feature:search`, `feature:product`, `feature:stats`, `feature:profile` должны использовать кэш (UP-TO-DATE)

**Критерий готовности:** ✅ Все тесты пройдены, приложение работает корректно

---

### Фаза 9: Коммит и документация

#### Шаг 9.1: Проверить изменения

```bash
git status
git diff
```

#### Шаг 9.2: Создать коммит

```bash
git add -A
git commit -m "feat: Модуляризация presentation слоя на feature модули

Разделение UI на независимые feature модули для улучшения:
- Изоляции кода (каждый feature - отдельный модуль)
- Времени сборки (изменения в одном feature не пересобирают другие)
- Тестируемости (модули можно тестировать изолированно)

Создан core:navigation модуль:
- FeatureApi - базовый интерфейс для feature модулей
- NavRoutes - централизованные маршруты с type-safety

Созданы feature модули:
- feature:home - главный экран (дневник питания)
- feature:search - поиск продуктов
- feature:product - детальная информация о продукте
- feature:stats - статистика
- feature:profile - профиль пользователя

Каждый feature модуль:
- Имеет свой navigation API (FeatureApi implementation)
- Зависит от core:navigation, но НЕ от других features
- Регистрируется автоматически через Hilt @IntoSet

app модуль:
- Интегрирует все feature модули через Set<FeatureApi>
- Содержит только MainActivity, FoodTrackerApp, navigation setup
- Зависит от всех feature модулей (единственный модуль с такими зависимостями)

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

#### Шаг 9.3: Обновить NAVIGATION_README.md

**Добавить секцию о модуляризации в конец файла:**

```markdown
---

## Модуляризация (Feature Modules)

С версии от 2026-02-12 navigation разделена на feature модули.

### Архитектура

Каждый экран вынесен в отдельный feature модуль:
- `feature:home` - главная страница
- `feature:search` - поиск продуктов
- `feature:product` - детальная страница продукта
- `feature:stats` - статистика
- `feature:profile` - профиль

### Навигация между feature модулями

Feature модули используют `core:navigation` для доступа к `NavRoutes`:

```kotlin
// Из любого feature модуля
navController.navigate(NavRoutes.Search)
navController.navigate(NavRoutes.ProductDetail(productId = 123))
```

### Регистрация feature графов

Все feature модули автоматически регистрируются через Hilt:

```kotlin
// В app модуле
@Inject
lateinit var featureApis: Set<FeatureApi>

// В FoodNavHost
featureApis.forEach { api ->
    api.registerGraph(navGraphBuilder, navController)
}
```

Подробнее см. `MODULARIZATION_PLAN.md`
```

**Критерий готовности:** ✅ Изменения закоммичены, документация обновлена

---

## Итоговая структура settings.gradle.kts

```kotlin
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FoodTracker"

// Main modules
include(":app")
include(":domain")
include(":data")

// Core modules
include(":core:common")
include(":core:ui")
include(":core:navigation")  // ✨ НОВЫЙ

// Feature modules
include(":feature:home")     // ✨ НОВЫЙ
include(":feature:search")   // ✨ НОВЫЙ
include(":feature:product")  // ✨ НОВЫЙ
include(":feature:stats")    // ✨ НОВЫЙ
include(":feature:profile")  // ✨ НОВЫЙ
```

---

## Итоговый граф зависимостей

```
┌─────────────────────────────────────────────────────┐
│                       app                            │
│  (MainActivity, FoodNavHost, BottomNavBar)          │
└─────────────────────────────────────────────────────┘
        │
        ├─────────────────────────────────────┐
        │                                     │
        ▼                                     ▼
┌──────────────┐                      ┌──────────────┐
│ feature:home │                      │ feature:...  │
│ feature:search│                     │              │
│ feature:product│                    │              │
│ feature:stats│                      │              │
│ feature:profile│                    │              │
└──────────────┘                      └──────────────┘
        │                                     │
        ├─────────────────────────────────────┤
        │                                     │
        ▼                                     ▼
┌──────────────────┐              ┌──────────────────┐
│ core:navigation  │              │ domain           │
│ (NavRoutes,      │              │ (UseCases,       │
│  FeatureApi)     │              │  Models)         │
└──────────────────┘              └──────────────────┘
                                           │
                                           ▼
                                  ┌──────────────────┐
                                  │ data             │
                                  │ (Repositories,   │
                                  │  Room, etc.)     │
                                  └──────────────────┘
        ├─────────────────────────────────────┤
        ▼                                     ▼
┌──────────────┐                      ┌──────────────┐
│ core:ui      │                      │ core:common  │
│ (Theme,      │                      │ (Utils,      │
│  Components) │                      │  Constants)  │
└──────────────┘                      └──────────────┘
```

**Ключевые правила:**
- ✅ Только `app` зависит от `feature:*` модулей
- ✅ `feature:*` модули НЕ зависят друг от друга
- ✅ Все `feature:*` зависят от `core:navigation`
- ✅ Нет циклических зависимостей

---

## Проверка результата модуляризации

### До модуляризации

```bash
# Изменили HomeScreen.kt
# Пересборка: app (весь модуль) ❌
# Время: ~30-60 секунд
```

### После модуляризации

```bash
# Изменили HomeScreen.kt
# Пересборка: feature:home (5-10 сек) + app (10-20 сек) ✅
# Время: ~15-30 секунд
# НЕ пересобираются: feature:search, product, stats, profile, domain, data, core:*
```

**Экономия времени:** ~50% при изменении одного экрана

---

## Troubleshooting

### Проблема: Hilt не может найти FeatureApi

**Ошибка:**
```
error: [Dagger/MissingBinding] Set<FeatureApi> cannot be provided
```

**Решение:**
1. Проверить что в каждом feature модуле есть `NavigationModule` с `@Binds @IntoSet`
2. Проверить что все feature модули добавлены в `app/build.gradle.kts` как `implementation(project(":feature:..."))`
3. Пересобрать проект: `./gradlew clean build`

---

### Проблема: NavRoutes не найден в feature модуле

**Ошибка:**
```
Unresolved reference: NavRoutes
```

**Решение:**
1. Проверить import: `import com.ruslan.foodtracker.core.navigation.NavRoutes`
2. Проверить зависимость в `build.gradle.kts`: `implementation(project(":core:navigation"))`
3. Sync Gradle: File → Sync Project with Gradle Files

---

### Проблема: Циклическая зависимость

**Ошибка:**
```
Circular dependency between the following tasks
```

**Причина:** Feature модуль зависит от другого feature модуля

**Решение:**
1. Убрать прямую зависимость между feature модулями из `build.gradle.kts`
2. Использовать навигацию через `NavRoutes` вместо прямых ссылок

---

### Проблема: BottomNavBar не обновляется при смене экрана

**Причина:** `currentRoute` не определяется корректно

**Решение:**
Проверить логику в `FoodNavHost.kt`:
```kotlin
val currentRoute = navBackStackEntry?.destination?.route?.let { route ->
    when {
        route.contains("Home") -> NavRoutes.Home
        // ... остальные маршруты
    }
}
```

---

## Следующие шаги (после модуляризации)

1. **Convention Plugins** (опционально)
   - Создать `build-logic` модуль с convention plugins
   - Вынести общие зависимости в `android.feature` и `android.library.compose` plugins
   - Упростить `build.gradle.kts` файлы

2. **Тестирование feature модулей**
   - Добавить unit тесты для каждого ViewModel в своем feature модуле
   - Добавить UI тесты для экранов

3. **Dependency Analysis**
   - Использовать Gradle Dependency Analysis Plugin для проверки unused dependencies
   - Оптимизировать `implementation` vs `api`

4. **Parallel builds**
   - Включить параллельную сборку модулей в `gradle.properties`:
     ```properties
     org.gradle.parallel=true
     org.gradle.caching=true
     ```

5. **Feature flags**
   - Возможность включать/выключать feature модули на уровне сборки
   - Динамическая доставка feature модулей (Dynamic Feature Modules)

---

## Заключение

После выполнения всех фаз плана:

✅ Presentation слой разделен на изолированные feature модули
✅ Нет циклических зависимостей
✅ Сборка оптимизирована (изменение в одном feature не пересобирает другие)
✅ Type-safe навигация сохранена
✅ Код структурирован и легко масштабируется
✅ Каждый feature можно разрабатывать и тестировать независимо

**Архитектура готова к росту команды и кодовой базы!** 🚀