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
        const val SAVE_TO_PICTURES_KEY = "save_to_pictures_directory"
        const val DEFAULT_SAVE_TO_PICTURES = false
    }

    /**
     * 获取当前图片保存设置
     */
    fun getSaveToPictures(): Boolean {
        return sharedPreferences.getBoolean(SAVE_TO_PICTURES_KEY, DEFAULT_SAVE_TO_PICTURES)
    }

    /**
     * 设置图片保存
     * @return Boolean 是否设置成功
     */
    fun setSaveToPictures(status: Boolean): Boolean {
        sharedPreferences.edit().putBoolean(SAVE_TO_PICTURES_KEY, status).apply()
        return true
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
        sharedPreferences.edit().putBoolean(SAVE_TO_PICTURES_KEY, DEFAULT_SAVE_TO_PICTURES).apply()
    }
}