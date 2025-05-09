package com.icloudwar.localdrop// FileInfo.kt
import java.io.Serializable

data class FileInfo(
    val fileName: String,
    val fileSize: Long,
    val fileType: FileType,
    var info: String,
    val raw: ByteArray?,
) : Serializable

enum class FileType {
    TEXT, IMG, FILE, AUDIO, VIDEO, DIR, QUICK_MESSAGE
}