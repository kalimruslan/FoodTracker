package com.ruslan.foodtracker.data.di;

import android.content.Context;
import com.ruslan.foodtracker.data.local.FoodTrackerDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class DatabaseModule_ProvideFoodTrackerDatabaseFactory implements Factory<FoodTrackerDatabase> {
  private final Provider<Context> contextProvider;

  public DatabaseModule_ProvideFoodTrackerDatabaseFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public FoodTrackerDatabase get() {
    return provideFoodTrackerDatabase(contextProvider.get());
  }

  public static DatabaseModule_ProvideFoodTrackerDatabaseFactory create(
      javax.inject.Provider<Context> contextProvider) {
    return new DatabaseModule_ProvideFoodTrackerDatabaseFactory(Providers.asDaggerProvider(contextProvider));
  }

  public static DatabaseModule_ProvideFoodTrackerDatabaseFactory create(
      Provider<Context> contextProvider) {
    return new DatabaseModule_ProvideFoodTrackerDatabaseFactory(contextProvider);
  }

  public static FoodTrackerDatabase provideFoodTrackerDatabase(Context context) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideFoodTrackerDatabase(context));
  }
}
