package com.ruslan.foodtracker.feature.search.di

import com.ruslan.foodtracker.core.navigation.FeatureApi
import com.ruslan.foodtracker.feature.search.navigation.SearchImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class SearchNavigationModule {

    @Binds
    @IntoSet
    abstract fun bindSearchApi(impl: SearchImpl): FeatureApi
}
