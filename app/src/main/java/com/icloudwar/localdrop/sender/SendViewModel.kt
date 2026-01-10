package com.icloudwar.localdrop.sender

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.icloudwar.localdrop.FileInfo

class SendViewModel : ViewModel() {
    // 使用 MutableLiveData 来保存待发送的文件列表
    private val _waitSendFiles = MutableLiveData<MutableList<FileInfo>>()
    val waitSendFiles: LiveData<MutableList<FileInfo>> = _waitSendFiles

    init {
        // 初始化空列表
        _waitSendFiles.value = mutableListOf()
    }

    // 添加文件到列表
    fun addFile(file: FileInfo) {
        val currentList = _waitSendFiles.value ?: mutableListOf()
        currentList.add(file)
        _waitSendFiles.value = currentList.toMutableList() // 创建新列表以触发 LiveData 更新
    }

    // 移除指定位置的文件
    fun removeFile(position: Int) {
        val currentList = _waitSendFiles.value ?: mutableListOf()
        if (position in currentList.indices) {
            currentList.removeAt(position)
            _waitSendFiles.value = currentList.toMutableList() // 创建新列表以触发 LiveData 更新
        }
    }

    // 清空列表
    fun clearFiles() {
        _waitSendFiles.value = mutableListOf()
    }

    // 获取文件列表大小
    fun getFileCount(): Int {
        return _waitSendFiles.value?.size ?: 0
    }

    // 获取文件列表
    fun getFileList(): MutableList<FileInfo> {
        return _waitSendFiles.value ?: mutableListOf()
    }

    // 检查列表是否为空
    fun isEmpty(): Boolean {
        return _waitSendFiles.value?.isEmpty() ?: true
    }
}
