package com.example.framework.utils

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
    fun hasSdcard() = Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

}

/**
 * 获取sd卡目录-绝对路径
 */
fun Context.getSdcardAbsolutePath(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        getExternalFilesDir(null)?.absolutePath.orEmpty()
    } else {
        Environment.getExternalStorageDirectory().absolutePath
    }
}

/**
 * 获取sd卡目录-相对路径
 */
fun Context.getSdcardPath(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        getExternalFilesDir(null)?.path.orEmpty()
    } else {
        Environment.getExternalStorageDirectory().path
    }
}

/**
 * 获取sd卡存储空间类
 */
fun Context.getSdcardStatFs() = StatFs(getSdcardPath())

/**
 * 获得内置sd卡总容量，单位M
 */
fun Context.getSdcardTotalCapacity(): Long {
    //获得sdcard上 block的总数
    val blockCount = getSdcardStatFs().blockCountLong
    //获得sdcard上每个block 的大小
    val blockSize = getSdcardStatFs().blockSizeLong
    return (blockCount * blockSize) / 1024 / 1024
}

/**
 * 获得内置sd卡可用容量，即可用大小，单位M
 */
fun Context.getSdcardAvailableCapacity(): Long {
    //获得sdcard上 block的总数
    val blockCount = getSdcardStatFs().availableBlocksLong
    //获得sdcard上每个block 的大小
    val blockSize = getSdcardStatFs().blockSizeLong
    return (blockCount * blockSize) / 1024 / 1024
}

/**
 * 获得内置sd卡不可用容量，即已用大小，单位M
 */
fun Context.getSdcardUnavailableCapacity() = getSdcardTotalCapacity() - getSdcardAvailableCapacity()

