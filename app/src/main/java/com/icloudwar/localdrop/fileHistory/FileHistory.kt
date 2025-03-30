package com.icloudwar.localdrop.fileHistory

// FileHistory.kt
import com.icloudwar.localdrop.FileType
import java.io.Serializable

data class FileHistory(
    val id: Long = 0,
    val fileName: String,
    val filePath: String?,
    val fileSize: Long,
    val fileType: FileType,
    val info: String?,
    val receivedTime: Long = System.currentTimeMillis()
) : Serializable



