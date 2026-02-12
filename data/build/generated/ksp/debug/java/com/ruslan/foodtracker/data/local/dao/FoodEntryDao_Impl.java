package com.ruslan.foodtracker.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.ruslan.foodtracker.data.local.converter.Converters;
import com.ruslan.foodtracker.data.local.entity.FoodEntryEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalStateException;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class FoodEntryDao_Impl implements FoodEntryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<FoodEntryEntity> __insertionAdapterOfFoodEntryEntity;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<FoodEntryEntity> __deletionAdapterOfFoodEntryEntity;

  private final EntityDeletionOrUpdateAdapter<FoodEntryEntity> __updateAdapterOfFoodEntryEntity;

  public FoodEntryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfFoodEntryEntity = new EntityInsertionAdapter<FoodEntryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `food_entries` (`id`,`foodId`,`foodName`,`servings`,`calories`,`protein`,`carbs`,`fat`,`timestamp`,`mealType`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FoodEntryEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getFoodId());
        statement.bindString(3, entity.getFoodName());
        statement.bindDouble(4, entity.getServings());
        statement.bindLong(5, entity.getCalories());
        statement.bindDouble(6, entity.getProtein());
        statement.bindDouble(7, entity.getCarbs());
        statement.bindDouble(8, entity.getFat());
        final String _tmp = __converters.fromLocalDateTime(entity.getTimestamp());
        if (_tmp == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, _tmp);
        }
        statement.bindString(10, entity.getMealType());
      }
    };
    this.__deletionAdapterOfFoodEntryEntity = new EntityDeletionOrUpdateAdapter<FoodEntryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `food_entries` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FoodEntryEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfFoodEntryEntity = new EntityDeletionOrUpdateAdapter<FoodEntryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `food_entries` SET `id` = ?,`foodId` = ?,`foodName` = ?,`servings` = ?,`calories` = ?,`protein` = ?,`carbs` = ?,`fat` = ?,`timestamp` = ?,`mealType` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FoodEntryEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getFoodId());
        statement.bindString(3, entity.getFoodName());
        statement.bindDouble(4, entity.getServings());
        statement.bindLong(5, entity.getCalories());
        statement.bindDouble(6, entity.getProtein());
        statement.bindDouble(7, entity.getCarbs());
        statement.bindDouble(8, entity.getFat());
        final String _tmp = __converters.fromLocalDateTime(entity.getTimestamp());
        if (_tmp == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, _tmp);
        }
        statement.bindString(10, entity.getMealType());
        statement.bindLong(11, entity.getId());
      }
    };
  }

  @Override
  public Object insertEntry(final FoodEntryEntity entry,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfFoodEntryEntity.insertAndReturnId(entry);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteEntry(final FoodEntryEntity entry,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfFoodEntryEntity.handle(entry);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateEntry(final FoodEntryEntity entry,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfFoodEntryEntity.handle(entry);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<FoodEntryEntity>> getAllEntries() {
    final String _sql = "SELECT * FROM food_entries ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"food_entries"}, new Callable<List<FoodEntryEntity>>() {
      @Override
      @NonNull
      public List<FoodEntryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFoodId = CursorUtil.getColumnIndexOrThrow(_cursor, "foodId");
          final int _cursorIndexOfFoodName = CursorUtil.getColumnIndexOrThrow(_cursor, "foodName");
          final int _cursorIndexOfServings = CursorUtil.getColumnIndexOrThrow(_cursor, "servings");
          final int _cursorIndexOfCalories = CursorUtil.getColumnIndexOrThrow(_cursor, "calories");
          final int _cursorIndexOfProtein = CursorUtil.getColumnIndexOrThrow(_cursor, "protein");
          final int _cursorIndexOfCarbs = CursorUtil.getColumnIndexOrThrow(_cursor, "carbs");
          final int _cursorIndexOfFat = CursorUtil.getColumnIndexOrThrow(_cursor, "fat");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfMealType = CursorUtil.getColumnIndexOrThrow(_cursor, "mealType");
          final List<FoodEntryEntity> _result = new ArrayList<FoodEntryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FoodEntryEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpFoodId;
            _tmpFoodId = _cursor.getLong(_cursorIndexOfFoodId);
            final String _tmpFoodName;
            _tmpFoodName = _cursor.getString(_cursorIndexOfFoodName);
            final double _tmpServings;
            _tmpServings = _cursor.getDouble(_cursorIndexOfServings);
            final int _tmpCalories;
            _tmpCalories = _cursor.getInt(_cursorIndexOfCalories);
            final double _tmpProtein;
            _tmpProtein = _cursor.getDouble(_cursorIndexOfProtein);
            final double _tmpCarbs;
            _tmpCarbs = _cursor.getDouble(_cursorIndexOfCarbs);
            final double _tmpFat;
            _tmpFat = _cursor.getDouble(_cursorIndexOfFat);
            final LocalDateTime _tmpTimestamp;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfTimestamp)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfTimestamp);
            }
            final LocalDateTime _tmp_1 = __converters.toLocalDateTime(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpTimestamp = _tmp_1;
            }
            final String _tmpMealType;
            _tmpMealType = _cursor.getString(_cursorIndexOfMealType);
            _item = new FoodEntryEntity(_tmpId,_tmpFoodId,_tmpFoodName,_tmpServings,_tmpCalories,_tmpProtein,_tmpCarbs,_tmpFat,_tmpTimestamp,_tmpMealType);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getEntryById(final long id,
      final Continuation<? super FoodEntryEntity> $completion) {
    final String _sql = "SELECT * FROM food_entries WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<FoodEntryEntity>() {
      @Override
      @Nullable
      public FoodEntryEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFoodId = CursorUtil.getColumnIndexOrThrow(_cursor, "foodId");
          final int _cursorIndexOfFoodName = CursorUtil.getColumnIndexOrThrow(_cursor, "foodName");
          final int _cursorIndexOfServings = CursorUtil.getColumnIndexOrThrow(_cursor, "servings");
          final int _cursorIndexOfCalories = CursorUtil.getColumnIndexOrThrow(_cursor, "calories");
          final int _cursorIndexOfProtein = CursorUtil.getColumnIndexOrThrow(_cursor, "protein");
          final int _cursorIndexOfCarbs = CursorUtil.getColumnIndexOrThrow(_cursor, "carbs");
          final int _cursorIndexOfFat = CursorUtil.getColumnIndexOrThrow(_cursor, "fat");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfMealType = CursorUtil.getColumnIndexOrThrow(_cursor, "mealType");
          final FoodEntryEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpFoodId;
            _tmpFoodId = _cursor.getLong(_cursorIndexOfFoodId);
            final String _tmpFoodName;
            _tmpFoodName = _cursor.getString(_cursorIndexOfFoodName);
            final double _tmpServings;
            _tmpServings = _cursor.getDouble(_cursorIndexOfServings);
            final int _tmpCalories;
            _tmpCalories = _cursor.getInt(_cursorIndexOfCalories);
            final double _tmpProtein;
            _tmpProtein = _cursor.getDouble(_cursorIndexOfProtein);
            final double _tmpCarbs;
            _tmpCarbs = _cursor.getDouble(_cursorIndexOfCarbs);
            final double _tmpFat;
            _tmpFat = _cursor.getDouble(_cursorIndexOfFat);
            final LocalDateTime _tmpTimestamp;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfTimestamp)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfTimestamp);
            }
            final LocalDateTime _tmp_1 = __converters.toLocalDateTime(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.");
            } else {
              _tmpTimestamp = _tmp_1;
            }
            final String _tmpMealType;
            _tmpMealType = _cursor.getString(_cursorIndexOfMealType);
            _result = new FoodEntryEntity(_tmpId,_tmpFoodId,_tmpFoodName,_tmpServings,_tmpCalories,_tmpProtein,_tmpCarbs,_tmpFat,_tmpTimestamp,_tmpMealType);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
