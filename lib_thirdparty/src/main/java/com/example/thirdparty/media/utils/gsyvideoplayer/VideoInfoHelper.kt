package com.example.thirdparty.media.utils.gsyvideoplayer

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import com.example.common.utils.ScreenUtil.screenWidth
import com.example.framework.utils.function.value.matches
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.logWTF
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 视频横竖屏判断工具类
 * 功能：获取视频方向、需旋转角度，处理文件不存在、元数据读取失败等异常
 */
object VideoInfoHelper {
    private const val TAG = "VideoInfoHelper"
    // 视频方向常量
    const val ORIENTATION_UNKNOWN = -1   // 未知方向
    const val ORIENTATION_PORTRAIT = 0   // 竖屏（高 > 宽）
    const val ORIENTATION_LANDSCAPE = 1  // 横屏（宽 > 高）

    /**
     * 获取视频方向及旋转角度
     * @param context 上下文（建议用 ApplicationContext）
     * @param videoSource 视频源（支持 String 路径/URL、Uri、File）
     * @return int数组：[0]为视频方向（ORIENTATION_*），[1]为播放器需旋转角度（0/90/180/270）
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
     */
    suspend fun suspendingOrientationAndRotation(context: Context, videoSource: Any): IntArray {
        // 初始化返回结果：默认未知方向，旋转0度
        val result = intArrayOf(ORIENTATION_UNKNOWN, 0)
        val retriever = MediaMetadataRetriever()
        return withContext(IO) {
            try {
                // 调用独立的 setDataSource 函数，失败直接返回 null
                if (!setDataSource(context, retriever, videoSource)) return@withContext result
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
     * 计算视频目标高度（根据目标宽度和视频宽高比）
     * @param context 上下文
     * @param videoSource 视频源
     * @param targetWidth 目标宽度（默认屏幕宽度）
     * @return 适配后的目标高度（最小为 1，避免异常）
     */
    suspend fun suspendingCalculateHeight(context: Context, videoSource: Any, targetWidth: Int = screenWidth): Int {
        if (targetWidth <= 0) {
            "目标宽度无效：$targetWidth".logWTF(TAG)
            return 1
        }
        return withContext(Main.immediate) {
            // 获取视频宽高比
            val videoRatio = getDisplayAspectRatio(context, videoSource)
            // 计算目标高度（确保为正数）
            val targetHeight = (targetWidth / videoRatio).toInt().coerceAtLeast(1)
            // 返回换算高度
            targetHeight
        }
    }

    /**
     * 获取视频实际显示宽高比（已处理旋转角度）
     * @return 宽高比（宽/高），如 16:9→1.777，9:16→0.5625
     */
    private suspend fun getDisplayAspectRatio(context: Context, videoSource: Any): Float {
        val retriever = MediaMetadataRetriever()
        return withContext(IO) {
            try {
                // 调用独立的 setDataSource 函数，失败直接返回 null
                if (!setDataSource(context, retriever, videoSource)) return@withContext 0f
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
     * @param context 上下文（建议用 ApplicationContext 避免内存泄漏）
     * @param videoSource 视频源（支持 String 路径/URL、Uri、File、MediaItem）
     * @param timeUs 目标时间戳（微秒，默认1秒）
     * @return 提取的视频帧 Bitmap，失败返回 null
     * 网络视频：需在 AndroidManifest 中添加 <uses-permission android:name="android.permission.INTERNET" />。
     * 本地视频（Android 12-）：需添加 <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />。
     * 本地视频（Android 13+）：需添加 <uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />，并动态申请
     */
    suspend fun suspendingThumbnail(context: Context, videoSource: Any, timeUs: Long = 1000000L): Bitmap? {
        // 提前校验参数（避免无效操作）
        if (timeUs < 0) {
            "时间戳不能为负数：$timeUs".logWTF(TAG)
            return null
        }
        val retriever = MediaMetadataRetriever()
        return withContext(IO) {
            try {
                // 调用独立的 setDataSource 函数，失败直接返回 null
                if (!setDataSource(context, retriever, videoSource)) return@withContext null
                // 帧提取策略（兼容 API 23+，解决 null 问题）
                val frame = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                        // API 24+：优先精准同步帧，失败降级到下一帧
                        val closestSyncFrame = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                        closestSyncFrame ?: retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_NEXT_SYNC)
                    }
                    else -> {
                        // API 23-：低版本容错，尝试前后 500ms 帧
                        var frame = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST)
                        if (frame == null) {
                            frame = retriever.getFrameAtTime(timeUs + 500_000, MediaMetadataRetriever.OPTION_CLOSEST)
                            if (frame == null) {
                                frame = retriever.getFrameAtTime(timeUs - 500_000, MediaMetadataRetriever.OPTION_CLOSEST)
                            }
                        }
                        frame
                    }
                } ?: run {
                    "无法提取时间点 $timeUs us 的视频帧".logWTF(TAG)
                    return@withContext null
                }
                // 无需压缩，直接返回
                frame
            } catch (e: Exception) {
                "读取视频元数据失败：${e.message ?: e.toString()}".logWTF(TAG)
                null
            } finally {
                // 释放资源，避免内存泄漏
                retriever.release()
            }
        }
    }

    /**
     * 独立设置视频源（捕获分支内异常，确保返回 Boolean 不崩溃）
     * @return 成功设置返回 true，失败返回 false
     */
    private fun setDataSource(context: Context, retriever: MediaMetadataRetriever, videoSource: Any): Boolean {
        return try {
            when (videoSource) {
                is String -> {
                    if (videoSource.matches("^https?://.*")) {
                        // 网络视频：捕获无效 URL、网络异常等情况
                        retriever.setDataSource(videoSource, hashMapOf())
                        true
                    } else {
                        val file = File(videoSource)
                        if (!file.exists() || !file.canRead()) {
                            "本地视频文件不存在或无读取权限：$videoSource".logWTF(TAG)
                            return false
                        }
                        retriever.setDataSource(videoSource)
                        true
                    }
                }
                is Uri -> {
                    // 捕获 Uri 无效、无权限等异常
                    retriever.setDataSource(context, videoSource)
                    true
                }
                is File -> {
                    if (!videoSource.exists() || !videoSource.canRead()) {
                        "文件不存在或无读取权限：${videoSource.path}".logWTF(TAG)
                        return false
                    }
                    retriever.setDataSource(videoSource.path)
                    true
                }
                else -> {
                    "不支持的视频源类型：${videoSource.javaClass.simpleName}".logWTF(TAG)
                    false
                }
            }
        } catch (e: Exception) {
            // 捕获分支内的异常（如无效 URL、Uri 权限不足、格式不支持等）
            "设置视频源失败：${videoSource.javaClass.simpleName} - ${e.message ?: e.toString()}".logWTF(TAG)
            false
        }
    }

    /**
     * 获取视频的宽高
     */
    private fun getMediaDimensions(retriever: MediaMetadataRetriever): IntArray {
        return try {
            intArrayOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toSafeInt().orZero, retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toSafeInt().orZero)
        } catch (e: Exception) {
            throw e
        }
    }

}