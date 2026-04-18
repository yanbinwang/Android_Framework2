package com.yanzhenjie.album.app.album.data

import android.content.Context
import android.provider.MediaStore
import com.example.gallery.R
import com.yanzhenjie.album.callback.Filter
import com.yanzhenjie.album.model.AlbumFile
import com.yanzhenjie.album.model.AlbumFolder
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

/**
 * 系统媒体扫描器
 * 功能：扫描手机里的 图片 / 视频 / 全部媒体 并按文件夹分类，返回给相册使用
 */
class MediaReader(
    private val context: Context, // 上下文
    private val sizeFilter: Filter<Long>?, // 文件大小过滤器
    private val mimeFilter: Filter<String>?, // 文件类型过滤器
    private val durationFilter: Filter<Long>?, // 视频时长过滤器
    private val filterVisibility: Boolean // 过滤后的文件是否显示（true=显示但禁用，false=直接隐藏）
) {

    companion object {
        // 需要查询的【图片】字段
        private val IMAGES = arrayOf(
            MediaStore.Images.Media.DATA,  // 路径
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,  // 文件夹名
            MediaStore.Images.Media.MIME_TYPE,  // 类型
            MediaStore.Images.Media.DATE_ADDED,  // 添加时间
            MediaStore.Images.Media.LATITUDE,  // 纬度
            MediaStore.Images.Media.LONGITUDE,  // 经度
            MediaStore.Images.Media.SIZE // 大小
        )

        // 需要查询的【视频】字段
        private val VIDEOS = arrayOf(
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.LATITUDE,
            MediaStore.Video.Media.LONGITUDE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION // 视频时长
        )
    }

    /**
     * 获取【所有图片】（按文件夹分类）
     */
    suspend fun getAllImage(): ArrayList<AlbumFolder> {
        return withContext(IO) {
            val folderMap = hashMapOf<String, AlbumFolder>()
            val allFolder = AlbumFolder().apply {
                isChecked = true
                name = context.getString(R.string.album_all_images)
            }
            scanImageFile(folderMap, allFolder)
            buildFolderList(allFolder, folderMap)
        }
    }

    /**
     * 获取【所有视频】（按文件夹分类）
     */
    suspend fun getAllVideo(): ArrayList<AlbumFolder> {
        return withContext(IO) {
            val folderMap = hashMapOf<String, AlbumFolder>()
            val allFolder = AlbumFolder().apply {
                isChecked = true
                name = context.getString(R.string.album_all_videos)
            }
            scanVideoFile(folderMap, allFolder)
            buildFolderList(allFolder, folderMap)
        }
    }

    /**
     * 获取【所有媒体：图片 + 视频】
     */
    suspend fun getAllMedia(): ArrayList<AlbumFolder> {
        return withContext(IO) {
            val folderMap = hashMapOf<String, AlbumFolder>()
            val allFolder = AlbumFolder().apply {
                isChecked = true
                name = context.getString(R.string.album_all_images_videos)
            }
            scanImageFile(folderMap, allFolder)
            scanVideoFile(folderMap, allFolder)
            buildFolderList(allFolder, folderMap)
        }
    }

    /**
     * 统一构建文件夹列表
     */
    private fun buildFolderList(allFolder: AlbumFolder, folderMap: Map<String, AlbumFolder>): ArrayList<AlbumFolder> {
        val folders = arrayListOf<AlbumFolder>()
        allFolder.albumFiles.sort()
        folders.add(allFolder)
        folderMap.values.forEach { folder ->
            folder.albumFiles.sort()
            folders.add(folder)
        }
        return folders
    }

    /**
     * 扫描系统【图片】
     */
    private suspend fun scanImageFile(albumFolderMap: MutableMap<String, AlbumFolder>, allFileFolder: AlbumFolder) {
        withContext(IO) {
            val contentResolver = context.contentResolver
            val cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGES, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC")
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val path = cursor.getString(0)
                    val bucketName = cursor.getString(1)
                    val mimeType = cursor.getString(2)
                    val addDate = cursor.getLong(3)
                    val latitude = cursor.getFloat(4)
                    val longitude = cursor.getFloat(5)
                    val size = cursor.getLong(6)
                    // 封装成 AlbumFile
                    val imageFile = AlbumFile()
                    imageFile.mediaType = AlbumFile.TYPE_IMAGE
                    imageFile.path = path
                    imageFile.bucketName = bucketName
                    imageFile.mimeType = mimeType
                    imageFile.addDate = addDate
                    imageFile.latitude = latitude
                    imageFile.longitude = longitude
                    imageFile.size = size
                    // 过滤
                    applyFilters(imageFile, size, mimeType, 0, 0)
                    // 添加到“全部图片”文件夹
                    allFileFolder.addAlbumFile(imageFile)
                    // 添加到对应子文件夹
                    var albumFolder = albumFolderMap[bucketName]
                    if (albumFolder != null) {
                        albumFolder.addAlbumFile(imageFile)
                    } else {
                        albumFolder = AlbumFolder()
                        albumFolder.name = bucketName
                        albumFolder.addAlbumFile(imageFile)
                        albumFolderMap[bucketName] = albumFolder
                    }
                }
                cursor.close()
            }
        }
    }

    /**
     * 扫描系统【视频】
     */
    private suspend fun scanVideoFile(albumFolderMap: MutableMap<String, AlbumFolder>, allFileFolder: AlbumFolder) {
        withContext(IO) {
            val contentResolver = context.contentResolver
            val cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, VIDEOS, null, null, MediaStore.Video.Media.DATE_ADDED + " DESC")
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val path = cursor.getString(0)
                    val bucketName = cursor.getString(1)
                    val mimeType = cursor.getString(2)
                    val addDate = cursor.getLong(3)
                    val latitude = cursor.getFloat(4)
                    val longitude = cursor.getFloat(5)
                    val size = cursor.getLong(6)
                    val duration = cursor.getLong(7)
                    // 封装成 AlbumFile
                    val videoFile = AlbumFile()
                    videoFile.mediaType = AlbumFile.TYPE_VIDEO
                    videoFile.path = path
                    videoFile.bucketName = bucketName
                    videoFile.mimeType = mimeType
                    videoFile.addDate = addDate
                    videoFile.latitude = latitude
                    videoFile.longitude = longitude
                    videoFile.size = size
                    videoFile.duration = duration
                    // 过滤
                    applyFilters(videoFile, size, mimeType, duration, 1)
                    // 添加到“全部视频”文件夹
                    allFileFolder.addAlbumFile(videoFile)
                    // 添加到对应子文件夹
                    var albumFolder = albumFolderMap[bucketName]
                    if (albumFolder != null) {
                        albumFolder.addAlbumFile(videoFile)
                    } else {
                        albumFolder = AlbumFolder()
                        albumFolder.name = bucketName
                        albumFolder.addAlbumFile(videoFile)
                        albumFolderMap[bucketName] = albumFolder
                    }
                }
                cursor.close()
            }
        }
    }

    /**
     * type: 0 图片  1 视频
     * 只有视频才会走 duration 过滤
     */
    private fun applyFilters(file: AlbumFile, size: Long, mimeType: String, duration: Long, type: Int = 0) {
        var isFiltered = false
        // 大小过滤
        if (sizeFilter?.filter(size) == true) isFiltered = true
        // 类型过滤
        if (mimeFilter?.filter(mimeType) == true) isFiltered = true
        // 时长过滤
        if (type != 0 && durationFilter?.filter(duration) == true) isFiltered = true
        if (isFiltered) {
            if (!filterVisibility) return
            file.isDisable = true
        }
    }

}