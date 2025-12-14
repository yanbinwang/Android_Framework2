package com.example.thirdparty.media.oss.bean

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

    companion object {
        /**
         * 获取上传证据时服务器文件夹名称
         */
        @JvmStatic
        @Synchronized
        fun objectName(fileType: String, localFilepath: String = ""): String {
            val date = DateFormat.EN_YMDHMS.convert(System.currentTimeMillis())
            return when (fileType) {
                //拍照
                "1" -> "app/photo/android/${AccountHelper.getUserId()}_${date}_${UUID.randomUUID()}.jpg"
                //录像/录屏
                "2", "4" -> "app/video/android/${AccountHelper.getUserId()}_${date}_${UUID.randomUUID()}.mp4"
                //录音
                "3" -> "app/record/android/${AccountHelper.getUserId()}_${date}_${UUID.randomUUID()}.wav"
                //普通文件上传
                else -> "app/file/android/${AccountHelper.getUserId()}_${date}_${UUID.randomUUID()}_${File(localFilepath).name}"
            }
        }

        /**
         * 获取上传证据时服务器地址名
         * resume:是否是断点续传
         * privately:非断点续传情况下，服务器oss文件位置需要管控
         */
        @JvmStatic
        @Synchronized
        fun bucketName(resume: Boolean = true, privately: Boolean = false): String {
            return if (isDebug) {
                "test-eagle"
            } else {
                if (resume) {
                    "baoquan-v1"
                } else {
                    if (privately) {
                        "baoquan-v1"
                    } else {
                        "baoquan-p1"
                    }
                }
            }
        }
    }

}
