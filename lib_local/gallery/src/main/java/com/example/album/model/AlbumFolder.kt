package com.example.album.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 相册文件夹实体类
 * 代表手机里的一个图片/相册文件夹，包含文件夹名称、图片列表、选中状态
 */
@Parcelize
data class AlbumFolder(
    var isChecked: Boolean = false, // 相册文件夹是否被选中（在文件夹切换列表中标记当前选中项）
    var name: String? = null, // 文件夹名称（例如：Camera、Screenshots、微信）
    var albumFiles: ArrayList<AlbumFile> = ArrayList() // 该文件夹下的图片/视频文件列表
) : Parcelable {

    fun addAlbumFile(albumFile: AlbumFile) {
        albumFiles.add(albumFile)
    }

}