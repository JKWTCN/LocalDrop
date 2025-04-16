package com.icloudwar.localdrop.receiver// com.icloudwar.localdrop.receiver.FileReceiver.kt
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.icloudwar.localdrop.FileInfo
import com.icloudwar.localdrop.FileType
import com.icloudwar.localdrop.fileHistory.FileHistoryManager
import com.icloudwar.localdrop.setting.MySettings
import org.json.JSONObject
import java.io.*
import java.lang.ref.WeakReference
import java.net.ServerSocket
import java.net.SocketException
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

class FileReceiver(public var port: Int) {
    private var bigStartT = Thread()
    private var bigSocket = ServerSocket()
    private var historyManager: FileHistoryManager? = null

    // 进度回调接口
    interface ProgressCallback {
        fun onStart(fileName: String)
        fun onProgress(progress: Int)
        fun onComplete()
    }

    private var progressCallback: ProgressCallback? = null

    // 添加设置回调的方法
    fun setProgressCallback(callback: ProgressCallback) {
        this.progressCallback = callback
    }

    private fun showLog(msg: String) {
        Log.i("FileReceiver", msg)
    }

    fun stop() {
        bigSocket.close()
        bigStartT.interrupt()
    }

    fun getNowTimeString() {
        val currentTimeMillis: Long = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        dateFormat.format(Date(currentTimeMillis))
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun start(context: Context, historyManager: FileHistoryManager) {
        this.historyManager = historyManager
        bigStartT = Thread { fileHandleClient(context) }
        bigStartT.start()
    }

    private fun fileHandleClient(context: Context) {
        ServerSocket(port).use { serverSocket ->
            while (true) {
                try {
                    bigSocket = serverSocket
                    val socket = serverSocket.accept()
                    socket.use { s ->
                        val input = s.getInputStream()
                        val reader = InputStreamReader(input, StandardCharsets.UTF_8)
                        // 读取元数据长度
                        val lengthBytes = ByteArray(4)
                        input.read(lengthBytes, 0, 4)
                        val metaLength = ByteBuffer.wrap(lengthBytes).int
                        // 读取元数据内容
                        val metaBytes = ByteArray(metaLength)
                        input.read(metaBytes)
                        val metaJson = JSONObject(metaBytes.toString(StandardCharsets.UTF_8))
                        val fileInfo = FileInfo(
                            fileName = metaJson.getString("fileName"),
                            fileSize = metaJson.getLong("fileSize"),
                            fileType = FileType.valueOf(metaJson.getString("fileType")),
                            info = metaJson.getString("info"),
                            raw = null
                        )

                        // 通知开始接收
                        progressCallback?.onStart(fileInfo.fileName)
                        if (fileInfo.fileType != FileType.QUICK_MESSAGE) {// 处理文件名
                            val ext = fileInfo.fileName.substringAfterLast('.', "")
                            val (baseName, extension) = Pair(
                                fileInfo.fileName.substringBeforeLast(
                                    '.'
                                ), ext
                            )
                            // todo 读取设置
                            val mySettings = MySettings(context)
                            val saveToPictures = mySettings.getSaveToPictures()
                            val storageDir = if (saveToPictures) {
                                File(
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                                    "LocalDrop"
                                ).apply { if (!exists()) mkdirs() }
                            } else {
                                File(
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                                    "LocalDrop"
                                ).apply { if (!exists()) mkdirs() }
                            }
                            // 生成唯一文件名
                            var counter = 0
                            var targetFile: File
                            do {
                                val suffix = if (counter == 0) "" else "_$counter"
                                val fileName = buildString {
                                    append(baseName)
                                    append(suffix)
                                    if (extension.isNotEmpty()) append(".$extension")
                                }
                                targetFile = File(storageDir, fileName)
                                counter++
                            } while (targetFile.exists())

                            FileOutputStream(targetFile).use { fos ->
                                val buffer = ByteArray(8192)
                                var remaining = fileInfo.fileSize
                                var totalRead: Long = 0
                                // 分块接收文件内容
                                while (remaining > 0) {
                                    val readSize = min(
                                        remaining, buffer.size.toLong()
                                    ).toInt()
                                    val bytesRead = input.read(
                                        buffer, 0, readSize
                                    )
                                    if (bytesRead == -1) break
                                    fos.write(buffer, 0, bytesRead)
                                    remaining -= bytesRead
                                    totalRead += bytesRead

                                    // 计算并更新进度
                                    val progress = (totalRead * 100 / fileInfo.fileSize).toInt()
                                    progressCallback?.onProgress(progress)
                                }
                                fos.flush()
                                // 添加历史记录
                                historyManager?.addHistory(fileInfo, targetFile.absolutePath)
                                showLog("${fileInfo.fileName} saved")
                            }
                        } else {
                            historyManager?.addHistory(fileInfo, null)
                            showLog("rev:${fileInfo.info}")
                        }
                        // 通知接收完成
                        progressCallback?.onComplete()
                    }
                } catch (e: InterruptedException) {
                    showLog("fileHandleClient已退出")
                    return
                } catch (e: SocketException) {
                    showLog("fileHandleClient已退出")
                    return
                }
            }
        }
    }


}