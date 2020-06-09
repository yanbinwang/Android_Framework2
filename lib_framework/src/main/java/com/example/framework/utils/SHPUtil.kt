package com.example.framework.utils

import android.annotation.SuppressLint
import android.content.Context
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * author:wyb
 * 分文件管理sharedPreferences存储的值
 * 针对不同的变量做不同的调整
 */
@SuppressLint("ApplySharedPref")
class SHPUtil {
    private var name = "data"
    private var context: Context
    private val readWriteLock = ReentrantReadWriteLock()
    private val readLock = readWriteLock.readLock()
    private val writeLock = readWriteLock.writeLock()

    //普通的构造方法
    constructor(context: Context) {
        this.context = context
    }

    //传入文件名的构造方法
    constructor(context: Context, name: String) {
        this.name = name
        this.context = context
    }

    //清空参数
    fun clearParam() {
        val sharedPreferences = context.getSharedPreferences(name, 0).edit()
        writeLock.lock()
        try {
            sharedPreferences.clear()
            sharedPreferences.commit()
        } finally {
            writeLock.unlock()
        }
    }

    //保存参数
    fun saveParam(key: String, value: String) {
        val sharedPreferences = context.getSharedPreferences(name, 0).edit()
        sharedPreferences.putString(key, value)
        writeLock.lock()
        try {
            sharedPreferences.commit()
        } finally {
            writeLock.unlock()
        }
    }

    fun saveIntParam(key: String, value: Int) {
        val sharedPreferences = context.getSharedPreferences(name, 0).edit()
        sharedPreferences.putInt(key, value)
        writeLock.lock()
        try {
            sharedPreferences.commit()
        } finally {
            writeLock.unlock()
        }
    }

    fun saveLongParam(key: String, value: Long) {
        val sharedPreferences = context.getSharedPreferences(name, 0).edit()
        sharedPreferences.putLong(key, value)
        writeLock.lock()
        try {
            sharedPreferences.commit()
        } finally {
            writeLock.unlock()
        }
    }

    //获取参数
    fun getParam(key: String): String {
        val sharedPreferences = context.getSharedPreferences(name, 0)
        readLock.lock()
        try {
            return sharedPreferences.getString(key, null)
        } finally {
            readLock.unlock()
        }
    }

    fun getParam(key: String, defValue: String): String {
        val sharedPreferences = context.getSharedPreferences(name, 0)
        readLock.lock()
        try {
            return sharedPreferences.getString(key, defValue)
        } finally {
            readLock.unlock()
        }
    }

    fun getIntParam(key: String): Int {
        val sharedPreferences = context.getSharedPreferences(name, 0)
        readLock.lock()
        try {
            return sharedPreferences.getInt(key, 0)
        } finally {
            readLock.unlock()
        }
    }

    fun getLongParam(key: String): Long {
        val sharedPreferences = context.getSharedPreferences(name, 0)
        readLock.lock()
        try {
            return sharedPreferences.getLong(key, 0)
        } finally {
            readLock.unlock()
        }
    }

    //清除对应key值的值
    fun removeKey(key: String) {
        val sharedPreferences = context.getSharedPreferences(name, 0).edit()
        sharedPreferences.remove(key)
        writeLock.lock()
        try {
            sharedPreferences.commit()
        } finally {
            writeLock.unlock()
        }
    }

}