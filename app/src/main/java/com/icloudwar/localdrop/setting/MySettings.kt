package com.icloudwar.localdrop.setting

import android.content.Context
import android.content.SharedPreferences

class MySettings(context: Context) {
    // SharedPreferences 实例
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        const val PREFS_NAME = "LocalDropSettings"
        const val PORT_KEY = "server_port"
        const val DEFAULT_PORT = 27431 // 默认端口号
    }

    /**
     * 获取当前端口设置
     */
    fun getPort(): Int {
        return sharedPreferences.getInt(PORT_KEY, DEFAULT_PORT)
    }

    /**
     * 设置端口号
     * @return Boolean 是否设置成功
     */
    fun setPort(port: Int): Boolean {
        if (port < 1 || port > 65535) {
            return false
        }

        sharedPreferences.edit().putInt(PORT_KEY, port).apply()
        return true
    }

    /**
     * 重置为默认设置
     */
    fun resetToDefault() {
        sharedPreferences.edit().putInt(PORT_KEY, DEFAULT_PORT).apply()
    }
}