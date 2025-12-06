package com.uh.smdprojectsoulwrite

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "SoulWrite.db"

        // Table names
        private const val TABLE_JOURNALS = "journals"
        private const val TABLE_USERS = "users"

        // Journal columns
        private const val KEY_ID = "id"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_TITLE = "title"
        private const val KEY_CONTENT = "content"
        private const val KEY_IMAGE_URL = "image_url"
        private const val KEY_THUMBNAIL_URL = "thumbnail_url"
        private const val KEY_DATE = "date"
        private const val KEY_SYNCED = "synced"

        // User columns
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_PHONE = "phone"
        private const val KEY_PROFILE_IMAGE = "profile_image"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createJournalsTable = """CREATE TABLE $TABLE_JOURNALS (
            $KEY_ID TEXT PRIMARY KEY,
            $KEY_USER_ID TEXT,
            $KEY_TITLE TEXT,
            $KEY_CONTENT TEXT,
            $KEY_IMAGE_URL TEXT,
            $KEY_THUMBNAIL_URL TEXT,
            $KEY_DATE INTEGER,
            $KEY_SYNCED INTEGER DEFAULT 0
        )"""

        val createUsersTable = """CREATE TABLE $TABLE_USERS (
            $KEY_ID TEXT PRIMARY KEY,
            $KEY_NAME TEXT,
            $KEY_EMAIL TEXT,
            $KEY_PHONE TEXT,
            $KEY_PROFILE_IMAGE TEXT
        )"""

        db?.execSQL(createJournalsTable)
        db?.execSQL(createUsersTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_JOURNALS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    fun insertJournal(journal: Journal): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_ID, journal.id)
            put(KEY_USER_ID, journal.userId)
            put(KEY_TITLE, journal.title)
            put(KEY_CONTENT, journal.content)
            put(KEY_IMAGE_URL, journal.imageUrl)
            put(KEY_THUMBNAIL_URL, journal.thumbnailUrl)
            put(KEY_DATE, journal.date)
            put(KEY_SYNCED, 0)
        }
        return db.insertWithOnConflict(TABLE_JOURNALS, null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getAllJournals(): List<Journal> {
        val journals = mutableListOf<Journal>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_JOURNALS ORDER BY $KEY_DATE DESC", null)

        if (cursor.moveToFirst()) {
            do {
                val journal = Journal(
                    id = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID)),
                    userId = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE)),
                    content = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CONTENT)),
                    imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(KEY_IMAGE_URL)),
                    thumbnailUrl = cursor.getString(cursor.getColumnIndexOrThrow(KEY_THUMBNAIL_URL)),
                    date = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_DATE))
                )
                journals.add(journal)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return journals
    }

    fun updateJournal(journal: Journal): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_TITLE, journal.title)
            put(KEY_CONTENT, journal.content)
            put(KEY_IMAGE_URL, journal.imageUrl)
            put(KEY_THUMBNAIL_URL, journal.thumbnailUrl)
        }
        return db.update(TABLE_JOURNALS, values, "$KEY_ID = ?", arrayOf(journal.id))
    }

    fun deleteJournal(journalId: String): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_JOURNALS, "$KEY_ID = ?", arrayOf(journalId))
    }

    fun insertUser(user: User): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_ID, user.id)
            put(KEY_NAME, user.name)
            put(KEY_EMAIL, user.email)
            put(KEY_PHONE, user.phone)
            put(KEY_PROFILE_IMAGE, user.profileImageUrl)
        }
        return db.insert(TABLE_USERS, null, values)
    }

    fun getUser(userId: String): User? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            "$KEY_ID = ?",
            arrayOf(userId),
            null,
            null,
            null
        )

        var user: User? = null
        if (cursor.moveToFirst()) {
            user = User(
                id = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMAIL)),
                phone = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PHONE)),
                profileImageUrl = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_IMAGE))
            )
        }
        cursor.close()
        return user
    }

    fun getUserJournals(userId: String): List<Journal> {
        val journals = mutableListOf<Journal>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_JOURNALS WHERE $KEY_USER_ID = ? ORDER BY $KEY_DATE DESC",
            arrayOf(userId)
        )

        if (cursor.moveToFirst()) {
            do {
                val journal = Journal(
                    id = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID)),
                    userId = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE)),
                    content = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CONTENT)),
                    imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(KEY_IMAGE_URL)),
                    thumbnailUrl = cursor.getString(cursor.getColumnIndexOrThrow(KEY_THUMBNAIL_URL)),
                    date = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_DATE))
                )
                journals.add(journal)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return journals
    }

    fun clearAllJournals() {
        val db = this.writableDatabase
        db.delete(TABLE_JOURNALS, null, null)
        android.util.Log.d("DatabaseHelper", "Cleared all journals from local database")
    }
}

