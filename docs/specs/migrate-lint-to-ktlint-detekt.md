# Спецификация: Миграция с Android Lint на ktlint + detekt

Дата: 2026-02-23
Статус: Ready for implementation

---

## 1. Цель миграции

Android Lint — инструмент общего назначения для Android-проектов. Он хорошо находит
проблемы, специфичные для Android (неправильное использование API, accessibility,
ресурсы), но плохо подходит для контроля стиля Kotlin-кода и архитектурных метрик.

Цели перехода:
- **ktlint** — автоматическая проверка и форматирование Kotlin-кода по официальному
  Kotlin Coding Conventions. Заменяет ручные code review на предмет стиля.
- **detekt** — статический анализ Kotlin: сложность кода, потенциальные баги,
  code smells, архитектурные нарушения. Дополняет то, что Lint не умеет.
- Android Lint **остаётся** для Android-специфичных проверок (ресурсы, XML,
  Manifest, API уровни), но выводится из роли «единственного линтера» в CI.
- Результат: три независимых инструмента с чёткими зонами ответственности,
  все запускаются в CI.

---

## 2. Инструменты и версии

| Инструмент | Плагин/артефакт | Версия |
|---|---|---|
| ktlint-gradle | `org.jlleitschuh.gradle.ktlint` | 12.1.1 |
| ktlint (движок) | автоматически тянется плагином | 1.4.1 |
| detekt | `io.gitlab.arturbosch.detekt` | 1.23.7 |
| detekt-compose | `io.nlopez.compose.rules:detekt` | 0.4.14 |

---

## 3. Файлы для изменения

### 3.1 `gradle/libs.versions.toml`

Добавить в секцию `[versions]`:
```toml
ktlint = "12.1.1"
detekt = "1.23.7"
detektCompose = "0.4.14"
```

Добавить в секцию `[libraries]`:
```toml
detekt-compose-rules = { group = "io.nlopez.compose.rules", name = "detekt", version.ref = "detektCompose" }
```

Добавить в секцию `[plugins]`:
```toml
ktlint = { id = "org.jlleitschuh.gradle.ktlint", version.ref = "ktlint" }
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
```

### 3.2 `build.gradle.kts` (корневой)

Добавить в plugins (apply false):
```kotlin
alias(libs.plugins.ktlint) apply false
alias(libs.plugins.detekt) apply false
```

Добавить блок subprojects после plugins:
```kotlin
subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.4.1")
        android.set(true)
        ignoreFailures.set(false)
        reporters {
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.SARIF)
        }
        filter {
            exclude("**/generated/**")
            exclude("**/build/**")
        }
    }

    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom(rootProject.file("config/detekt/detekt.yml"))
        baseline = rootProject.file("config/detekt/baseline.xml")
        buildUponDefaultConfig = true
        allRules = false
        parallel = true
        source.setFrom(
            "src/main/java",
            "src/main/kotlin",
            "src/test/java",
            "src/test/kotlin"
        )
    }

    dependencies {
        "detektPlugins"(libs.detekt.compose.rules)
    }
}

tasks.register("detektAll") {
    group = "verification"
    description = "Runs detekt in all submodules"
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("detekt") })
}
```

### 3.3 `.editorconfig` (новый файл, корень проекта)

```ini
root = true

[*]
charset = utf-8
end_of_line = lf
indent_size = 4
indent_style = space
insert_final_newline = true
max_line_length = 120
trim_trailing_whitespace = true

[*.{kt,kts}]
ktlint_standard_no-wildcard-imports = enabled
ktlint_standard_import-ordering = enabled
ktlint_standard_max-line-length = enabled
ktlint_standard_trailing-comma-on-call-site = disabled
ktlint_standard_trailing-comma-on-declaration-site = disabled
ktlint_standard_multiline-expression-wrapping = disabled
ktlint_standard_function-naming = disabled
```

### 3.4 `config/detekt/detekt.yml` (новый файл)

