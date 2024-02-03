package com.example.mvvm.utils.oss

import com.example.common.utils.helper.AccountHelper
import com.example.framework.utils.function.value.DateFormat
import com.example.framework.utils.function.value.convert
import com.example.framework.utils.function.value.isDebug
import java.io.File
import java.util.UUID

/**
 * 阿里oss对象
 */
data class OssSts(
    var accessKeyId: String? = null,
    var accessKeySecret: String? = null,
    var expiration: String? = null,
    var securityToken: String? = null
) {
    /**
     * 获取上传证据时服务器文件夹名称
     */
    fun objectName(fileType: String): String {
        val date = DateFormat.EN_YMDHMS.convert(System.currentTimeMillis())
        return when (fileType) {
            "1" -> "app/photo/android/${AccountHelper.getUserId()}_${date}_${UUID.randomUUID()}.jpg"
            "2", "4" -> "app/video/android/${AccountHelper.getUserId()}_${date}_${UUID.randomUUID()}.mp4"
            "3" -> "app/record/android/${AccountHelper.getUserId()}_${date}_${UUID.randomUUID()}.wav"
            else -> ""
        }
    }

    /**
     * 获取上传证据时服务器地址名
     */
    fun bucketName(): String {
        return if (isDebug) "test-eagle" else "baoquan-v1"
    }

    fun objectNameByFile(localFilepath: String): String {
        val date = DateFormat.EN_YMDHMS.convert(System.currentTimeMillis())
        return "app/file/android/${AccountHelper.getUserId()}_${date}_${UUID.randomUUID()}_${File(localFilepath).name}"
    }

    fun bucketNameByFile(privately: Boolean): String {
        return if (isDebug) "test-eagle" else { if (!privately) "baoquan-p1" else "baoquan-v1" }
    }
}
