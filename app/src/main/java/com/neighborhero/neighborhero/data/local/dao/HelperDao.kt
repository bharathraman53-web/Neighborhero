package com.neighborhero.neighborhero.data.local.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.neighborhero.neighborhero.data.model.HelperProfile

class HelperDao(private val dbHelper: () -> SQLiteDatabase) {

    companion object {
        const val TABLE_NAME = "cached_helpers"
        const val COL_ID = "id"
        const val COL_NAME = "name"
        const val COL_TITLE = "title"
        const val COL_NEIGHBORHOOD = "neighborhood"
        const val COL_DISTANCE = "distance"
        const val COL_RATING = "rating"
        const val COL_REVIEW_COUNT = "review_count"
        const val COL_TASKS_DONE = "tasks_done"
        const val COL_ON_TIME_PERCENTAGE = "on_time_percentage"
        const val COL_BIO = "bio"
        const val COL_SKILLS = "skills"
        const val COL_IS_VERIFIED = "is_verified"
        const val COL_IS_AVAILABLE_TODAY = "is_available_today"
        const val COL_AVATAR_URL = "avatar_url"

        const val CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                $COL_ID TEXT PRIMARY KEY,
                $COL_NAME TEXT NOT NULL,
                $COL_TITLE TEXT NOT NULL,
                $COL_NEIGHBORHOOD TEXT NOT NULL,
                $COL_DISTANCE TEXT NOT NULL,
                $COL_RATING REAL NOT NULL,
                $COL_REVIEW_COUNT INTEGER NOT NULL,
                $COL_TASKS_DONE INTEGER NOT NULL,
                $COL_ON_TIME_PERCENTAGE INTEGER NOT NULL,
                $COL_BIO TEXT NOT NULL,
                $COL_SKILLS TEXT NOT NULL,
                $COL_IS_VERIFIED INTEGER NOT NULL,
                $COL_IS_AVAILABLE_TODAY INTEGER NOT NULL,
                $COL_AVATAR_URL TEXT NOT NULL
            )
        """
    }

    fun insertOrUpdate(helper: HelperProfile) {
        val db = dbHelper()
        val values = ContentValues().apply {
            put(COL_ID, helper.id)
            put(COL_NAME, helper.name)
            put(COL_TITLE, helper.title)
            put(COL_NEIGHBORHOOD, helper.neighborhood)
            put(COL_DISTANCE, helper.distance)
            put(COL_RATING, helper.rating)
            put(COL_REVIEW_COUNT, helper.reviewCount)
            put(COL_TASKS_DONE, helper.tasksDone)
            put(COL_ON_TIME_PERCENTAGE, helper.onTimePercentage)
            put(COL_BIO, helper.bio)
            put(COL_SKILLS, helper.skills.joinToString(","))
            put(COL_IS_VERIFIED, if (helper.isVerified) 1 else 0)
            put(COL_IS_AVAILABLE_TODAY, if (helper.isAvailableToday) 1 else 0)
            put(COL_AVATAR_URL, helper.avatarUrl)
        }
        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun insertAll(helpers: List<HelperProfile>) {
        val db = dbHelper()
        db.beginTransaction()
        try {
            for (h in helpers) {
                insertOrUpdate(h)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getAll(): List<HelperProfile> {
        val db = dbHelper()
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, "$COL_RATING DESC")
        val list = mutableListOf<HelperProfile>()
        cursor.use { c ->
            while (c.moveToNext()) {
                val skillsStr = c.getString(c.getColumnIndexOrThrow(COL_SKILLS))
                val skills = if (skillsStr.isBlank()) emptyList() else skillsStr.split(",")
                list.add(
                    HelperProfile(
                        id = c.getString(c.getColumnIndexOrThrow(COL_ID)),
                        name = c.getString(c.getColumnIndexOrThrow(COL_NAME)),
                        title = c.getString(c.getColumnIndexOrThrow(COL_TITLE)),
                        neighborhood = c.getString(c.getColumnIndexOrThrow(COL_NEIGHBORHOOD)),
                        distance = c.getString(c.getColumnIndexOrThrow(COL_DISTANCE)),
                        rating = c.getFloat(c.getColumnIndexOrThrow(COL_RATING)),
                        reviewCount = c.getInt(c.getColumnIndexOrThrow(COL_REVIEW_COUNT)),
                        tasksDone = c.getInt(c.getColumnIndexOrThrow(COL_TASKS_DONE)),
                        onTimePercentage = c.getInt(c.getColumnIndexOrThrow(COL_ON_TIME_PERCENTAGE)),
                        bio = c.getString(c.getColumnIndexOrThrow(COL_BIO)),
                        skills = skills,
                        isVerified = c.getInt(c.getColumnIndexOrThrow(COL_IS_VERIFIED)) == 1,
                        isAvailableToday = c.getInt(c.getColumnIndexOrThrow(COL_IS_AVAILABLE_TODAY)) == 1,
                        avatarUrl = c.getString(c.getColumnIndexOrThrow(COL_AVATAR_URL))
                    )
                )
            }
        }
        return list
    }

    fun getById(id: String): HelperProfile? {
        val db = dbHelper()
        val cursor = db.query(TABLE_NAME, null, "$COL_ID = ?", arrayOf(id), null, null, null)
        cursor.use { c ->
            if (c.moveToFirst()) {
                val skillsStr = c.getString(c.getColumnIndexOrThrow(COL_SKILLS))
                val skills = if (skillsStr.isBlank()) emptyList() else skillsStr.split(",")
                return HelperProfile(
                    id = c.getString(c.getColumnIndexOrThrow(COL_ID)),
                    name = c.getString(c.getColumnIndexOrThrow(COL_NAME)),
                    title = c.getString(c.getColumnIndexOrThrow(COL_TITLE)),
                    neighborhood = c.getString(c.getColumnIndexOrThrow(COL_NEIGHBORHOOD)),
                    distance = c.getString(c.getColumnIndexOrThrow(COL_DISTANCE)),
                    rating = c.getFloat(c.getColumnIndexOrThrow(COL_RATING)),
                    reviewCount = c.getInt(c.getColumnIndexOrThrow(COL_REVIEW_COUNT)),
                    tasksDone = c.getInt(c.getColumnIndexOrThrow(COL_TASKS_DONE)),
                    onTimePercentage = c.getInt(c.getColumnIndexOrThrow(COL_ON_TIME_PERCENTAGE)),
                    bio = c.getString(c.getColumnIndexOrThrow(COL_BIO)),
                    skills = skills,
                    isVerified = c.getInt(c.getColumnIndexOrThrow(COL_IS_VERIFIED)) == 1,
                    isAvailableToday = c.getInt(c.getColumnIndexOrThrow(COL_IS_AVAILABLE_TODAY)) == 1,
                    avatarUrl = c.getString(c.getColumnIndexOrThrow(COL_AVATAR_URL))
                )
            }
        }
        return null
    }

    fun clear() {
        dbHelper().delete(TABLE_NAME, null, null)
    }
}

