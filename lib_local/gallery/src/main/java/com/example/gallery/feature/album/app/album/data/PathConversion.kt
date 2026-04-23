package com.example.gallery.feature.album.app.album.data

import android.media.MediaPlayer
import com.example.gallery.feature.album.callback.Filter
import com.example.gallery.feature.album.model.AlbumFile
import com.example.gallery.feature.album.utils.AlbumUtil
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 路径转换器
 * 功能：将一个【图片/视频路径】转换成相册可识别的 AlbumFile 对象
 * 主要用于拍照/录像后的路径解析、外部传入路径解析
 */
class PathConversion(
    private val sizeFilter: Filter<Long>?, // 文件大小过滤器
    private val mimeFilter: Filter<String>?, // 文件类型过滤器
    private val durationFilter: Filter<Long>? // 视频时长过滤器
) {

    /**
     * 将文件路径 转换为 AlbumFile
     * 必须在子线程执行（因为有视频时长解析）
     */
    suspend fun convert(filePath: String): AlbumFile {
        return withContext(IO) {
            val file = File(filePath)
            // 创建实体类，设置基础信息
            val albumFile = AlbumFile()
            albumFile.path = filePath
            // 获取文件夹名称
            val parentFile = file.parentFile
            albumFile.bucketName = parentFile?.name
            // 获取文件类型（image/jpeg、video/mp4...）
            val mimeType = AlbumUtil.getMimeType(filePath)
            albumFile.mimeType = mimeType
            // 设置添加时间（当前时间）
            val nowTime = System.currentTimeMillis()
            albumFile.addDate = nowTime
            // 设置文件大小
            albumFile.size = file.length()
            // 判断是图片还是视频
            var mediaType = 0
            if (mimeType.isNotEmpty()) {
                // 视频
                if (mimeType.contains("video")) {
                    mediaType = AlbumFile.TYPE_VIDEO
                }
                // 图片
                if (mimeType.contains("image")) {
                    mediaType = AlbumFile.TYPE_IMAGE
                }
            }
            albumFile.mediaType = mediaType
            // 应用过滤规则（不符合则置为不可选）
            if (sizeFilter != null && sizeFilter.filter(file.length())) {
                albumFile.isDisable = true
            }
            if (mimeFilter != null && mimeFilter.filter(mimeType)) {
                albumFile.isDisable = true
            }
            // 如果是视频，获取时长
            if (mediaType == AlbumFile.TYPE_VIDEO) {
                var player: MediaPlayer? = null
                try {
                    player = MediaPlayer()
                    player.setDataSource(filePath)
                    player.prepare()
                    albumFile.duration = player.duration.toLong()
                } catch (_: Exception) {
                    // 异常忽略
                    albumFile.duration = 0
                } finally {
                    // 释放播放器，防止内存泄漏
                    player?.release()
                }
                // 视频时长过滤
                if (durationFilter != null && durationFilter.filter(albumFile.duration)) {
                    albumFile.isDisable = true
                }
            }
            // 返回封装好的实体
            albumFile
        }
    }

}