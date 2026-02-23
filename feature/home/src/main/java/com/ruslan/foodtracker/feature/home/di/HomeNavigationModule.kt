package com.ruslan.foodtracker.feature.home.di

import com.ruslan.foodtracker.core.navigation.FeatureApi
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
