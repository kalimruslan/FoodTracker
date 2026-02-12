package com.ruslan.foodtracker.data.di;

import com.ruslan.foodtracker.data.local.FoodTrackerDatabase;
import com.ruslan.foodtracker.data.local.dao.FoodDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideFoodDaoFactory implements Factory<FoodDao> {
  private final Provider<FoodTrackerDatabase> databaseProvider;

  public DatabaseModule_ProvideFoodDaoFactory(Provider<FoodTrackerDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public FoodDao get() {
    return provideFoodDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideFoodDaoFactory create(
      javax.inject.Provider<FoodTrackerDatabase> databaseProvider) {
    return new DatabaseModule_ProvideFoodDaoFactory(Providers.asDaggerProvider(databaseProvider));
  }

  public static DatabaseModule_ProvideFoodDaoFactory create(
      Provider<FoodTrackerDatabase> databaseProvider) {
    return new DatabaseModule_ProvideFoodDaoFactory(databaseProvider);
  }

  public static FoodDao provideFoodDao(FoodTrackerDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideFoodDao(database));
  }
}
