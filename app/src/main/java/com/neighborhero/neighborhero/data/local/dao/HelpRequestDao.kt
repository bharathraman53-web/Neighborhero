package com.neighborhero.neighborhero.data.local.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.neighborhero.neighborhero.data.model.HelpRequest
import com.neighborhero.neighborhero.data.model.RequestStatus

class HelpRequestDao(private val dbHelper: () -> SQLiteDatabase) {

    companion object {
        const val TABLE_NAME = "cached_requests"
        const val COL_ID = "id"
        const val COL_TITLE = "title"
        const val COL_CATEGORY = "category"
        const val COL_DESCRIPTION = "description"
        const val COL_REQUESTER_NAME = "requester_name"
        const val COL_REQUESTER_RATING = "requester_rating"
        const val COL_REQUESTER_TASK_COUNT = "requester_task_count"
        const val COL_LOCATION = "location"
        const val COL_DISTANCE = "distance"
        const val COL_REQUESTED_TIME = "requested_time"
        const val COL_IS_URGENT = "is_urgent"
        const val COL_STATUS = "status"
        const val COL_ASSIGNED_HELPER_ID = "assigned_helper_id"
        const val COL_ASSIGNED_HELPER_NAME = "assigned_helper_name"

        const val CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                $COL_ID TEXT PRIMARY KEY,
                $COL_TITLE TEXT NOT NULL,
                $COL_CATEGORY TEXT NOT NULL,
                $COL_DESCRIPTION TEXT NOT NULL,
                $COL_REQUESTER_NAME TEXT NOT NULL,
                $COL_REQUESTER_RATING REAL NOT NULL,
                $COL_REQUESTER_TASK_COUNT INTEGER NOT NULL,
                $COL_LOCATION TEXT NOT NULL,
                $COL_DISTANCE TEXT NOT NULL,
                $COL_REQUESTED_TIME TEXT NOT NULL,
                $COL_IS_URGENT INTEGER NOT NULL,
                $COL_STATUS TEXT NOT NULL,
                $COL_ASSIGNED_HELPER_ID TEXT,
                $COL_ASSIGNED_HELPER_NAME TEXT
            )
        """
    }

    fun insertOrUpdate(request: HelpRequest) {
        val db = dbHelper()
        val values = ContentValues().apply {
            put(COL_ID, request.id)
            put(COL_TITLE, request.title)
            put(COL_CATEGORY, request.category)
            put(COL_DESCRIPTION, request.description)
            put(COL_REQUESTER_NAME, request.requesterName)
            put(COL_REQUESTER_RATING, request.requesterRating)
            put(COL_REQUESTER_TASK_COUNT, request.requesterTaskCount)
            put(COL_LOCATION, request.location)
            put(COL_DISTANCE, request.distance)
            put(COL_REQUESTED_TIME, request.requestedTime)
            put(COL_IS_URGENT, if (request.isUrgent) 1 else 0)
            put(COL_STATUS, request.status.name)
            put(COL_ASSIGNED_HELPER_ID, request.assignedHelperId)
            put(COL_ASSIGNED_HELPER_NAME, request.assignedHelperName)
        }
        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun insertAll(requests: List<HelpRequest>) {
        val db = dbHelper()
        db.beginTransaction()
        try {
            for (req in requests) {
                insertOrUpdate(req)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getAll(): List<HelpRequest> {
        val db = dbHelper()
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, "$COL_IS_URGENT DESC")
        val list = mutableListOf<HelpRequest>()
        cursor.use { c ->
            while (c.moveToNext()) {
                val statusStr = c.getString(c.getColumnIndexOrThrow(COL_STATUS))
                val status = try {
                    RequestStatus.valueOf(statusStr)
                } catch (e: Exception) {
                    RequestStatus.OPEN
                }
                list.add(
                    HelpRequest(
                        id = c.getString(c.getColumnIndexOrThrow(COL_ID)),
                        title = c.getString(c.getColumnIndexOrThrow(COL_TITLE)),
                        category = c.getString(c.getColumnIndexOrThrow(COL_CATEGORY)),
                        description = c.getString(c.getColumnIndexOrThrow(COL_DESCRIPTION)),
                        requesterName = c.getString(c.getColumnIndexOrThrow(COL_REQUESTER_NAME)),
                        requesterRating = c.getFloat(c.getColumnIndexOrThrow(COL_REQUESTER_RATING)),
                        requesterTaskCount = c.getInt(c.getColumnIndexOrThrow(COL_REQUESTER_TASK_COUNT)),
                        location = c.getString(c.getColumnIndexOrThrow(COL_LOCATION)),
                        distance = c.getString(c.getColumnIndexOrThrow(COL_DISTANCE)),
                        requestedTime = c.getString(c.getColumnIndexOrThrow(COL_REQUESTED_TIME)),
                        isUrgent = c.getInt(c.getColumnIndexOrThrow(COL_IS_URGENT)) == 1,
                        status = status,
                        assignedHelperId = c.getString(c.getColumnIndexOrThrow(COL_ASSIGNED_HELPER_ID)),
                        assignedHelperName = c.getString(c.getColumnIndexOrThrow(COL_ASSIGNED_HELPER_NAME))
                    )
                )
            }
        }
        return list
    }

    fun getById(id: String): HelpRequest? {
        val db = dbHelper()
        val cursor = db.query(TABLE_NAME, null, "$COL_ID = ?", arrayOf(id), null, null, null)
        cursor.use { c ->
            if (c.moveToFirst()) {
                val statusStr = c.getString(c.getColumnIndexOrThrow(COL_STATUS))
                val status = try {
                    RequestStatus.valueOf(statusStr)
                } catch (e: Exception) {
                    RequestStatus.OPEN
                }
                return HelpRequest(
                    id = c.getString(c.getColumnIndexOrThrow(COL_ID)),
                    title = c.getString(c.getColumnIndexOrThrow(COL_TITLE)),
                    category = c.getString(c.getColumnIndexOrThrow(COL_CATEGORY)),
                    description = c.getString(c.getColumnIndexOrThrow(COL_DESCRIPTION)),
                    requesterName = c.getString(c.getColumnIndexOrThrow(COL_REQUESTER_NAME)),
                    requesterRating = c.getFloat(c.getColumnIndexOrThrow(COL_REQUESTER_RATING)),
                    requesterTaskCount = c.getInt(c.getColumnIndexOrThrow(COL_REQUESTER_TASK_COUNT)),
                    location = c.getString(c.getColumnIndexOrThrow(COL_LOCATION)),
                    distance = c.getString(c.getColumnIndexOrThrow(COL_DISTANCE)),
                    requestedTime = c.getString(c.getColumnIndexOrThrow(COL_REQUESTED_TIME)),
                    isUrgent = c.getInt(c.getColumnIndexOrThrow(COL_IS_URGENT)) == 1,
                    status = status,
                    assignedHelperId = c.getString(c.getColumnIndexOrThrow(COL_ASSIGNED_HELPER_ID)),
                    assignedHelperName = c.getString(c.getColumnIndexOrThrow(COL_ASSIGNED_HELPER_NAME))
                )
            }
        }
        return null
    }

    fun clear() {
        dbHelper().delete(TABLE_NAME, null, null)
    }
}

