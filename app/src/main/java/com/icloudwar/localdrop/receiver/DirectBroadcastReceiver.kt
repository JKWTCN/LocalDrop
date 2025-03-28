package com.icloudwar.localdrop.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log


class DirectBroadcastReceiver(
    private val wifiP2pManager: WifiP2pManager,
    private val wifiP2pChannel: WifiP2pManager.Channel,
    private val directActionListener: DirectActionListener
) : BroadcastReceiver() {

    private val Tag = "DirectBroadcastReceiver";


    companion object {

        fun getIntentFilter(): IntentFilter {
            val intentFilter = IntentFilter()
//            Wi-Fi P2P 状态改变时触发。
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
//            Wi-Fi P2P 设备列表改变时触发。
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
//            Wi-Fi P2P 连接状态改变时触发
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
//            本设备的 Wi-Fi P2P 状态改变时触发
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
            return intentFilter
        }

    }

    private var now_peers_list = listOf<WifiP2pDevice>()

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                val enabled = intent.getIntExtra(
                    WifiP2pManager.EXTRA_WIFI_STATE, -1
                ) == WifiP2pManager.WIFI_P2P_STATE_ENABLED
                directActionListener.wifiP2pEnabled(enabled)
                if (!enabled) {
                    directActionListener.onPeersAvailable(emptyList())
                }
                Log.i(Tag, "WIFI_P2P_STATE_CHANGED_ACTION： $enabled")
            }

            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {

                wifiP2pManager.requestPeers(wifiP2pChannel) { peers ->
                    directActionListener.onPeersAvailable(peers.deviceList.toList())
                    now_peers_list = peers.deviceList.toList()
                    if (now_peers_list.isNotEmpty()) {
                        Log.i(Tag, "当前peer总数:${now_peers_list.size}")
                        for (i in now_peers_list) {
                            // i.status
                            // CONNECTED   = 0;
                            // INVITED     = 1;
                            // FAILED      = 2;
                            // AVAILABLE   = 3;
                            // UNAVAILABLE = 4;
                            var _status_text = "";
                            when (i.status) {
                                0 -> _status_text = "CONNECTED"
                                1 -> _status_text = "INVITED"
                                2 -> _status_text = "FAILED"
                                3 -> _status_text = "AVAILABLE"
                                4 -> _status_text = "UNAVAILABLE"
                            }
                            Log.i(
                                Tag,
                                "peer信息:设备名:${i.deviceName};设备地址:${i.deviceAddress};设备类型:${i.primaryDeviceType};设备状态:${_status_text}"
                            )
                        }
                    } else {
                        Log.i(Tag, "当前peer总数:0")
                    }
                    // Log.i(Tag, "可用的类似应用列表:${peers.deviceList.toList()}")
                }

            }

            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                val networkInfo =
                    intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)

                Log.i(Tag, "Wi-Fi 直连连接的状态已更改 ：networkInfo:${networkInfo} ")
                if (networkInfo != null && networkInfo.isConnected) {
                    wifiP2pManager.requestConnectionInfo(wifiP2pChannel) { info ->
                        if (info != null) {
                            directActionListener.onConnectionInfoAvailable(info)
                            // Log.i(Tag, "已连接 P2P 设备,info为:${info}")
                        }
                    }

                } else {
                    directActionListener.onDisconnection()
                    Log.i(Tag, "与 P2P 设备已断开连接")
                }
            }

            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                val wifiP2pDevice =
                    intent.getParcelableExtra<WifiP2pDevice>(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)
                if (wifiP2pDevice != null) {
                    directActionListener.onSelfDeviceAvailable(wifiP2pDevice)
                }
                Log.i(Tag, "此设备的配置详细信息已更改: ${wifiP2pDevice.toString()}")
            }
        }
    }




}
