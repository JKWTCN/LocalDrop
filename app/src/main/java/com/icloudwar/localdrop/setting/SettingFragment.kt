package com.icloudwar.localdrop.setting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.icloudwar.localdrop.R


class SettingFragment : Fragment() {
    private fun show_log(message: String) {
        Log.i("SettingFragment", message)
    }

    private val et_port by lazy {
        activity?.findViewById<EditText>(R.id.et_port)
    }
    private val btn_github by lazy {
        activity?.findViewById<Button>(R.id.btn_github)
    }
    private val btn_bilibili by lazy {
        activity?.findViewById<Button>(R.id.btn_bilibili)
    }
    private val btn_save_port by lazy {
        activity?.findViewById<Button>(R.id.btn_save_port)
    }
    private val btn_resetToDefault by lazy {
        activity?.findViewById<Button>(R.id.btn_resetToDefault)
    }

    private lateinit var mySettings: MySettings

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.activity_setting, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.enableEdgeToEdge()
        mySettings = MySettings(requireContext())
        show_log("onCreate")

    }

    /**
     * 加载设置
     */
    private fun loadSettings() {
        val port = mySettings.getPort()
        et_port?.setText(port.toString())
        show_log("Loaded settings - Port: $port")
    }

    /**
     * 保存设置
     */
    private fun saveSettings() {
        try {
            val portText = et_port?.text.toString()
            if (portText.isEmpty()) {
                Toast.makeText(context, "请输入端口号", Toast.LENGTH_SHORT).show()
                return
            }

            val port = portText.toInt()
            if (mySettings.setPort(port)) {
                Toast.makeText(context, "设置已保存", Toast.LENGTH_SHORT).show()
                show_log("Settings saved - Port: $port")
            } else {
                Toast.makeText(context, "端口号必须在1-65535之间", Toast.LENGTH_SHORT).show()
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(context, "请输入有效的端口号", Toast.LENGTH_SHORT).show()
            show_log("Invalid port number format")
        }
    }

    private fun openUrl(url: String) {
        show_log("openUrl: $url")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    override fun onDetach() {
        super.onDetach()
        show_log("onDetach")
    }

    override fun onStart() {

        super.onStart()
        // activity?.enableEdgeToEdge()
        show_log("onCreateView")

        loadSettings()
        btn_save_port?.setOnClickListener {
            saveSettings()
        }

        btn_resetToDefault?.setOnClickListener {
            mySettings.resetToDefault()
        }

        // GitHub按钮点击事件
        btn_github?.setOnClickListener {
            openUrl("https://github.com/JKWTCN/LocalDrop")
        }

        // B站按钮点击事件
        btn_bilibili?.setOnClickListener {
            openUrl("https://space.bilibili.com/283390377")
        }
        show_log("onStart")
    }

    override fun onResume() {
        super.onResume()
        show_log("onResume")
    }

    override fun onPause() {
        super.onPause()
        show_log("onPause")
    }

    override fun onStop() {
        super.onStop()
        show_log("onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        show_log("onDestroy")
    }


}