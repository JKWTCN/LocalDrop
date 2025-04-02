package com.icloudwar.localdrop.setting

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.icloudwar.localdrop.R


class SettingFragment : Fragment() {
    private fun show_log(message: String) {
        Log.i("SettingFragment", message)
    }

    private val btn_github by lazy {
        activity?.findViewById<Button>(R.id.btn_github)
    }
    private val btn_bilibili by lazy {
        activity?.findViewById<Button>(R.id.btn_bilibili)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.activity_setting, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.enableEdgeToEdge()
        show_log("onCreate")

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