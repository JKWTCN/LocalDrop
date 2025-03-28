// FileSender.kt
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import com.google.protobuf.ByteString
import com.icloudwar.localdrop.FileInfo
import com.icloudwar.localdrop.FileTransferOuterClass
import com.icloudwar.localdrop.FileType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket
import kotlin.random.Random

class FileSender(private val host: String, private val port: Int) {

    fun show_log(msg: String) {
        Log.i("FileSender", msg)
    }

    fun sendFiles(waitSendFiles: List<FileInfo>) {
        Socket(host, port).use { socket ->
            val output = socket.getOutputStream()
            val input = socket.getInputStream()

            for (i in waitSendFiles) {
                show_log("send ${i.fileName},size:${i.fileSize} info ${i.info}")
                val send_bytes = FileTransferOuterClass.FileTransfer.newBuilder().apply {
                    fileName = i.fileName
                    fileSize = i.fileSize
                    info = i.info
                    raw = ByteString.copyFrom(i.raw)
                    fileType = FileType.TEXT.ordinal
                }.build().toByteArray()
                output.write(send_bytes)
                show_log("发送${i.fileName}")
            }
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





