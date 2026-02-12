package com.ruslan.foodtracker.feature.product.di

import com.ruslan.foodtracker.core.navigation.FeatureApi
import com.ruslan.foodtracker.feature.product.navigation.ProductImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class ProductNavigationModule {

    @Binds
    @IntoSet
    abstract fun bindProductApi(impl: ProductImpl): FeatureApi
}
