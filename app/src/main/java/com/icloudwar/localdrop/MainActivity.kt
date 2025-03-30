package com.icloudwar.localdrop

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {
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
            // showToast(message = "已获得全部权限")
        } else {
            showToast("获取权限失败，请重启应用重新获取")
        }
    }

    private fun initView() {
        supportActionBar?.title = "LocalDrop (接收模式)"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissionLaunch.launch(requestedPermissions);
        initView()
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        findViewById<BottomNavigationView>(R.id.bottom_nav).setupWithNavController(navController)

        val intent = intent
        val action = intent.action // action
        val type = intent.type // 类型
        when (intent.action) {
            Intent.ACTION_SEND_MULTIPLE -> {
                val type = intent.type ?: return
                val uris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                if (uris != null && uris.isNotEmpty()) {
                    navigateToSendFragmentWithData(uris)
                }
            }

            Intent.ACTION_SEND -> {
                // 可选：处理单文件分享
                val type = intent.type ?: return
                val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                uri?.let {
                    navigateToSendFragmentWithData(arrayListOf(it))
                }
            }
        }
    }

    // 带参数导航到 SendFragment
    private fun navigateToSendFragmentWithData(uris: ArrayList<Uri>) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // 创建 Bundle 传递数据
        val args = Bundle().apply {
            putParcelableArrayList("shared_uris", uris)
        }

        // 使用导航图的 action 或直接 ID 跳转
        navController.navigate(
            R.id.sendFragment,  // 确保导航图中 sendFragment 的 ID 正确
            args,
            NavOptions.Builder()
                .setPopUpTo(navController.graph.startDestinationId, false)
                .build()
        )
    }
}
