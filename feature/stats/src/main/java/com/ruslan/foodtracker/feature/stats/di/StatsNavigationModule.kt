package com.ruslan.foodtracker.feature.stats.di

import com.ruslan.foodtracker.core.navigation.FeatureApi
import com.ruslan.foodtracker.feature.stats.navigation.StatsImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class StatsNavigationModule {
    @Binds
    @IntoSet
    abstract fun bindStatsApi(impl: StatsImpl): FeatureApi
}
