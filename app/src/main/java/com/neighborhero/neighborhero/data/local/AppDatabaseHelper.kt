package com.neighborhero.neighborhero.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.neighborhero.neighborhero.data.local.dao.HelpRequestDao
import com.neighborhero.neighborhero.data.local.dao.HelperDao
import com.neighborhero.neighborhero.data.local.dao.NotificationDao

class AppDatabaseHelper private constructor(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    companion object {
        const val DATABASE_NAME = "neighborhero_cache.db"
        const val DATABASE_VERSION = 3

        @Volatile
        private var instance: AppDatabaseHelper? = null

        fun getInstance(context: Context? = null): AppDatabaseHelper {
            return instance ?: synchronized(this) {
                instance ?: run {
                    val ctx = context?.applicationContext
                    checkNotNull(ctx) {
                        "AppDatabaseHelper has not been initialized. Call AppDatabaseHelper.getInstance(context) or RepositoryProvider.init(context) with a valid Context before calling getInstance()."
                    }
                    AppDatabaseHelper(ctx).also { instance = it }
                }
            }
        }

        fun isInitialized(): Boolean = instance != null

        fun resetInstanceForTesting() {
            synchronized(this) {
                instance?.close()
                instance = null
            }
        }
    }

    val helpRequestDao: HelpRequestDao by lazy { HelpRequestDao { writableDatabase } }
    val helperDao: HelperDao by lazy { HelperDao { writableDatabase } }
    val notificationDao: NotificationDao by lazy { NotificationDao { writableDatabase } }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(HelpRequestDao.CREATE_TABLE)
        db.execSQL(HelperDao.CREATE_TABLE)
        db.execSQL(NotificationDao.CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            ensureColumnExists(db, HelpRequestDao.TABLE_NAME, HelpRequestDao.COL_LATITUDE, "REAL")
            ensureColumnExists(db, HelpRequestDao.TABLE_NAME, HelpRequestDao.COL_LONGITUDE, "REAL")
        }
        if (oldVersion < 3) {
            ensureColumnExists(db, HelpRequestDao.TABLE_NAME, HelpRequestDao.COL_PHOTO_URL, "TEXT")
        }
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        ensureColumnExists(db, HelpRequestDao.TABLE_NAME, HelpRequestDao.COL_PHOTO_URL, "TEXT")
        ensureColumnExists(db, HelpRequestDao.TABLE_NAME, HelpRequestDao.COL_LATITUDE, "REAL")
        ensureColumnExists(db, HelpRequestDao.TABLE_NAME, HelpRequestDao.COL_LONGITUDE, "REAL")
        ensureColumnExists(db, HelpRequestDao.TABLE_NAME, HelpRequestDao.COL_REQUESTER_ID, "TEXT")
    }

    private fun ensureColumnExists(db: SQLiteDatabase, table: String, column: String, type: String) {
        try {
            val cursor = db.rawQuery("PRAGMA table_info($table)", null)
            var exists = false
            cursor.use { c ->
                val nameIndex = c.getColumnIndex("name")
                while (c.moveToNext()) {
                    if (nameIndex >= 0 && c.getString(nameIndex).equals(column, ignoreCase = true)) {
                        exists = true
                        break
                    }
                }
            }
            if (!exists) {
                db.execSQL("ALTER TABLE $table ADD COLUMN $column $type")
            }
        } catch (e: Exception) {
            android.util.Log.w("AppDatabaseHelper", "Column $column verification failed: ${e.message}")
        }
    }
}
