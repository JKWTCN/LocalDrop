package com.icloudwar.localdrop.fileHistory

// FileHistoryDbHelper.kt
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class FileHistoryDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "file_history.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_HISTORY = "history"
        const val COLUMN_ID = "_id"
        const val COLUMN_FILE_NAME = "file_name"
        const val COLUMN_FILE_PATH = "file_path"
        const val COLUMN_FILE_SIZE = "file_size"
        const val COLUMN_FILE_TYPE = "file_type"
        const val COLUMN_INFO = "info"
        const val COLUMN_RECEIVED_TIME = "received_time"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_HISTORY (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_FILE_NAME TEXT NOT NULL,
                $COLUMN_FILE_PATH TEXT,
                $COLUMN_FILE_SIZE INTEGER NOT NULL,
                $COLUMN_FILE_TYPE TEXT NOT NULL,
                $COLUMN_INFO TEXT,
                $COLUMN_RECEIVED_TIME INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HISTORY")
        onCreate(db)
    }
}


