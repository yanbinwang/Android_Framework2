package com.example.multimedia.utils

import android.content.Context
import android.media.MediaPlayer
import com.example.common.config.Constants
import com.example.common.utils.helper.AccountHelper
import com.example.framework.utils.function.value.convert
import com.example.framework.utils.getSdcardAvailableCapacity
import com.example.framework.utils.hasSdcard
import com.example.framework.utils.logE
import com.example.multimedia.utils.MultimediaUtil.TAG
import java.io.File
import java.util.Date

/**
 * @description 多媒体工具类
 * @author yan
 */
object MultimediaUtil {
    const val TAG = "MultimediaUtil"

    /**
     * 获取对应文件类型的存储地址
     */
    fun getOutputFile(mimeType: MediaType): File? {
        if (!hasSdcard()) {
            "未找到手机sd卡".logE(TAG)
            return null
        }
        //根据类型在sd卡picture目录下建立对应app名称的对应类型文件
        val storage = "${Constants.APPLICATION_PATH}/手机文件/${AccountHelper.getUserId()}/"
        val storageInfo = when (mimeType) {
            //拍照/抓拍
            MediaType.IMAGE -> "${storage}拍照取证" to "jpg"
            //录像
            MediaType.VIDEO -> "${storage}录像取证" to "mp4"
            //录音
            MediaType.AUDIO -> "${storage}录音取证" to "wav"
            //录屏
            MediaType.SCREEN -> "${storage}录屏取证" to "mp4"
        }
        //先在包名目录下建立对应类型的文件夹，构建失败直接返回null
        val storageDir = File(storageInfo.first)
        if (!storageDir.exists()) {
            "开始创建文件目录\n地址:${storageDir.path}".logE(TAG)
            if (!storageDir.mkdirs()) {
                "创建文件目录失败".logE(TAG)
                return null
            }
        } else "文件目录已创建\n地址:${storageDir.path}".logE(TAG)
        return File("${storageDir.path}/${"yyyyMMdd_HHmmss".convert(Date())}.${storageInfo.second}")
    }

}

/**
 * 传入指定大小的文件长度，扫描sd卡空间是否足够
 * 需有1G的默认大小的空间
 */
fun Context.scanDiskSpace(space: Long = 1024) = getSdcardAvailableCapacity() > space

/**
 * 返回时长(音频，视频)->不支持在线音视频
 * 放在线程中读取，超时会导致卡顿或闪退
 */
fun String?.getDuration(): Int {
    if (isNullOrEmpty()) return 0
    val file = File(this)
    if (!file.exists()) return 0
    val medialPlayer = MediaPlayer()
    medialPlayer.setDataSource(file.absolutePath)
    medialPlayer.prepare()
    val time = medialPlayer.duration//视频时长（毫秒）
    val duration = (time / 1000)
    "文件时长：${duration}秒".logE(TAG)
    return duration
}

/**
 * 定义传入的枚举类型
 */
enum class MediaType {
    IMAGE, VIDEO, AUDIO, SCREEN
}