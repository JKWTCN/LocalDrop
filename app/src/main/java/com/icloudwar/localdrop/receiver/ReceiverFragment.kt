package com.icloudwar.localdrop.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatActivity.WIFI_P2P_SERVICE
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.icloudwar.localdrop.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume


class ReceiverFragment : Fragment() {

    // 日志相关
    private fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()

    }

    private fun show_log(message: String) {
        Log.d("ReceiverFragment", message)
    }

    // view相关
    private val cbAutoSaveAll by lazy {
        activity?.findViewById<CheckBox>(R.id.cbAutoSaveAll)
    }
    private val cbAutoSaveNone by lazy {
        activity?.findViewById<CheckBox>(R.id.cbAutoSaveNone)
    }
    private val cbAutoSaveFavorite by lazy {
        activity?.findViewById<CheckBox>(R.id.cbAutoSaveFavorite)
    }
    private val btnFavorite by lazy {
        activity?.findViewById<Button>(R.id.btnFavorite)
    }
    private val btnHistory by lazy {
        activity?.findViewById<Button>(R.id.btnHistory)
    }

    // 0:所有人 1:收藏 2:无
    private var rev_mode = 0;
    private fun initView() {
        val mActivity = activity as AppCompatActivity
        mActivity.supportActionBar?.title = "LocalDrop (接收模式)"
        cbAutoSaveAll?.setChecked(true)
        cbAutoSaveNone?.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                rev_mode = 2
                cbAutoSaveAll?.setChecked(false)
                cbAutoSaveFavorite?.setChecked(false)
            }
        })
        cbAutoSaveFavorite?.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                rev_mode = 1
                cbAutoSaveAll?.setChecked(false)
                cbAutoSaveNone?.setChecked(false)
            }
        })
        cbAutoSaveAll?.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                rev_mode = 0
                cbAutoSaveNone?.setChecked(false)
                cbAutoSaveFavorite?.setChecked(false)
            }
        })

        btnFavorite?.setOnClickListener {
            // todo 收藏
        }
        btnHistory?.setOnClickListener {
            // todo 历史
        }
    }

    // 设备相关
    private lateinit var wifiP2pChannel: WifiP2pManager.Channel
    private val wifiP2pManager: WifiP2pManager by lazy {
        activity?.getSystemService(WIFI_P2P_SERVICE) as WifiP2pManager
    }
    private var broadcastReceiver: BroadcastReceiver? = null
    val receiver = FileReceiver(
        port = 27431, saveDir = File("/download")
    )
    var receiverThread = Thread()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun initDevice() {
//        wifiP2pManager初始化
        wifiP2pChannel =
            wifiP2pManager.initialize(activity, activity?.mainLooper, directActionListener)
//        广播接收器初始化
        broadcastReceiver = DirectBroadcastReceiver(
            wifiP2pManager = wifiP2pManager,
            wifiP2pChannel = wifiP2pChannel,
            directActionListener = directActionListener
        )
//        注册广播接收器
        activity?.let {
            ContextCompat.registerReceiver(
                it,
                broadcastReceiver,
                DirectBroadcastReceiver.getIntentFilter(),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }
        createGroup()
        receiver.start()
        // receiverThread = Thread { receiver.start() }
        // receiverThread.start()

    }

    // 应用层相关
    @SuppressLint("MissingPermission")
    private fun createGroup() {
        lifecycleScope.launch {
            removeGroupIfNeed()
            wifiP2pManager.createGroup(wifiP2pChannel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    show_log("createGroup onSuccess")
                    showToast("创建群组成功")
                }

                override fun onFailure(reason: Int) {
                    val log = "createGroup onFailure: $reason"
                    show_log(log)
                    showToast(message = log)
                }
            })
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun removeGroupIfNeed() {
        return suspendCancellableCoroutine { continuation ->
            wifiP2pManager.requestGroupInfo(wifiP2pChannel) { group ->
                if (group == null) {
                    continuation.resume(value = Unit)
                } else {
                    wifiP2pManager.removeGroup(wifiP2pChannel,
                        object : WifiP2pManager.ActionListener {
                            override fun onSuccess() {
                                val log = "removeGroup onSuccess"
                                show_log(log)
                                showToast(message = log)
                                continuation.resume(value = Unit)
                            }

                            override fun onFailure(reason: Int) {
                                val log = "removeGroup onFailure: $reason"
                                show_log(log)
                                showToast(message = log)
                                continuation.resume(value = Unit)
                            }
                        })
                }
            }
        }
    }

    // 生命周期相关
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.enableEdgeToEdge()
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
        receiver.stop()
        // receiverThread.interrupt()
        lifecycleScope.launch {
            if (broadcastReceiver != null) {
                activity?.unregisterReceiver(broadcastReceiver)
            }
            removeGroupIfNeed();
        }
        // show_log("onStop")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_receiver, container, false)
    }

    //  ActionListener
    private val directActionListener = object : DirectActionListener {
        override fun wifiP2pEnabled(enabled: Boolean) {
            show_log("wifiP2pEnabled: $enabled")
        }

        override fun onConnectionInfoAvailable(wifiP2pInfo: WifiP2pInfo) {
            show_log(
                "onConnectionInfoAvailable " + "\n" + "isGroupOwner: " + wifiP2pInfo.isGroupOwner + "\n" + "groupFormed: " + wifiP2pInfo.groupFormed + "\n" + "groupOwnerAddress: " + wifiP2pInfo.groupOwnerAddress.toString()
            )
        }

        override fun onDisconnection() {
            show_log("onDisconnection")
        }

        override fun onSelfDeviceAvailable(device: WifiP2pDevice) {
            show_log("onSelfDeviceAvailable: $device")
        }


        override fun onPeersAvailable(devices: List<WifiP2pDevice>) {
            show_log("onPeersAvailable, size:" + devices.size)
            for (wifiP2pDevice in devices) {
                show_log("wifiP2pDevice: $wifiP2pDevice")
            }
        }

        override fun onChannelDisconnected() {
            show_log("onChannelDisconnected")
        }
    }

}