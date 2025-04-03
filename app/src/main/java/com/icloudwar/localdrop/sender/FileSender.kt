package com.icloudwar.localdrop.sender

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.icloudwar.localdrop.FileInfo
import com.icloudwar.localdrop.FileType
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.OutputStreamWriter
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class FileSender(private val host: String, private val port: Int) {

    private fun showLog(msg: String) {
        Log.i("FileSender", msg)
    }

    private fun showToast(message: String, context: Context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    interface ProgressCallback {
        fun onProgress(progress: Int)
    }

    private var progressCallback: ProgressCallback? = null

    // 设置回调
    fun setProgressCallback(callback: ProgressCallback) {
        this.progressCallback = callback
    }

    fun sendFile(file: FileInfo, context: Context) {
        Socket(host, port).use { socket ->
            val output = socket.getOutputStream()
            val writer = OutputStreamWriter(output, StandardCharsets.UTF_8)
            var nowInfo = ""
            if (file.fileType == FileType.QUICK_MESSAGE)
                nowInfo = file.info
            // [4字节JSON长度头][JSON元数据][文件数据]
            // 构建元数据JSON
            val metaJson = buildJsonObject {
                put("fileName", file.fileName)
                put("fileSize", file.fileSize)
                put("fileType", file.fileType.name)
                put("info", nowInfo)
            }.toString()
            // 发送元数据长度（4字节）
            val metaBytes = metaJson.toByteArray(StandardCharsets.UTF_8)
            output.write(
                ByteBuffer.allocate(4).putInt(metaBytes.size).array()
            )
            // 发送元数据内容
            output.write(metaBytes)
            output.flush()
            showLog("begin send ${file.fileName}")
            // 分块发送文件内容
            if (file.fileType != FileType.QUICK_MESSAGE) {
                val uri = Uri.parse(file.info)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalSent: Long = 0
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                    // 计算并更新进度
                    val progress = (totalSent * 100 / file.fileSize).toInt()
                    progressCallback?.onProgress(progress)
                    output.flush()
                }
            }
            showLog("${file.fileName} sent")
        }
    }
}






