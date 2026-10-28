package com.neighborhero.neighborhero.data.local.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.neighborhero.neighborhero.data.model.NotificationItem
import com.neighborhero.neighborhero.data.model.NotificationType

class NotificationDao(private val dbHelper: () -> SQLiteDatabase) {

    companion object {
        const val TABLE_NAME = "cached_notifications"
        const val COL_ID = "id"
        const val COL_TITLE = "title"
        const val COL_MESSAGE = "message"
        const val COL_TIME_AGO = "time_ago"
        const val COL_TYPE = "type"
        const val COL_IS_READ = "is_read"

        const val CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                $COL_ID TEXT PRIMARY KEY,
                $COL_TITLE TEXT NOT NULL,
                $COL_MESSAGE TEXT NOT NULL,
                $COL_TIME_AGO TEXT NOT NULL,
                $COL_TYPE TEXT NOT NULL,
                $COL_IS_READ INTEGER NOT NULL
            )
        """
    }

    fun insertOrUpdate(item: NotificationItem) {
        val db = dbHelper()
        val values = ContentValues().apply {
            put(COL_ID, item.id)
            put(COL_TITLE, item.title)
            put(COL_MESSAGE, item.message)
            put(COL_TIME_AGO, item.timeAgo)
            put(COL_TYPE, item.type.name)
            put(COL_IS_READ, if (item.isRead) 1 else 0)
        }
        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun insertAll(items: List<NotificationItem>) {
        val db = dbHelper()
        db.beginTransaction()
        try {
            for (item in items) {
                insertOrUpdate(item)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getAll(): List<NotificationItem> {
        val db = dbHelper()
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, null)
        val list = mutableListOf<NotificationItem>()
        cursor.use { c ->
            while (c.moveToNext()) {
                val typeStr = c.getString(c.getColumnIndexOrThrow(COL_TYPE))
                val type = try {
                    NotificationType.valueOf(typeStr)
                } catch (e: Exception) {
                    NotificationType.COMMUNITY_NEWS
                }
                list.add(
                    NotificationItem(
                        id = c.getString(c.getColumnIndexOrThrow(COL_ID)),
                        title = c.getString(c.getColumnIndexOrThrow(COL_TITLE)),
                        message = c.getString(c.getColumnIndexOrThrow(COL_MESSAGE)),
                        timeAgo = c.getString(c.getColumnIndexOrThrow(COL_TIME_AGO)),
                        type = type,
                        isRead = c.getInt(c.getColumnIndexOrThrow(COL_IS_READ)) == 1
                    )
                )
            }
        }
        return list
    }

    fun markAsRead(id: String) {
        val db = dbHelper()
        val values = ContentValues().apply {
            put(COL_IS_READ, 1)
        }
        db.update(TABLE_NAME, values, "$COL_ID = ?", arrayOf(id))
    }

    fun markAllAsRead() {
        val db = dbHelper()
        val values = ContentValues().apply {
            put(COL_IS_READ, 1)
        }
        db.update(TABLE_NAME, values, null, null)
    }

    fun clear() {
        dbHelper().delete(TABLE_NAME, null, null)
    }
}

