package com.ruslan.foodtracker.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.ruslan.foodtracker.data.local.dao.FoodDao;
import com.ruslan.foodtracker.data.local.dao.FoodDao_Impl;
import com.ruslan.foodtracker.data.local.dao.FoodEntryDao;
import com.ruslan.foodtracker.data.local.dao.FoodEntryDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class FoodTrackerDatabase_Impl extends FoodTrackerDatabase {
  private volatile FoodDao _foodDao;

  private volatile FoodEntryDao _foodEntryDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `foods` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `calories` INTEGER NOT NULL, `protein` REAL NOT NULL, `carbs` REAL NOT NULL, `fat` REAL NOT NULL, `servingSize` REAL NOT NULL, `servingUnit` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `food_entries` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `foodId` INTEGER NOT NULL, `foodName` TEXT NOT NULL, `servings` REAL NOT NULL, `calories` INTEGER NOT NULL, `protein` REAL NOT NULL, `carbs` REAL NOT NULL, `fat` REAL NOT NULL, `timestamp` TEXT NOT NULL, `mealType` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'bdbef1004a0bfa2c81e42f3951d0921d')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `foods`");
        db.execSQL("DROP TABLE IF EXISTS `food_entries`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsFoods = new HashMap<String, TableInfo.Column>(8);
        _columnsFoods.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFoods.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFoods.put("calories", new TableInfo.Column("calories", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFoods.put("protein", new TableInfo.Column("protein", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFoods.put("carbs", new TableInfo.Column("carbs", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFoods.put("fat", new TableInfo.Column("fat", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFoods.put("servingSize", new TableInfo.Column("servingSize", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFoods.put("servingUnit", new TableInfo.Column("servingUnit", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysFoods = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesFoods = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoFoods = new TableInfo("foods", _columnsFoods, _foreignKeysFoods, _indicesFoods);
        final TableInfo _existingFoods = TableInfo.read(db, "foods");
        if (!_infoFoods.equals(_existingFoods)) {
          return new RoomOpenHelper.ValidationResult(false, "foods(com.ruslan.foodtracker.data.local.entity.FoodEntity).\n"
                  + " Expected:\n" + _infoFoods + "\n"
                  + " Found:\n" + _existingFoods);
        }
        final HashMap<String, TableInfo.Column> _columnsFoodEntries = new HashMap<String, TableInfo.Column>(10);
        _columnsFoodEntries.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFoodEntries.put("foodId", new TableInfo.Column("foodId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFoodEntries.put("foodName", new TableInfo.Column("foodName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFoodEntries.put("servings", new TableInfo.Column("servings", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFoodEntries.put("calories", new TableInfo.Column("calories", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFoodEntries.put("protein", new TableInfo.Column("protein", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFoodEntries.put("carbs", new TableInfo.Column("carbs", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFoodEntries.put("fat", new TableInfo.Column("fat", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFoodEntries.put("timestamp", new TableInfo.Column("timestamp", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFoodEntries.put("mealType", new TableInfo.Column("mealType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysFoodEntries = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesFoodEntries = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoFoodEntries = new TableInfo("food_entries", _columnsFoodEntries, _foreignKeysFoodEntries, _indicesFoodEntries);
        final TableInfo _existingFoodEntries = TableInfo.read(db, "food_entries");
        if (!_infoFoodEntries.equals(_existingFoodEntries)) {
          return new RoomOpenHelper.ValidationResult(false, "food_entries(com.ruslan.foodtracker.data.local.entity.FoodEntryEntity).\n"
                  + " Expected:\n" + _infoFoodEntries + "\n"
                  + " Found:\n" + _existingFoodEntries);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "bdbef1004a0bfa2c81e42f3951d0921d", "0bd79ccee07d6cfb55b4b5809fcce3cb");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "foods","food_entries");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `foods`");
      _db.execSQL("DELETE FROM `food_entries`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(FoodDao.class, FoodDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(FoodEntryDao.class, FoodEntryDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public FoodDao foodDao() {
    if (_foodDao != null) {
      return _foodDao;
    } else {
      synchronized(this) {
        if(_foodDao == null) {
          _foodDao = new FoodDao_Impl(this);
        }
        return _foodDao;
      }
    }
  }

  @Override
  public FoodEntryDao foodEntryDao() {
    if (_foodEntryDao != null) {
      return _foodEntryDao;
    } else {
      synchronized(this) {
        if(_foodEntryDao == null) {
          _foodEntryDao = new FoodEntryDao_Impl(this);
        }
        return _foodEntryDao;
      }
    }
  }
}
