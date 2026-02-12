package com.ruslan.foodtracker.data.repository;

import com.ruslan.foodtracker.data.local.dao.FoodDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class FoodRepositoryImpl_Factory implements Factory<FoodRepositoryImpl> {
  private final Provider<FoodDao> foodDaoProvider;

  public FoodRepositoryImpl_Factory(Provider<FoodDao> foodDaoProvider) {
    this.foodDaoProvider = foodDaoProvider;
  }

  @Override
  public FoodRepositoryImpl get() {
    return newInstance(foodDaoProvider.get());
  }

  public static FoodRepositoryImpl_Factory create(javax.inject.Provider<FoodDao> foodDaoProvider) {
    return new FoodRepositoryImpl_Factory(Providers.asDaggerProvider(foodDaoProvider));
  }

  public static FoodRepositoryImpl_Factory create(Provider<FoodDao> foodDaoProvider) {
    return new FoodRepositoryImpl_Factory(foodDaoProvider);
  }

  public static FoodRepositoryImpl newInstance(FoodDao foodDao) {
    return new FoodRepositoryImpl(foodDao);
  }
}
