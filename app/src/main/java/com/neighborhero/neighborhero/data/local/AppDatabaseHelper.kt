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
        const val DATABASE_VERSION = 1

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
        db.execSQL("DROP TABLE IF EXISTS ${HelpRequestDao.TABLE_NAME}")
        db.execSQL("DROP TABLE IF EXISTS ${HelperDao.TABLE_NAME}")
        db.execSQL("DROP TABLE IF EXISTS ${NotificationDao.TABLE_NAME}")
        onCreate(db)
    }
}
