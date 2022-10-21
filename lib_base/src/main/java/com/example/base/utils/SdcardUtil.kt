package com.example.base.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs

/**
 *  Created by wangyanbin
 *  sd卡工具类
 *  只能拿到外置存储器的大致容量，并不准确
 */
object SdcardUtil {

    /**
     * 判断sd卡是否存在
     */
    @JvmStatic
    fun hasSdcard() = Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

    /**
     * 获取sd卡目录-绝对路径
     */
    @JvmStatic
    fun getSdcardAbsolutePath(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.getExternalFilesDir(null)?.absolutePath.orEmpty()
        } else {
            Environment.getExternalStorageDirectory().absolutePath
        }
    }

    /**
     * 获取sd卡目录-相对路径
     */
    @JvmStatic
    fun getSdcardPath(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.getExternalFilesDir(null)?.path.orEmpty()
        } else {
            Environment.getExternalStorageDirectory().path
        }
    }

    /**
     * 获取sd卡存储空间类
     */
    @JvmStatic
    fun getSdcardStatFs(context: Context) = StatFs(getSdcardPath(context))

    /**
     * 获得内置sd卡总容量，单位M
     */
    @JvmStatic
    fun getSdcardTotalCapacity(context: Context): Long {
        //获得sdcard上 block的总数
        val blockCount = getSdcardStatFs(context).blockCountLong
        //获得sdcard上每个block 的大小
        val blockSize = getSdcardStatFs(context).blockSizeLong
        return (blockCount * blockSize) / 1024 / 1024
    }

    /**
     * 获得内置sd卡可用容量，即可用大小，单位M
     */
    @JvmStatic
    fun getSdcardAvailableCapacity(context: Context): Long {
        //获得sdcard上 block的总数
        val blockCount = getSdcardStatFs(context).availableBlocksLong
        //获得sdcard上每个block 的大小
        val blockSize = getSdcardStatFs(context).blockSizeLong
        return (blockCount * blockSize) / 1024 / 1024
    }

    /**
     * 获得内置sd卡不可用容量，即已用大小，单位M
     */
    @JvmStatic
    fun getSdcardUnavailableCapacity(context: Context) = getSdcardTotalCapacity(context) - getSdcardAvailableCapacity(context)

}