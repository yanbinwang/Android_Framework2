package com.example.thirdparty.media.utils

import android.media.MediaMetadataRetriever
import com.example.framework.utils.logWTF
import java.io.File
import kotlin.text.isEmpty
import kotlin.text.toInt


/**
 * 视频横竖屏判断工具类
 * 功能：获取视频方向、需旋转角度，处理文件不存在、元数据读取失败等异常
 */
object VideoOrientationUtil {
    // 视频方向常量
    const val ORIENTATION_UNKNOWN = -1 // 未知方向
    const val ORIENTATION_PORTRAIT = 0 // 竖屏
    const val ORIENTATION_LANDSCAPE = 1 // 横屏
    private val TAG = "VideoOrientationUtil"

    /**
     * 获取视频方向及旋转角度
     * @param videoPath 视频文件路径
     * @return int数组：[0]为视频方向（ORIENTATION_*），[1]为播放器需旋转角度（0/90/180/270）
     */
    @JvmStatic
    fun getVideoOrientationAndRotation(videoPath: String?): IntArray {
        // 初始化返回结果：默认未知方向，旋转0度
        val result = intArrayOf(ORIENTATION_UNKNOWN, 0)
        var retriever: MediaMetadataRetriever? = null
//        // 校验视频文件是否存在
//        if (videoPath == null || !File(videoPath).exists()) {
//            "视频文件不存在或路径为空：${videoPath}".logWTF(TAG)
//            return result
//        }
        try {
            retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoPath)
            // 优先读取旋转元数据
            val rotationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
            if (rotationStr != null && !rotationStr.isEmpty()) {
                val rotation = rotationStr.toInt()
                // 记录需旋转角度
                result[1] = rotation
                // 根据旋转角度判断方向
                if (rotation == 90 || rotation == 270) {
                    result[0] = ORIENTATION_PORTRAIT
                } else if (rotation == 0 || rotation == 180) {
                    // 横屏角度，结合宽高比二次确认（避免元数据异常）
                    result[0] = getOrientationByWH(retriever)
                }
                "通过旋转元数据判断：方向=${getOrientationDesc(result[0])}，需旋转=${rotation}度".logWTF(TAG)
                return result
            }
            // 旋转元数据无效，用宽高比兜底判断
            result[0] = getOrientationByWH(retriever)
            result[1] = 0 // 宽高比判断时，默认无需旋转（需根据播放器实际渲染调整）
            "通过宽高比兜底判断：方向=${getOrientationDesc(result[0])}".logWTF(TAG)
        } catch (e: Exception) {
            "读取视频元数据失败：${e.message}".logWTF(TAG)
        } finally {
            // 释放资源，避免内存泄漏
            try {
                retriever?.release()
            } catch (e: Exception) {
                "释放MediaMetadataRetriever失败：${e.message}".logWTF(TAG)
            }
        }
        return result
    }

    /**
     * 通过宽高比判断视频方向
     * @param retriever MediaMetadataRetriever实例（已设置数据源）
     * @return 视频方向（ORIENTATION_*）
     */
    private fun getOrientationByWH(retriever: MediaMetadataRetriever): Int {
        try {
            val widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            if (widthStr != null && heightStr != null) {
                val width = widthStr.toInt()
                val height = heightStr.toInt()
                return if (width > height) ORIENTATION_LANDSCAPE else ORIENTATION_PORTRAIT
            }
        } catch (e: Exception) {
            "读取视频宽高失败：${e.message}".logWTF(TAG)
        }
        return ORIENTATION_UNKNOWN
    }

    /**
     * 转换方向常量为文字描述（方便日志打印）
     * @param orientation 视频方向（ORIENTATION_*）
     * @return 方向描述文字
     */
    private fun getOrientationDesc(orientation: Int): String {
        return when (orientation) {
            ORIENTATION_PORTRAIT -> "竖屏"
            ORIENTATION_LANDSCAPE -> "横屏"
            else -> "未知"
        }
    }

}