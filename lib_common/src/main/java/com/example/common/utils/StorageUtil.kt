package com.example.common.utils

import android.content.Context
import android.media.MediaPlayer
import com.example.common.utils.helper.AccountHelper.STORAGE
import com.example.framework.utils.function.value.convert
import com.example.framework.utils.function.value.divide
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.getSdcardAvailableCapacity
import com.example.framework.utils.hasSdcard
import com.example.framework.utils.logE
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date

/**
 * @description 多媒体工具类
 * @author yan
 */
object StorageUtil {

    /**
     * 定义传入的枚举类型
     */
    enum class StorageType {
        IMAGE, VIDEO, AUDIO, SCREEN
    }

    /**
     * 获取对应文件类型的存储地址
     */
    @JvmStatic
    fun getOutputFile(mimeType: StorageType): File? {
        if (!hasSdcard()) {
            "未找到手机sd卡".logE()
            return null
        }
        //根据类型在sd卡picture目录下建立对应app名称的对应类型文件
        val storageInfo = getStorageInfo(mimeType)
        //先在包名目录下建立对应类型的文件夹，构建失败直接返回null
        val storageDir = File(storageInfo.first)
        if (!storageDir.exists()) {
            "开始创建文件目录\n地址:${storageDir.path}".logE()
            if (!storageDir.mkdirs()) {
                "创建文件目录失败".logE()
                return null
            }
        } else {
            "文件目录已创建\n地址:${storageDir.path}".logE()
        }
        return File("${storageDir.path}/${"yyyyMMdd_HHmmss".convert(Date())}.${storageInfo.second}")
    }

    /**
     * 获取输出文件的路径
     */
    @JvmStatic
    fun getStorageInfo(mimeType: StorageType): Pair<String, String> {
        return when (mimeType) {
            //拍照/抓拍
            StorageType.IMAGE -> "${STORAGE}/拍照" to "jpg"
            //录像
            StorageType.VIDEO -> "${STORAGE}/录像" to "mp4"
            //录音
            StorageType.AUDIO -> "${STORAGE}/录音" to "wav"
            //录屏
            StorageType.SCREEN -> "${STORAGE}/录屏" to "mp4"
        }
    }

    /**
     * 获取用户id命名的文件夹下的文件
     * "${Constants.APPLICATION_PATH}/手机文件/${getUserId()}"
     */
    @JvmStatic
    fun getStoragePath(fileName: String): String {
        return "${STORAGE}/${fileName}"
    }

}

/**
 * 传入指定大小的文件长度，扫描sd卡空间是否足够
 * 需有1G的默认大小的空间
 */
fun Context.scanDisk(space: Long = 1024) = getSdcardAvailableCapacity() > space

/**
 * 返回时长(音频，视频)->不支持在线音视频
 * 放在线程中读取，超时会导致卡顿或闪退
 */
suspend fun String?.mediaTime(): Int {
    val sourcePath = this
    if (null == sourcePath || !File(sourcePath).exists()) return 0
    return withContext(IO) {
        val medialPlayer = MediaPlayer()
        medialPlayer.setDataSource(sourcePath)
        medialPlayer.prepare()
        val millisecond = medialPlayer.duration//视频时长（毫秒）
        val second = (millisecond.toString()).divide("1000").toSafeInt()
        "文件时长：${second}秒".logE()
        second
    }
}