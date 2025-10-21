package com.example.thirdparty.media.utils

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import com.example.common.utils.ScreenUtil.screenWidth
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.logWTF
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext

/**
 * 视频横竖屏判断工具类
 * 功能：获取视频方向、需旋转角度，处理文件不存在、元数据读取失败等异常
 */
object VideoInfoHelper {
    // 视频方向常量
    const val ORIENTATION_UNKNOWN = -1  // 未知方向
    const val ORIENTATION_PORTRAIT = 0   // 竖屏（高 > 宽）
    const val ORIENTATION_LANDSCAPE = 1  // 横屏（宽 > 高）
    private val TAG = "VideoOrientationUtil"

    /**
     * 获取视频方向及旋转角度
     * // 校验视频文件是否存在
     * if (videoPath == null || !File(videoPath).exists()) {
     *     "视频文件不存在或路径为空：${videoPath}".logWTF(TAG)
     *     return result
     * }
     * val videoInfo = suspendingOrientationAndRotation(url)
     * val videoOrientation = videoInfo[0] // 视频方向
     * val needRotation = videoInfo[1] // 播放器需旋转的角度
     * if (videoOrientation == ORIENTATION_LANDSCAPE) {
     *     lockLand = true
     * }
     * @param videoUrl 视频路径
     * @return int数组：[0]为视频方向（ORIENTATION_*），[1]为播放器需旋转角度（0/90/180/270）
     */
    suspend fun suspendingOrientationAndRotation(videoUrl: String?): IntArray {
        // 初始化返回结果：默认未知方向，旋转0度
        val result = intArrayOf(ORIENTATION_UNKNOWN, 0)
        val retriever = MediaMetadataRetriever()
        return withContext(IO) {
            try {
                // 设置数据源
                retriever.setDataSource(videoUrl)
                // 优先读取旋转元数据
                val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toSafeInt()
                if (rotation != null) {
                    // 记录需旋转角度
                    result[1] = rotation
                    // 根据旋转角度判断方向
                    if (rotation == 90 || rotation == 270) {
                        result[0] = ORIENTATION_PORTRAIT
                    } else if (rotation == 0 || rotation == 180) {
                        // 横屏角度，结合宽高比二次确认（避免元数据异常）
                        result[0] = getOrientationBySize(retriever)
                    }
                    "通过旋转元数据判断：方向=${getOrientationDesc(result[0])}，需旋转=${rotation}度".logWTF(TAG)
                    result
                } else {
                    // 旋转元数据无效，用宽高比兜底判断
                    result[0] = getOrientationBySize(retriever)
                    result[1] = 0 // 宽高比判断时，默认无需旋转（需根据播放器实际渲染调整）
                    "通过宽高比兜底判断：方向=${getOrientationDesc(result[0])}".logWTF(TAG)
                    result
                }
            } catch (e: Exception) {
                "读取视频元数据失败：${e.message}".logWTF(TAG)
                result
            } finally {
                // 释放资源，避免内存泄漏
                retriever.release()
            }
        }
    }

    /**
     * 通过宽高比判断视频方向
     * @param retriever MediaMetadataRetriever实例（已设置数据源）
     * @return 视频方向（ORIENTATION_*）
     */
    private fun getOrientationBySize(retriever: MediaMetadataRetriever): Int {
        try {
            val rawInfo = getMediaDimensions(retriever)
            val rawWidth = rawInfo[0]
            val rawHeight = rawInfo[1]
            return if (rawWidth > rawHeight) ORIENTATION_LANDSCAPE else ORIENTATION_PORTRAIT
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

    /**
     * 在 Activity/Fragment 中调用（需在主线程获取屏幕宽度）
     */
    suspend fun suspendingCalculateHeight(videoUrl: String, targetWidth: Int = screenWidth): Int {
        return withContext(Main.immediate) {
            // 获取视频宽高比
            val videoRatio = getDisplayAspectRatio(videoUrl)
            // 计算目标高度（确保为正数）
            val targetHeight = (targetWidth / videoRatio).toInt().coerceAtLeast(1)
            // 返回换算高度
            targetHeight
        }
    }

    /**
     * 获取视频实际显示的宽高比（已处理旋转角度）
     * @return 宽高比（宽/高），如 16:9 返回 1.777...，9:16 返回 0.5625
     */
    private suspend fun getDisplayAspectRatio(videoUrl: String): Float {
        val retriever = MediaMetadataRetriever()
        return withContext(IO) {
            try {
                // 设置数据源
                retriever.setDataSource(videoUrl)
                // 获取宽高信息
                val rawInfo = getMediaDimensions(retriever)
                // 获取视频文件本身的宽、高（未考虑旋转）
                val rawWidth = rawInfo[0]
                val rawHeight = rawInfo[1]
                // 获取视频旋转角度（解决竖屏录制视频的比例问题）
                val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toSafeInt()
                // 根据旋转角度计算实际显示的宽高比
                if (rotation == 90 || rotation == 270) {
                    // 旋转90/270度：宽高互换（比如原 1080×1920 → 实际显示 1920×1080，比例 16:9）
                    rawHeight.toSafeFloat() / rawWidth.toSafeFloat()
                } else {
                    // 0/180度：正常宽高比（比如 1920×1080 → 比例 16:9）
                    rawWidth.toSafeFloat() / rawHeight.toSafeFloat()
                }
            } catch (e: Exception) {
                // 网络异常、URL 无效、视频格式不支持等情况返回 0f
                e.printStackTrace()
                0f
            } finally {
                // 必须释放，避免内存泄漏
                retriever.release()
            }
        }
    }

    /**
     * 获取视频缩略图
     * @param videoUrl 视频文件路径（本地路径或网络URL）
     * @param timeUs 截取帧的时间点（微秒，如 1000000 表示 1秒处）
     * @return 缩略图 Bitmap，失败返回 null
     */
    suspend fun suspendingThumbnail(videoUrl: String, timeUs: Long = 1000000L): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return withContext(IO) {
            try {
                // 设置数据源
                retriever.setDataSource(videoUrl)
                // 获取指定时间点的帧（返回 Bitmap）OPTION_CLOSEST 表示取最接近该时间的帧
                retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST)
            } catch (e: Exception) {
                "读取视频元数据失败：${e.message}".logWTF(TAG)
                null
            } finally {
                // 释放资源，避免内存泄漏
                retriever.release()
            }
        }
    }

    /**
     * 获取视频的宽高
     */
    private fun getMediaDimensions(retriever: MediaMetadataRetriever): IntArray {
        return try {
            intArrayOf(
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                    ?.toSafeInt().orZero,
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                    ?.toSafeInt().orZero
            )
        } catch (e: Exception) {
            throw e
        }
    }

}