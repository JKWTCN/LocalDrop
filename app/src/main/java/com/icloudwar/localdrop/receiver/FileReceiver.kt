package com.icloudwar.localdrop.receiver// com.icloudwar.localdrop.receiver.FileReceiver.kt
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import com.icloudwar.localdrop.FileInfo
import com.icloudwar.localdrop.FileType
import org.json.JSONObject
import java.io.*
import java.net.ServerSocket
import java.net.SocketException
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import kotlin.math.min

class FileReceiver(private val port: Int) {
    private var bigStartT = Thread()
    private var bigSocket = ServerSocket()

    private fun showLog(msg: String) {
        Log.i("FileReceiver", msg)
    }

    fun stop() {
        bigSocket.close()
        bigStartT.interrupt()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun start() {
        bigStartT = Thread { fileHandleClient() }
        bigStartT.start()
    }

    private fun fileHandleClient() {
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
                        if (fileInfo.fileType != FileType.QUICK_MESSAGE) {// 处理文件名
                            val ext = fileInfo.fileName.substringAfterLast('.', "")
                            val (baseName, extension) = Pair(
                                fileInfo.fileName.substringBeforeLast(
                                    '.'
                                ), ext
                            )
                            val storageDir = File(
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                                "LocalDrop"
                            ).apply {
                                if (!exists()) mkdirs()
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
                                }
                                fos.flush()
                                showLog("${fileInfo.fileName} saved")
                            }
                        } else {
                            showLog("rev:${fileInfo.info}")
                        }

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