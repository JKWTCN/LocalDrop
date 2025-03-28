package com.icloudwar.localdrop.receiver// com.icloudwar.localdrop.receiver.FileReceiver.kt
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import com.icloudwar.localdrop.FileInfo
import com.icloudwar.localdrop.FileTransferOuterClass
import com.icloudwar.localdrop.FileType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import kotlin.math.log

class FileReceiver(private val port: Int, private val saveDir: File) {
    var start_t = Thread()
    var nowSocket = ServerSocket()
    fun show_log(msg: String) {
        Log.i("FileReceiver", msg)
    }

    fun stop() {
        nowSocket.close()
        start_t.interrupt()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun start() {
        saveDir.mkdirs()
        start_t = Thread { handleClient() }
        start_t.start()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun handleClient() {
        ServerSocket(port).use { clientSocket ->
            while (true) {
                try {
                    nowSocket = clientSocket
                    val socket = clientSocket.accept()
                    show_log("rev successfully")
                    val output = socket.getOutputStream()
                    val input = socket.getInputStream()
                    val FileTransfer: FileTransferOuterClass.FileTransfer =
                        FileTransferOuterClass.FileTransfer.parseFrom(input.readAllBytes())
                    show_log("file name:${FileTransfer.fileName} file size:${FileTransfer.fileSize} file type:${FileTransfer.fileType} file info:${FileTransfer.info}")
                    val fileInfo = FileInfo(
                        fileName = FileTransfer.fileName,
                        fileSize = FileTransfer.fileSize,
                        fileType = FileType.entries[FileTransfer.fileType],
                        info = FileTransfer.info,
                        raw = FileTransfer.raw.toByteArray()
                    )
                    //     todo 存储FileInfo对象
                    // 异步处理文件存储
                    CoroutineScope(Dispatchers.IO).launch {
                        val savedFile = saveFileWithRetry(fileInfo)
                        savedFile?.let {
                            withContext(Dispatchers.Main) {
                                show_log("文件保存成功：${it.absolutePath}")
                            }
                        } ?: run {
                            withContext(Dispatchers.Main) {
                                show_log("文件保存失败")
                            }
                        }
                    }
                } catch (e: InterruptedException) {
                    show_log("handleClient已退出")
                    return
                } catch (e: SocketException) {
                    show_log("handleClient已退出")
                    return
                }
            }
        }
    }

    private fun saveFileWithRetry(fileInfo: FileInfo): File? {
        return try {
            // 获取存储目录
            val storageDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "LocalDrop"
            ).apply {
                if (!exists()) mkdirs()
            }

            // 处理文件名
            val (baseName, extension) = when (fileInfo.fileType) {
                FileType.TEXT -> {
                    val nameWithoutExt = fileInfo.fileName.substringBeforeLast('.')
                    Pair(nameWithoutExt, "txt")
                }

                else -> {
                    val ext = fileInfo.fileName.substringAfterLast('.', "")
                    Pair(fileInfo.fileName.substringBeforeLast('.'), ext)
                }
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

            // 写入文件内容
            fileInfo.raw?.let { bytes ->
                targetFile.outputStream().use { fos ->
                    fos.write(bytes)
                    fos.flush()
                }
                targetFile
            }
        } catch (e: SecurityException) {
            show_log("存储权限被拒绝: ${e.message}")
            null
        } catch (e: IOException) {
            show_log("文件写入失败: ${e.message}")
            null
        } catch (e: Exception) {
            show_log("未知错误: ${e.message}")
            null
        }
    }
}