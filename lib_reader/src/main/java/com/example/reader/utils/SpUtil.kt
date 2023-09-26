package com.example.reader.utils

import android.content.Context
import com.example.common.BaseApplication

object SpUtil {
    private val mContext by lazy { BaseApplication.instance.applicationContext }
    private const val DEFAULT_TEXT_SIZE = 56f
    private const val DEFAULT_ROW_SPACE = 24f
    private const val DEFAULT_THEME = 0
    private const val DEFAULT_BRIGHTNESS = -1f
    private const val DEFAULT_TURN_TYPE = 0
    private const val DEFAULT_IS_NIGHT_MODE = false
    private const val NAME = "freader_data"
    private const val KEY_TEXT_SIZE = "key_text_size"//文字大小
    private const val KEY_ROW_SPACE = "key_row_space"//行距
    private const val KEY_THEME = "key_theme"//阅读主题
    private const val KEY_BRIGHTNESS = "key_brightness"//亮度
    private const val KEY_IS_NIGHT_MODE = "key_is_night_mode"//是否为夜间模式
    private const val KEY_TURN_TYPE = "key_turn_type"//翻页模式

    @JvmStatic
    fun saveTextSize(textSize: Float) {
        val editor = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
        editor.putFloat(KEY_TEXT_SIZE, textSize)
        editor.apply()
    }

    @JvmStatic
    fun getTextSize(): Float {
        val sp = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        return sp.getFloat(KEY_TEXT_SIZE, DEFAULT_TEXT_SIZE)
    }

    @JvmStatic
    fun saveRowSpace(rowSpace: Float) {
        val editor = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
        editor.putFloat(KEY_ROW_SPACE, rowSpace)
        editor.apply()
    }

    @JvmStatic
    fun getRowSpace(): Float {
        val sp = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        return sp.getFloat(KEY_ROW_SPACE, DEFAULT_ROW_SPACE)
    }

    @JvmStatic
    fun saveTheme(theme: Int) {
        val editor = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
        editor.putInt(KEY_THEME, theme)
        editor.apply()
    }

    @JvmStatic
    fun getTheme(): Int {
        val sp = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        return sp.getInt(KEY_THEME, DEFAULT_THEME)
    }

    @JvmStatic
    fun saveBrightness(brightness: Float) {
        val editor = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
        editor.putFloat(KEY_BRIGHTNESS, brightness)
        editor.apply()
    }

    @JvmStatic
    fun getBrightness(): Float {
        val sp = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        return sp.getFloat(KEY_BRIGHTNESS, DEFAULT_BRIGHTNESS)
    }

    @JvmStatic
    fun saveIsNightMode(isNightMode: Boolean) {
        val editor = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
        editor.putBoolean(KEY_IS_NIGHT_MODE, isNightMode)
        editor.apply()
    }

    @JvmStatic
    fun getIsNightMode(): Boolean {
        val sp = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        return sp.getBoolean(KEY_IS_NIGHT_MODE, DEFAULT_IS_NIGHT_MODE)
    }

    @JvmStatic
    fun saveTurnType(turnType: Int) {
        val editor = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
        editor.putInt(KEY_TURN_TYPE, turnType)
        editor.apply()
    }

    @JvmStatic
    fun getTurnType(): Int {
        val sp = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        return sp.getInt(KEY_TURN_TYPE, DEFAULT_TURN_TYPE)
    }

}