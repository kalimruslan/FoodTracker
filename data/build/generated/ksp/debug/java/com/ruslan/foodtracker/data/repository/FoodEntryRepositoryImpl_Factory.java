package com.ruslan.foodtracker.data.repository;

import com.ruslan.foodtracker.data.local.dao.FoodEntryDao;
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
public final class FoodEntryRepositoryImpl_Factory implements Factory<FoodEntryRepositoryImpl> {
  private final Provider<FoodEntryDao> foodEntryDaoProvider;

  public FoodEntryRepositoryImpl_Factory(Provider<FoodEntryDao> foodEntryDaoProvider) {
    this.foodEntryDaoProvider = foodEntryDaoProvider;
  }

  @Override
  public FoodEntryRepositoryImpl get() {
    return newInstance(foodEntryDaoProvider.get());
  }

  public static FoodEntryRepositoryImpl_Factory create(
      javax.inject.Provider<FoodEntryDao> foodEntryDaoProvider) {
    return new FoodEntryRepositoryImpl_Factory(Providers.asDaggerProvider(foodEntryDaoProvider));
  }

  public static FoodEntryRepositoryImpl_Factory create(
      Provider<FoodEntryDao> foodEntryDaoProvider) {
    return new FoodEntryRepositoryImpl_Factory(foodEntryDaoProvider);
  }

  public static FoodEntryRepositoryImpl newInstance(FoodEntryDao foodEntryDao) {
    return new FoodEntryRepositoryImpl(foodEntryDao);
  }
}
