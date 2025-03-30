// FileSender.kt
import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.protobuf.ByteString
import com.icloudwar.localdrop.FileInfo
import com.icloudwar.localdrop.FileTransferOuterClass
import com.icloudwar.localdrop.FileType
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStreamWriter
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class FileSender(private val host: String, private val port: Int, private val port2: Int) {

    private fun showLog(msg: String) {
        Log.i("FileSender", msg)
    }


    fun sendBigFile(file: FileInfo, context: Context) {
        Socket(host, port2).use { socket ->
            val output = socket.getOutputStream()
            val writer = OutputStreamWriter(output, StandardCharsets.UTF_8)

            // [4字节JSON长度头][JSON元数据][文件数据]

            // 构建元数据JSON
            val metaJson = buildJsonObject {
                put("fileName", file.fileName)
                put("fileSize", file.fileSize)
                put("fileType", file.fileType.name)
                put("info", "")
            }.toString()

            // 发送元数据长度（4字节）

            val metaBytes = metaJson.toByteArray(StandardCharsets.UTF_8)
            output.write(
                ByteBuffer.allocate(4)
                    .putInt(metaBytes.size)
                    .array()
            )

            // 发送元数据内容
            output.write(metaBytes)
            output.flush()

            if (file.fileType == FileType.BIG_FILE) {
                // 分块发送文件内容
                val uri = Uri.parse(file.info)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                    output.flush()
                }

            }
        }
    }

    fun sendFile(waitSendFile: FileInfo) {
        Socket(host, port).use { socket ->
            val output = socket.getOutputStream()
            val input = socket.getInputStream()
            showLog("send ${waitSendFile.fileName},size:${waitSendFile.fileSize} info ${waitSendFile.info}")
            val sendBytes = FileTransferOuterClass.FileTransfer.newBuilder().apply {
                fileName = waitSendFile.fileName
                fileSize = waitSendFile.fileSize
                info = waitSendFile.info
                raw = ByteString.copyFrom(waitSendFile.raw)
                fileType = FileType.entries.indexOf(waitSendFile.fileType)
            }.build().toByteArray()
            output.write(sendBytes)
            showLog("发送${waitSendFile.fileName}")
        }
    }


    fun sendMessage(message: String) {
        Socket(host, port).use { socket ->
            val output = ObjectOutputStream(socket.getOutputStream())
            val input = ObjectInputStream(socket.getInputStream())
            val fileTransfer = FileInfo(
                fileName = "test",
                fileSize = 1024,
                info = "hello world",
                raw = ByteArray(1024),
                fileType = FileType.TEXT
            )
            output.writeObject(fileTransfer)
        }
    }
}





