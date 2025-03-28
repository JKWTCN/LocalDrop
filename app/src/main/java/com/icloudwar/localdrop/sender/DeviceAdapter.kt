package com.icloudwar.localdrop.sender

import android.net.wifi.p2p.WifiP2pDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.icloudwar.localdrop.R

interface OnItemClickListener {

    fun onItemClick(position: Int)

}

class DeviceAdapter(private val wifiP2pDeviceList: List<WifiP2pDevice>) :
    RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {

    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.device_item, parent, false)
        return ViewHolder(view)
    }

    private fun getStatus(i: Int): String {
        var _status_text = ""
        when (i) {
            0 -> _status_text = "CONNECTED"
            1 -> _status_text = "INVITED"
            2 -> _status_text = "FAILED"
            3 -> _status_text = "AVAILABLE"
            4 -> _status_text = "UNAVAILABLE"
        }
        return _status_text
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = wifiP2pDeviceList[position]
        holder.tvDeviceName.text = device.deviceName
        holder.tvDeviceAddress.text = device.deviceAddress
        holder.tvDeviceDetails.text = getStatus(device.status)
        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClick(position = position)
        }
    }

    override fun getItemCount(): Int {
        return wifiP2pDeviceList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDeviceName: TextView = itemView.findViewById(R.id.tvDeviceName)
        val tvDeviceAddress: TextView = itemView.findViewById(R.id.tvDeviceAddress)
        val tvDeviceDetails: TextView = itemView.findViewById(R.id.tvDeviceDetails)

    }

}