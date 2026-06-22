package com.example.gallery.feature.album.bean

import android.os.Parcelable
import androidx.annotation.IntDef
import kotlinx.parcelize.Parcelize

/**
 * 相册媒体文件实体类（图片 / 视频）
 * 实现序列化、排序、相等判断，是整个相册库的核心数据模型
 */
@Parcelize
data class AlbumFile(
    var path: String? = null,
    var bucketName: String? = null,
    var mimeType: String? = null,
    var addDate: Long = 0L,
    var latitude: Float = 0f,
    var longitude: Float = 0f,
    var size: Long = 0L,
    var duration: Long = 0L,
    @field:MediaType
    var mediaType: Int = 0,
    var isChecked: Boolean = false,
    var isDisable: Boolean = false
) : Parcelable, Comparable<AlbumFile> {

    /**
     * 类型：图片/视频
     */
    companion object {
        const val TYPE_IMAGE = 1
        const val TYPE_VIDEO = 2

        /**
         * 媒体类型限定注解
         */
        @IntDef(TYPE_IMAGE, TYPE_VIDEO)
        @Retention(AnnotationRetention.SOURCE)
        annotation class MediaType
    }

    /**
     * 按时间排序（最新在前）
     */
    override fun compareTo(other: AlbumFile): Int {
        val time = other.addDate - addDate
        return when {
            time > Int.MAX_VALUE -> Int.MAX_VALUE
            time < -Int.MAX_VALUE -> -Int.MAX_VALUE
            else -> time.toInt()
        }
    }

    /**
     * 比较：根据路径判断是否同一个文件
     */
    override fun equals(other: Any?): Boolean {
        if (other !is AlbumFile) return false
        if (path != null && other.path != null) {
            return path == other.path
        }
        return super.equals(other)
    }

    /**
     * 哈希：路径作为唯一标识
     */
    override fun hashCode(): Int {
        return path?.hashCode() ?: super.hashCode()
    }

}