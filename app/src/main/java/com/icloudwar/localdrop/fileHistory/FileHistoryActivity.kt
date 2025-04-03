package com.icloudwar.localdrop.fileHistory

// FileHistoryActivity.kt
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.icloudwar.localdrop.FileType.AUDIO
import com.icloudwar.localdrop.FileType.DIR
import com.icloudwar.localdrop.FileType.FILE
import com.icloudwar.localdrop.FileType.IMG
import com.icloudwar.localdrop.FileType.QUICK_MESSAGE
import com.icloudwar.localdrop.FileType.TEXT
import com.icloudwar.localdrop.FileType.VIDEO
import com.icloudwar.localdrop.R
import java.io.File


class FileHistoryActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var adapter: HistoryAdapter
    private lateinit var historyManager: FileHistoryManager
    private var historyList = mutableListOf<FileHistory>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_history)
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        builder.detectFileUriExposure()

        historyManager = FileHistoryManager(this)
        listView = findViewById(R.id.historyListView)
        val emptyView = findViewById<TextView>(R.id.emptyView)

        // 设置空视图
        listView.emptyView = emptyView

        // 初始化适配器
        adapter = HistoryAdapter(historyList)
        listView.adapter = adapter

        // 加载数据
        loadHistories()

        // 点击事件处理
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val history = historyList[position]
            handleItemClick(history)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun loadHistories() {
        historyList.clear()
        historyList.addAll(historyManager.getAllHistories().subList(0, 20))
        adapter.notifyDataSetChanged()
    }

    private fun refreshData() {
        historyList.clear()
        historyList.addAll(historyManager.getAllHistories())
        adapter.notifyDataSetChanged()
    }

    private fun handleItemClick(history: FileHistory) {
        when (history.fileType) {
            QUICK_MESSAGE -> showQuickMessage(history)
            else -> openFile(history)
        }
    }

    private fun openFile(history: FileHistory) {
        val file = history.filePath?.let { File(it) }
        if (file?.exists() == true) {
            val intent = Intent(Intent.ACTION_VIEW)
            val uri = Uri.fromFile(file)
            val mimeType = when (history.fileType) {
                IMG -> "image/*"
                TEXT -> "text/*"
                AUDIO -> "video/*"
                else -> "*/*"
            }
            intent.setDataAndType(uri, mimeType)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(intent, "打开文件"))
        } else {
            showFileNotFoundDialog(history)
        }
    }

    private fun showQuickMessage(history: FileHistory) {
        AlertDialog.Builder(this)
            .setTitle("快速消息")
            .setMessage(history.info ?: "无内容")
            .setPositiveButton("确定", null)
            .show()
    }

    private fun showFileNotFoundDialog(history: FileHistory) {
        AlertDialog.Builder(this)
            .setTitle("文件不存在")
            .setMessage("文件可能已被删除，是否删除该记录？")
            .setPositiveButton("删除") { _, _ ->
                historyManager.deleteHistory(history.id)
                refreshData()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private inner class HistoryAdapter(private val data: List<FileHistory>) : BaseAdapter() {

        override fun getCount(): Int = data.size

        override fun getItem(position: Int): FileHistory = data[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_file_history, parent, false)

            val history = getItem(position)
            view.findViewById<TextView>(R.id.tvFileName).text = history.fileName
            view.findViewById<TextView>(R.id.tvFileSize).text = formatFileSize(history.fileSize)
            view.findViewById<TextView>(R.id.tvReceivedTime).text =
                android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", history.receivedTime)

            // 显示文件状态图标
            val ivStatus = view.findViewById<ImageView>(R.id.ivStatus)
            when {
                history.fileType == QUICK_MESSAGE ->
                    ivStatus.setImageResource(R.drawable.ic_message)

                File(history.filePath ?: "").exists() -> {

                    when (history.fileType) {
                        FILE -> ivStatus.setImageResource(R.drawable.file)
                        TEXT -> ivStatus.setImageResource(R.drawable.text)
                        IMG -> ivStatus.setImageResource(R.drawable.img)
                        AUDIO -> ivStatus.setImageResource(R.drawable.ic_audio)
                        VIDEO -> ivStatus.setImageResource(R.drawable.ic_video)
                        DIR -> ivStatus.setImageResource(R.drawable.file)
                        QUICK_MESSAGE -> ivStatus.setImageResource(R.drawable.ic_message)
                    }
                }

                else ->
                    ivStatus.setImageResource(R.drawable.ic_file_miss)
            }

            return view
        }

        private fun formatFileSize(size: Long): String {
            return when {
                size >= 1_000_000_000 -> "${size / 1_000_000_000} GB"
                size >= 1_000_000 -> "${size / 1_000_000} MB"
                size >= 1_000 -> "${size / 1_000} KB"
                else -> "$size B"
            }
        }
    }
}