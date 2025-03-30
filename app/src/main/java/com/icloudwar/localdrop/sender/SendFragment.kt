package com.icloudwar.localdrop.sender

import FileSender
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.net.wifi.WifiManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatActivity.WIFI_P2P_SERVICE
import androidx.appcompat.app.AppCompatActivity.WIFI_SERVICE
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.icloudwar.localdrop.FileInfo
import com.icloudwar.localdrop.FileType
import com.icloudwar.localdrop.R
import com.icloudwar.localdrop.receiver.DirectActionListener
import kotlin.math.log


class SendFragment : Fragment() {
    // 日志相关
    private fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()

    }

    private fun show_log(message: String) {
        Log.i("SendFragment", message)
    }

    // 视图相关
    private var wifiP2pEnabled = false
    private var wifiP2pInfo: WifiP2pInfo? = null
    private var waitSendFiles = mutableListOf<FileInfo>()
    var wifiP2pDeviceList = mutableListOf<WifiP2pDevice>()
    private val deviceAdapter = DeviceAdapter(wifiP2pDeviceList)
    private var filesAdapter: FileAdapter? = null


    private val recyclerView by lazy {
        activity?.findViewById<RecyclerView>(R.id.horizontalListView)
    }
    private val rvDeviceList by lazy {
        activity?.findViewById<RecyclerView>(R.id.rvDeviceList)
    }
    private val btnScanner by lazy {
        activity?.findViewById<Button>(R.id.btnScanner)
    }
    private val btnSelectFile by lazy {
        activity?.findViewById<Button>(R.id.btnSelectFile)
    }
    private val btnSelectImage by lazy {
        activity?.findViewById<Button>(R.id.btnSelectImage)
    }
    private val btnSendMessage by lazy {
        activity?.findViewById<Button>(R.id.btnSendMessage)
    }
    private val btnDisconnect by lazy {
        activity?.findViewById<Button>(R.id.btnDisconnect)
    }

    @SuppressLint("Range")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun Uri.toFileInfo(context: Context): FileInfo? {
        return try {
            val contentResolver = context.contentResolver
            // 获取文件名和大小
            var fileName = ""
            var fileSize = 0L
            contentResolver.query(this, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    fileSize = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE))
                }
            }
            // 获取MIME类型
            val mimeType = contentResolver.getType(this)?.lowercase() ?: ""
            // 判断文件类型
            val fileType = when {
                mimeType.startsWith("image/") -> FileType.IMG
                mimeType.startsWith("text/") -> FileType.FILE
                mimeType.startsWith("audio/") -> FileType.FILE
                mimeType.startsWith("video/") -> FileType.FILE
                mimeType == "application/octet-stream" -> FileType.FILE
                else -> FileType.FILE
            }
            val rawBytes = if (fileSize < 100 * 1024 * 1024) { // 限制100MB以下文件
                contentResolver.openInputStream(this)?.use { it.readBytes() }
            } else {
                null
            }
            // val rawBy
            // tes = contentResolver.openInputStream(this)?.use { it.readBytes() }
            if (rawBytes != null)
                FileInfo(
                    fileName = fileName,
                    fileSize = fileSize,
                    fileType = fileType,
                    info = "$this",
                    raw = rawBytes
                )
            else {
                FileInfo(
                    fileName = fileName,
                    fileSize = fileSize,
                    fileType = FileType.BIG_FILE,
                    info = "$this",
                    raw = null
                )
            }
        } catch (e: Exception) {
            show_log("Failed to read file: ${e.message}")
            null
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { imageUri ->
            if (imageUri != null) {
                show_log("选择的文件路径：$imageUri")
                for (uri in imageUri) {
                    activity?.applicationContext?.let { it ->
                        uri.toFileInfo(
                            context = it
                        )?.let { waitSendFiles.add(it) }
                    }
                }
            }
            filesAdapter?.notifyDataSetChanged()
        }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val FlierPickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { imageUri ->
            if (imageUri != null) {
                show_log("选择的文件路径：$imageUri")
                for (uri in imageUri) {
                    activity?.applicationContext?.let { it ->
                        uri.toFileInfo(
                            context = it
                        )?.let { waitSendFiles.add(it) }
                    }
                }
            }
            filesAdapter?.notifyDataSetChanged()
        }


    fun getWlanStatus(ctx: Context): Boolean {
        // 从系统服务中获取无线网络管理器
        val wm = ctx.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        return wm.isWifiEnabled
    }

    @SuppressLint("MissingPermission")
    private fun connectAndSendFIles(wifiP2pDevice: WifiP2pDevice) {
        if (wifiP2pDevice.status == WifiP2pDevice.AVAILABLE) {
            val wifiP2pConfig = WifiP2pConfig()
            wifiP2pConfig.deviceAddress = wifiP2pDevice.deviceAddress
            wifiP2pConfig.wps.setup = WpsInfo.PBC
            show_log("正在连接，deviceName: " + wifiP2pDevice.deviceName)
            wifiP2pManager.connect(
                wifiP2pChannel,
                wifiP2pConfig,
                object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        show_log("connect onSuccess")
                    }

                    override fun onFailure(reason: Int) {
                        showToast(message = "连接失败 $reason")
                    }
                })
        } else if (wifiP2pDevice.status == WifiP2pDevice.INVITED || wifiP2pDevice.status == WifiP2pDevice.CONNECTED) {
            val ipAddress = wifiP2pInfo?.groupOwnerAddress?.hostAddress
            show_log("获取到根地址,开始发送 $ipAddress")
            // 客户端发送文件
            // val sender = ipAddress?.let { FileSender(it, 27431) }
            for (waitSendFile in waitSendFiles) {
                val sender = ipAddress?.let { FileSender(it, 27431, 27432) }
                if (sender != null && waitSendFile.fileType != FileType.BIG_FILE) {
                    val t = Thread {
                        sender.sendFile(waitSendFile)
                    }
                    t.start()
                    t.join()
                } else if (sender != null) {
                    val t = Thread {
                        activity?.applicationContext?.let { sender.sendBigFile(waitSendFile, it) }
                    }
                    t.start()
                    t.join()
                }
            }
            waitSendFiles.clear()
            filesAdapter?.notifyDataSetChanged()

        }
    }


    private fun disconnect() {
        wifiP2pManager.cancelConnect(wifiP2pChannel, object : WifiP2pManager.ActionListener {
            override fun onFailure(reasonCode: Int) {
                show_log("cancelConnect onFailure:$reasonCode")
            }

            override fun onSuccess() {
                show_log("cancelConnect onSuccess")
            }
        })
        wifiP2pManager.removeGroup(wifiP2pChannel, null)
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    private fun initView() {
        val mActivity = activity as AppCompatActivity
        mActivity.supportActionBar?.title = "LocalDrop (发送模式)"
        if (activity?.let { getWlanStatus(it.applicationContext) } == true) directActionListener.wifiP2pEnabled(
            true
        )
        // tvFilenum?.text = "0"
        btnSelectFile?.setOnClickListener {
            imagePickerLauncher.launch("*/*")
        }
        btnSelectImage?.setOnClickListener {
            FlierPickerLauncher.launch("image/*")
        }
        btnDisconnect?.setOnClickListener {
            disconnect()
            show_log("断开连接")
        }
        btnSendMessage?.setOnClickListener {
            val et = EditText(activity)
            AlertDialog.Builder(activity).setTitle("请输入消息")
                .setIcon(android.R.drawable.sym_def_app_icon).setView(et)
                .setPositiveButton("确定") { _, i -> // 按下确定键后的事件
                    val text = et.text.toString()
                    val textTransfer = FileInfo(
                        fileName = "text",
                        fileSize = text.length.toLong(),
                        fileType = FileType.TEXT,
                        info = text,
                        raw = text.toByteArray(),
                    )
                    waitSendFiles.add(textTransfer)
                    filesAdapter?.notifyDataSetChanged()
                }.setNegativeButton("取消", null).show()
        }
        btnScanner?.setOnClickListener {
            if (!wifiP2pEnabled) {
                show_log("需要先打开Wifi并且关闭移动热点")
                showToast("需要先打开Wifi并且关闭移动热点")
                return@setOnClickListener
            }
            wifiP2pManager.discoverPeers(wifiP2pChannel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    // show_log("discoverPeers Success")
                }

                override fun onFailure(reasonCode: Int) {
                    show_log("discoverPeers Failure：$reasonCode")
                }
            })
        }

        deviceAdapter.onItemClickListener = object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                if (waitSendFiles.isEmpty()) {
                    showToast("请先添加要发送的文件")
                } else {
                    val wifiP2pDevice = wifiP2pDeviceList.getOrNull(position)
                    if (wifiP2pDevice != null) {
                        connectAndSendFIles(wifiP2pDevice = wifiP2pDevice)
                    }
                }
            }
        }
        rvDeviceList?.layoutManager = LinearLayoutManager(activity)
        rvDeviceList?.adapter = deviceAdapter


        filesAdapter = FileAdapter(waitSendFiles) { file ->
            Toast.makeText(activity, "Clicked: ${file.fileName}", Toast.LENGTH_SHORT).show()
            // 创建对话框
            val alertDialog = AlertDialog.Builder(requireContext()).apply {
                setTitle("是否移除该文件？")
                // 如果是图片类型添加预览
                if (file.fileType == FileType.IMG) {
                    val imageView = ImageView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            resources.getDimensionPixelSize(R.dimen.thumbnail_size),
                            resources.getDimensionPixelSize(R.dimen.thumbnail_size)
                        )
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                    try {
                        file.raw?.let {
                            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                            imageView.setImageBitmap(bitmap)
                        }
                    } catch (e: Exception) {
                        show_log("图片加载失败: ${e.message}")
                    }
                    setView(imageView)
                }
                if (file.fileType == FileType.TEXT) {
                    val textView = TextView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            resources.getDimensionPixelSize(R.dimen.thumbnail_size),
                            resources.getDimensionPixelSize(R.dimen.thumbnail_size)
                        )
                        text = file.info
                    }
                    setView(textView)
                }
                // 确认按钮
                setPositiveButton("是") { _, _ ->
                    val position = waitSendFiles.indexOfFirst { it == file }
                    if (position != -1) {
                        waitSendFiles.removeAt(position)
                    }
                    filesAdapter?.notifyItemRemoved(position)
                }
                // 取消按钮
                setNegativeButton("否", null)
            }.create()
            // 显示对话框
            alertDialog.show()
        }
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        recyclerView?.layoutManager = layoutManager
        recyclerView?.isHorizontalScrollBarEnabled = true
        recyclerView?.adapter = filesAdapter
    }

    // 设备相关
    private lateinit var wifiP2pChannel: WifiP2pManager.Channel
    private val wifiP2pManager: WifiP2pManager by lazy {
        activity?.getSystemService(WIFI_P2P_SERVICE) as WifiP2pManager
    }
    private var broadcastReceiver: BroadcastReceiver? = null
    var discoverThread = Thread()

    @SuppressLint("MissingPermission")
    fun DiscoverPeers() {
        while (true) {
            try {
                if (!wifiP2pEnabled) {
                    show_log("需要先打开Wifi并且关闭移动热点")
                    showToast("需要打开Wifi并且关闭移动热点")
                }
                wifiP2pManager.discoverPeers(wifiP2pChannel,
                    object : WifiP2pManager.ActionListener {
                        override fun onSuccess() {
                            // show_log("discoverPeers Success")
                        }

                        override fun onFailure(reasonCode: Int) {
                            show_log("discoverPeers Failure：$reasonCode")
                        }
                    })
                Thread.sleep(2000)
            } catch (e: InterruptedException) {
                show_log("DiscoverPeers线程已退出")
                return
            }

        }
    }

    private fun initDevice() {
        wifiP2pChannel =
            wifiP2pManager.initialize(activity, activity?.mainLooper, directActionListener)
        broadcastReceiver =
            SendDirectBroadcastReceiver(wifiP2pManager, wifiP2pChannel, directActionListener)
        activity?.let {
            ContextCompat.registerReceiver(
                it,
                broadcastReceiver,
                SendDirectBroadcastReceiver.getIntentFilter(),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }
        discoverThread = Thread { DiscoverPeers() }
        discoverThread.start()
    }


    // 生命周期相关
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_send, container, false)
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.enableEdgeToEdge()
        // show_log("onCreate")
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onStart() {
        super.onStart()
        initView()
        initDevice()
        // show_log("onStart")
    }

    override fun onStop() {
        super.onStop()
        discoverThread.interrupt()
        if (broadcastReceiver != null) {
            activity?.unregisterReceiver(broadcastReceiver)
        }
        show_log("onStop")
    }


    // ActionListener
    private val directActionListener = object : DirectActionListener {
        override fun wifiP2pEnabled(enabled: Boolean) {
            wifiP2pEnabled = enabled
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        override fun onConnectionInfoAvailable(wifiP2pInfo: WifiP2pInfo) {
            show_log(
                "onConnectionInfoAvailable\ngroupFormed:${wifiP2pInfo.groupFormed}\n" + "isGroupOwner:${wifiP2pInfo.isGroupOwner}\n" + "groupOwnerAddress hostAddress:${wifiP2pInfo.groupOwnerAddress.hostAddress}" + "isGroupOwner:${wifiP2pInfo.isGroupOwner}\nisGroupOwner: ${wifiP2pInfo.isGroupOwner}\n" + "groupFormed:${wifiP2pInfo.groupFormed}\n" + "groupOwnerAddress hostAddress: ${wifiP2pInfo.groupOwnerAddress.hostAddress}"
            )
            if (wifiP2pInfo.groupFormed && !wifiP2pInfo.isGroupOwner) {
                this@SendFragment.wifiP2pInfo = wifiP2pInfo
                val ipAddress = wifiP2pInfo.groupOwnerAddress?.hostAddress
                show_log("获取到根地址,开始发送 $ipAddress")
                // 客户端发送文件
                for (waitSendFile in waitSendFiles) {
                    val sender = ipAddress?.let { FileSender(it, 27431, 27432) }
                    if (sender != null && waitSendFile.fileType != FileType.BIG_FILE) {
                        val t = Thread {
                            sender.sendFile(waitSendFile)
                        }
                        t.start()
                        t.join()
                    } else if (sender != null) {
                        val t = Thread {
                            activity?.applicationContext?.let {
                                sender.sendBigFile(
                                    waitSendFile,
                                    it
                                )
                            }
                        }
                        t.start()
                        t.join()
                    }
                }
                waitSendFiles.clear()
                filesAdapter?.notifyDataSetChanged()
            }
        }


        override fun onDisconnection() {
            show_log("onDisconnection")
            wifiP2pInfo = null
            show_log("处于非连接状态")
        }

        override fun onSelfDeviceAvailable(device: WifiP2pDevice) {
            var _status_text = ""
            when (device.status) {
                0 -> _status_text = "CONNECTED"
                1 -> _status_text = "INVITED"
                2 -> _status_text = "FAILED"
                3 -> _status_text = "AVAILABLE"
                4 -> _status_text = "UNAVAILABLE"
            }
            val log = buildString {
                append("deviceName: " + device.deviceName)
                append("\n")
                append("deviceAddress: " + device.deviceAddress)
                append("\n")
                append(
                    "status: " + device.status + " " + _status_text
                )
            }
            show_log(log)
        }

        override fun onPeersAvailable(devices: List<WifiP2pDevice>) {
            wifiP2pDeviceList.clear()
            if (devices.isEmpty()) {
                show_log("onPeersAvailable: 0")
            } else {
                wifiP2pDeviceList.addAll(elements = devices)
                // show_log("onPeersAvailable: " + devices.size)
            }
            deviceAdapter.notifyDataSetChanged()

        }

        override fun onChannelDisconnected() {
            show_log("onChannelDisconnected")
        }
    }
}
