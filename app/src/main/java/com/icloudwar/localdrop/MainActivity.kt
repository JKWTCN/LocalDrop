package com.icloudwar.localdrop

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.icloudwar.localdrop.receiver.ReceiverFragment
import com.icloudwar.localdrop.sender.SendFragment


class MainActivity : AppCompatActivity() {
    private val viewpager by lazy {
        findViewById<ViewPager2>(R.id.viewpager)
    }
    private val rg_tab by lazy {
        findViewById<RadioGroup>(R.id.rg_tab)
    }
    private val rb_receiver by lazy {
        findViewById<RadioButton>(R.id.rb_receiver)
    }
    private val rb_send by lazy {
        findViewById<RadioButton>(R.id.rb_send)
    }
    private val rb_setting by lazy {
        findViewById<RadioButton>(R.id.rb_setting)
    }


    private var mViews = mutableListOf<View>()// 存放视图

    // 需要权限列表
    private val requestedPermissions = buildList {
        add(Manifest.permission.INTERNET)
        add(Manifest.permission.ACCESS_WIFI_STATE)
        add(Manifest.permission.CHANGE_WIFI_STATE)
        add(Manifest.permission.ACCESS_NETWORK_STATE)
        add(Manifest.permission.CHANGE_NETWORK_STATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.NEARBY_WIFI_DEVICES)
        } else {
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }.toTypedArray()

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    }

    private val requestPermissionLaunch = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { it ->
        if (it.all { it.value }) {
            showToast(message = "已获得全部权限")
        } else {
            showToast("获取权限失败")
        }
    }


    // ViewPager适配器
    class ViewPagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int = 3
        var oldPosition: Int = 0
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> {
                    oldPosition = 0
                    ReceiverFragment()
                }

                1 -> {
                    oldPosition = 1
                    SendFragment()
                }

                2 -> {
                    oldPosition = 2
                    SettingActivity()
                }

                else -> throw IllegalArgumentException("Invalid position")
            }
        }
    }

    private fun initView() {
        supportActionBar?.title = "LocalDrop (接收模式)"
        mViews.add(LayoutInflater.from(this).inflate(R.layout.activity_receiver, null))
        mViews.add(LayoutInflater.from(this).inflate(R.layout.activity_send, null));
        mViews.add(LayoutInflater.from(this).inflate(R.layout.activity_setting, null));
        viewpager.adapter = ViewPagerAdapter(this)
        viewpager.isUserInputEnabled = false
        rg_tab.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_receiver -> viewpager.currentItem = 0
                R.id.rb_send -> viewpager.currentItem = 1
                R.id.rb_setting -> viewpager.currentItem = 2
            }
        }
        viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> rg_tab.check(R.id.rb_receiver)
                    1 -> rg_tab.check(R.id.rb_send)
                    2 -> rg_tab.check(R.id.rb_setting)
                }
            }
        })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissionLaunch.launch(requestedPermissions);
        initView()
    }


}
