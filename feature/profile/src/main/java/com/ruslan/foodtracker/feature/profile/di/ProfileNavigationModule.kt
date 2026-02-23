package com.ruslan.foodtracker.feature.profile.di

import com.ruslan.foodtracker.core.navigation.FeatureApi
import com.ruslan.foodtracker.feature.profile.navigation.ProfileImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileNavigationModule {
    @Binds
    @IntoSet
    abstract fun bindProfileApi(impl: ProfileImpl): FeatureApi
}
