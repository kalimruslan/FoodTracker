package com.ruslan.foodtracker.data.di;

import com.ruslan.foodtracker.data.local.FoodTrackerDatabase;
import com.ruslan.foodtracker.data.local.dao.FoodEntryDao;
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
public final class DatabaseModule_ProvideFoodEntryDaoFactory implements Factory<FoodEntryDao> {
  private final Provider<FoodTrackerDatabase> databaseProvider;

  public DatabaseModule_ProvideFoodEntryDaoFactory(Provider<FoodTrackerDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public FoodEntryDao get() {
    return provideFoodEntryDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideFoodEntryDaoFactory create(
      javax.inject.Provider<FoodTrackerDatabase> databaseProvider) {
    return new DatabaseModule_ProvideFoodEntryDaoFactory(Providers.asDaggerProvider(databaseProvider));
  }

  public static DatabaseModule_ProvideFoodEntryDaoFactory create(
      Provider<FoodTrackerDatabase> databaseProvider) {
    return new DatabaseModule_ProvideFoodEntryDaoFactory(databaseProvider);
  }

  public static FoodEntryDao provideFoodEntryDao(FoodTrackerDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideFoodEntryDao(database));
  }
}