```yaml
build:
  maxIssues: 0

config:
  validation: true
  warningsAsErrors: false

complexity:
  ComplexMethod:
    threshold: 15
  LongMethod:
    threshold: 60
  LongParameterList:
    threshold: 8
    ignoreDefaultParameters: true
  TooManyFunctions:
    thresholdInFiles: 30
    thresholdInClasses: 15
    thresholdInInterfaces: 10
    thresholdInObjects: 15
    thresholdInEnums: 8

naming:
  FunctionNaming:
    functionPattern: '[a-zA-Z][a-zA-Z0-9]*'
    ignoreAnnotated: ['Composable']
  TopLevelPropertyNaming:
    constantPattern: '[A-Z][_A-Z0-9]*'
    propertyPattern: '[a-z][a-zA-Z0-9]*'
    privatePropertyPattern: '_?[a-z][a-zA-Z0-9]*'

style:
  MagicNumber:
    ignoreNumbers: ['-1', '0', '1', '2', '100', '0.5', '1f', '0f']
    ignoreHashCodeFunction: true
    ignorePropertyDeclaration: true
    ignoreConstantDeclaration: true
    ignoreCompanionObjectPropertyDeclaration: true
    ignoreAnnotation: true
    ignoreNamedArgument: true
    ignoreEnums: true
  ReturnCount:
    max: 4
  WildcardImport:
    active: true
    excludeImports:
      - 'androidx.compose.foundation.layout.*'
      - 'androidx.compose.material3.*'
      - 'androidx.compose.ui.*'

coroutines:
  active: true
  GlobalCoroutineUsage:
    active: true
  RedundantSuspendModifier:
    active: true
  SuspendFunWithFlowReturnType:
    active: true
```

### 3.5 `config/detekt/baseline.xml` (новый файл)

```xml
<?xml version="1.0" encoding="utf-8"?>
<SmellBaseline>
  <ManuallySuppressedIssues/>
  <CurrentIssues/>
</SmellBaseline>
```

### 3.6 `lint.xml` (новый файл, корень проекта)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<lint>
    <issue id="NullSafeMutableLiveData" severity="ignore"/>
</lint>
```

### 3.7 Изменения lint-блоков в модулях

**`app/build.gradle.kts`** — упростить lint блок:
```kotlin
lint {
    abortOnError = false
}
```

**Из всех feature-модулей** (`feature/home`, `feature/search`, `feature/product`,
`feature/stats`, `feature/profile`) — удалить:
```kotlin
lint {
    disable += "NullSafeMutableLiveData"
}
```

### 3.8 `.github/workflows/ci.yml`

Заменить единственный `lint` job на три отдельных:

```yaml
  ktlint:
    name: ktlint
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Cache Gradle
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties', '**/libs.versions.toml') }}
          restore-keys: |
            ${{ runner.os }}-gradle-
      - name: Make gradlew executable
        run: chmod +x ./gradlew
      - name: Run ktlint
        run: ./gradlew ktlintCheck --no-daemon --continue
      - name: Upload ktlint Reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: ktlint-reports
          path: '**/build/reports/ktlint/**'

  detekt:
    name: detekt
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Cache Gradle
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties', '**/libs.versions.toml') }}
          restore-keys: |
            ${{ runner.os }}-gradle-
      - name: Make gradlew executable
        run: chmod +x ./gradlew
      - name: Run detekt
        run: ./gradlew detektAll --no-daemon --continue
      - name: Upload detekt Reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: detekt-reports
          path: '**/build/reports/detekt/**'

  lint:
    name: Android Lint
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Cache Gradle
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties', '**/libs.versions.toml') }}
          restore-keys: |
            ${{ runner.os }}-gradle-
      - name: Make gradlew executable
        run: chmod +x ./gradlew
      - name: Run Android Lint (app only)
        run: ./gradlew :app:lintDebug --no-daemon
      - name: Upload Lint Reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: lint-reports
          path: 'app/build/reports/lint-results*'
```

---

## 4. Порядок выполнения

1. Обновить `gradle/libs.versions.toml` (добавить версии и объявления)
2. Создать конфиг-файлы: `.editorconfig`, `config/detekt/detekt.yml`, `config/detekt/baseline.xml`, `lint.xml`
3. Обновить корневой `build.gradle.kts` (добавить плагины + subprojects блок)
4. Убрать дублирующие lint-блоки из feature-модулей и app
5. Запустить `./gradlew ktlintFormat` для автоформатирования
6. Запустить `./gradlew detektBaseline` для формирования baseline
7. Обновить `.github/workflows/ci.yml`
8. Финальная проверка: `./gradlew ktlintCheck detektAll :app:lintDebug`

---

## 5. Потенциальные проблемы

### Composable-функции с заглавной буквы
ktlint по умолчанию требует lowerCamelCase. Решение: `ktlint_standard_function-naming = disabled` в `.editorconfig`.

### LongParameterList в Composable
Решение: `ignoreDefaultParameters: true` и `threshold: 8` в detekt.yml.

### Kotlin 2.1.0 + detekt
detekt 1.23.7 совместима с Kotlin 2.0+. При проблемах — отключать конкретные правила в detekt.yml.

---

## 6. Команды для разработчиков

```bash
# Форматирование
./gradlew ktlintFormat

# Проверка форматирования
./gradlew ktlintCheck

# Статический анализ
./gradlew detektAll

# Android Lint
./gradlew :app:lintDebug

# Всё вместе
./gradlew ktlintCheck detektAll :app:lintDebug
```