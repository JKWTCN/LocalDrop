package com.icloudwar.localdrop

import android.icu.lang.UCharacter.GraphemeClusterBreak.L
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment


class SettingActivity : Fragment() {
    private fun show_log(message: String) {
        Log.i("SendFragment", message)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()
        // setContentView(R.layout.activity_setting)
        Log.d("SettingActivity", "onCreate: ");
        // ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
        //     val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        //     v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
        //     insets
        // }
    }




    override fun onDetach() {
        super.onDetach()
        Log.d("SettingActivity", "onDetach")
    }

    override fun onStart() {
        super.onStart()
        Log.d("SettingActivity", "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("SettingActivity", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("SettingActivity", "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("SettingActivity", "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("SettingActivity", "onDestroy")
    }


}