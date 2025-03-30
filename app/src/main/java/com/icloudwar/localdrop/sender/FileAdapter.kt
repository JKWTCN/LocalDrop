package com.icloudwar.localdrop.sender

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.icloudwar.localdrop.FileInfo
import com.icloudwar.localdrop.FileType
import com.icloudwar.localdrop.R

class FileAdapter(
    private val files: MutableList<FileInfo>,
    private val onClick: (FileInfo) -> Unit
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    inner class FileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
        private val tvFileName: TextView = view.findViewById(R.id.tvFileName)
        private val tvFileSize: TextView = view.findViewById(R.id.tvFileSize)

        fun bind(file: FileInfo) {
            tvFileName.text = file.fileName
            tvFileSize.text = formatFileSize(file.fileSize)

            // 根据文件类型设置图标
            ivIcon.setImageResource(
                when (file.fileType) {
                    FileType.TEXT -> R.drawable.text
                    FileType.IMG -> R.drawable.img
                    FileType.FILE -> R.drawable.file
                    FileType.AUDIO -> R.drawable.file
                    FileType.VIDEO -> R.drawable.file
                    FileType.DIR -> R.drawable.file
                    FileType.BIG_FILE -> R.drawable.file
                }
            )

            itemView.setOnClickListener { onClick(file) }
        }

        private fun formatFileSize(size: Long): String {
            // 添加你的文件大小格式化逻辑
            return if (size < 1024) "$size B" else "${size / 1024} KB"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(files[position])
    }

    override fun getItemCount() = files.size
}