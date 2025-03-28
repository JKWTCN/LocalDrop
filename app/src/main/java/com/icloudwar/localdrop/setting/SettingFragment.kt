package com.icloudwar.localdrop.setting

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.Fragment
import com.icloudwar.localdrop.R


class SettingFragment : Fragment() {


    private fun show_log(message: String) {
        Log.i("SettingFragment", message)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // activity?.enableEdgeToEdge()
        show_log("onCreateView")
        return inflater.inflate(R.layout.activity_setting, container, false)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.enableEdgeToEdge()
        show_log("onCreate")
    }

    override fun onDetach() {
        super.onDetach()
        show_log("onDetach")
    }

    override fun onStart() {
        super.onStart()
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