package com.icloudwar.localdrop.fileHistory

// FileHistoryManager.kt
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.icloudwar.localdrop.FileInfo
import com.icloudwar.localdrop.FileType

class FileHistoryManager(private val context: Context) {

    private val dbHelper = FileHistoryDbHelper(context)

    fun addHistory(fileInfo: FileInfo, filePath: String?): Long {
        val db = dbHelper.writableDatabase
        return db.insertWithOnConflict(
            FileHistoryDbHelper.TABLE_HISTORY,
            null,
            createContentValues(fileInfo, filePath),
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun deleteHistory(id: Long): Boolean {
        val db = dbHelper.writableDatabase
        return db.delete(
            FileHistoryDbHelper.TABLE_HISTORY,
            "${FileHistoryDbHelper.COLUMN_ID} = ?",
            arrayOf(id.toString())
        ) > 0
    }

    fun updateHistoryInfo(id: Long, newInfo: String): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(FileHistoryDbHelper.COLUMN_INFO, newInfo)
        }
        return db.update(
            FileHistoryDbHelper.TABLE_HISTORY,
            values,
            "${FileHistoryDbHelper.COLUMN_ID} = ?",
            arrayOf(id.toString())
        ) > 0
    }

    fun getAllHistories(): List<FileHistory> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            FileHistoryDbHelper.TABLE_HISTORY,
            null, null, null, null, null,
            "${FileHistoryDbHelper.COLUMN_RECEIVED_TIME} DESC"
        )
        return parseHistories(cursor)
    }

    fun getHistoryById(id: Long): FileHistory? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            FileHistoryDbHelper.TABLE_HISTORY,
            null,
            "${FileHistoryDbHelper.COLUMN_ID} = ?",
            arrayOf(id.toString()),
            null, null, null
        )
        return parseHistories(cursor).firstOrNull()
    }

    private fun createContentValues(fileInfo: FileInfo, filePath: String?): ContentValues {
        return ContentValues().apply {
            put(FileHistoryDbHelper.COLUMN_FILE_NAME, fileInfo.fileName)
            put(FileHistoryDbHelper.COLUMN_FILE_PATH, filePath)
            put(FileHistoryDbHelper.COLUMN_FILE_SIZE, fileInfo.fileSize)
            put(FileHistoryDbHelper.COLUMN_FILE_TYPE, fileInfo.fileType.name)
            put(FileHistoryDbHelper.COLUMN_INFO, fileInfo.info)
            put(FileHistoryDbHelper.COLUMN_RECEIVED_TIME, System.currentTimeMillis())
        }
    }

    @SuppressLint("Range")
    private fun parseHistories(cursor: Cursor): List<FileHistory> {
        val histories = mutableListOf<FileHistory>()
        with(cursor) {
            while (moveToNext()) {
                histories.add(
                    FileHistory(
                        id = getLong(getColumnIndex(FileHistoryDbHelper.COLUMN_ID)),
                        fileName = getString(getColumnIndex(FileHistoryDbHelper.COLUMN_FILE_NAME)),
                        filePath = getString(getColumnIndex(FileHistoryDbHelper.COLUMN_FILE_PATH)),
                        fileSize = getLong(getColumnIndex(FileHistoryDbHelper.COLUMN_FILE_SIZE)),
                        fileType = FileType.valueOf(getString(getColumnIndex(FileHistoryDbHelper.COLUMN_FILE_TYPE))),
                        info = getString(getColumnIndex(FileHistoryDbHelper.COLUMN_INFO)),
                        receivedTime = getLong(getColumnIndex(FileHistoryDbHelper.COLUMN_RECEIVED_TIME))
                    )
                )
            }
        }
        cursor.close()
        return histories
    }
}