plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.ruslan.foodtracker.core.navigation"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    // Navigation Compose (для NavGraphBuilder, NavHostController)
    api(libs.androidx.navigation.compose)

    // Kotlin Serialization (для @Serializable маршрутов)
    api(libs.kotlinx.serialization.json)

    // Javax Inject (для @Singleton, @Inject)
    api("javax.inject:javax.inject:1")
}
