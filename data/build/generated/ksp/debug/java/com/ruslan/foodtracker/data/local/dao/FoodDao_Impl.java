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
import com.ruslan.foodtracker.data.local.entity.FoodEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
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
public final class FoodDao_Impl implements FoodDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<FoodEntity> __insertionAdapterOfFoodEntity;

  private final EntityDeletionOrUpdateAdapter<FoodEntity> __deletionAdapterOfFoodEntity;

  private final EntityDeletionOrUpdateAdapter<FoodEntity> __updateAdapterOfFoodEntity;

  public FoodDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfFoodEntity = new EntityInsertionAdapter<FoodEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `foods` (`id`,`name`,`calories`,`protein`,`carbs`,`fat`,`servingSize`,`servingUnit`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FoodEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getCalories());
        statement.bindDouble(4, entity.getProtein());
        statement.bindDouble(5, entity.getCarbs());
        statement.bindDouble(6, entity.getFat());
        statement.bindDouble(7, entity.getServingSize());
        statement.bindString(8, entity.getServingUnit());
      }
    };
    this.__deletionAdapterOfFoodEntity = new EntityDeletionOrUpdateAdapter<FoodEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `foods` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FoodEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfFoodEntity = new EntityDeletionOrUpdateAdapter<FoodEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `foods` SET `id` = ?,`name` = ?,`calories` = ?,`protein` = ?,`carbs` = ?,`fat` = ?,`servingSize` = ?,`servingUnit` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FoodEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getCalories());
        statement.bindDouble(4, entity.getProtein());
        statement.bindDouble(5, entity.getCarbs());
        statement.bindDouble(6, entity.getFat());
        statement.bindDouble(7, entity.getServingSize());
        statement.bindString(8, entity.getServingUnit());
        statement.bindLong(9, entity.getId());
      }
    };
  }

  @Override
  public Object insertFood(final FoodEntity food, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfFoodEntity.insertAndReturnId(food);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteFood(final FoodEntity food, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfFoodEntity.handle(food);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateFood(final FoodEntity food, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfFoodEntity.handle(food);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<FoodEntity>> getAllFoods() {
    final String _sql = "SELECT * FROM foods ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"foods"}, new Callable<List<FoodEntity>>() {
      @Override
      @NonNull
      public List<FoodEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCalories = CursorUtil.getColumnIndexOrThrow(_cursor, "calories");
          final int _cursorIndexOfProtein = CursorUtil.getColumnIndexOrThrow(_cursor, "protein");
          final int _cursorIndexOfCarbs = CursorUtil.getColumnIndexOrThrow(_cursor, "carbs");
          final int _cursorIndexOfFat = CursorUtil.getColumnIndexOrThrow(_cursor, "fat");
          final int _cursorIndexOfServingSize = CursorUtil.getColumnIndexOrThrow(_cursor, "servingSize");
          final int _cursorIndexOfServingUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "servingUnit");
          final List<FoodEntity> _result = new ArrayList<FoodEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FoodEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpCalories;
            _tmpCalories = _cursor.getInt(_cursorIndexOfCalories);
            final double _tmpProtein;
            _tmpProtein = _cursor.getDouble(_cursorIndexOfProtein);
            final double _tmpCarbs;
            _tmpCarbs = _cursor.getDouble(_cursorIndexOfCarbs);
            final double _tmpFat;
            _tmpFat = _cursor.getDouble(_cursorIndexOfFat);
            final double _tmpServingSize;
            _tmpServingSize = _cursor.getDouble(_cursorIndexOfServingSize);
            final String _tmpServingUnit;
            _tmpServingUnit = _cursor.getString(_cursorIndexOfServingUnit);
            _item = new FoodEntity(_tmpId,_tmpName,_tmpCalories,_tmpProtein,_tmpCarbs,_tmpFat,_tmpServingSize,_tmpServingUnit);
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
  public Object getFoodById(final long id, final Continuation<? super FoodEntity> $completion) {
    final String _sql = "SELECT * FROM foods WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<FoodEntity>() {
      @Override
      @Nullable
      public FoodEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCalories = CursorUtil.getColumnIndexOrThrow(_cursor, "calories");
          final int _cursorIndexOfProtein = CursorUtil.getColumnIndexOrThrow(_cursor, "protein");
          final int _cursorIndexOfCarbs = CursorUtil.getColumnIndexOrThrow(_cursor, "carbs");
          final int _cursorIndexOfFat = CursorUtil.getColumnIndexOrThrow(_cursor, "fat");
          final int _cursorIndexOfServingSize = CursorUtil.getColumnIndexOrThrow(_cursor, "servingSize");
          final int _cursorIndexOfServingUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "servingUnit");
          final FoodEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpCalories;
            _tmpCalories = _cursor.getInt(_cursorIndexOfCalories);
            final double _tmpProtein;
            _tmpProtein = _cursor.getDouble(_cursorIndexOfProtein);
            final double _tmpCarbs;
            _tmpCarbs = _cursor.getDouble(_cursorIndexOfCarbs);
            final double _tmpFat;
            _tmpFat = _cursor.getDouble(_cursorIndexOfFat);
            final double _tmpServingSize;
            _tmpServingSize = _cursor.getDouble(_cursorIndexOfServingSize);
            final String _tmpServingUnit;
            _tmpServingUnit = _cursor.getString(_cursorIndexOfServingUnit);
            _result = new FoodEntity(_tmpId,_tmpName,_tmpCalories,_tmpProtein,_tmpCarbs,_tmpFat,_tmpServingSize,_tmpServingUnit);
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

  @Override
  public Flow<List<FoodEntity>> searchFoods(final String query) {
    final String _sql = "SELECT * FROM foods WHERE name LIKE '%' || ? || '%' ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"foods"}, new Callable<List<FoodEntity>>() {
      @Override
      @NonNull
      public List<FoodEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCalories = CursorUtil.getColumnIndexOrThrow(_cursor, "calories");
          final int _cursorIndexOfProtein = CursorUtil.getColumnIndexOrThrow(_cursor, "protein");
          final int _cursorIndexOfCarbs = CursorUtil.getColumnIndexOrThrow(_cursor, "carbs");
          final int _cursorIndexOfFat = CursorUtil.getColumnIndexOrThrow(_cursor, "fat");
          final int _cursorIndexOfServingSize = CursorUtil.getColumnIndexOrThrow(_cursor, "servingSize");
          final int _cursorIndexOfServingUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "servingUnit");
          final List<FoodEntity> _result = new ArrayList<FoodEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FoodEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpCalories;
            _tmpCalories = _cursor.getInt(_cursorIndexOfCalories);
            final double _tmpProtein;
            _tmpProtein = _cursor.getDouble(_cursorIndexOfProtein);
            final double _tmpCarbs;
            _tmpCarbs = _cursor.getDouble(_cursorIndexOfCarbs);
            final double _tmpFat;
            _tmpFat = _cursor.getDouble(_cursorIndexOfFat);
            final double _tmpServingSize;
            _tmpServingSize = _cursor.getDouble(_cursorIndexOfServingSize);
            final String _tmpServingUnit;
            _tmpServingUnit = _cursor.getString(_cursorIndexOfServingUnit);
            _item = new FoodEntity(_tmpId,_tmpName,_tmpCalories,_tmpProtein,_tmpCarbs,_tmpFat,_tmpServingSize,_tmpServingUnit);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
