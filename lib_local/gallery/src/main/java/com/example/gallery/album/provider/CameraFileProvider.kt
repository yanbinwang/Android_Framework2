package com.example.gallery.album.provider

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * 相机文件 FileProvider 扩展类
 * 用于 Android 7.0+ 安全地对外提供文件访问（解决相机拍照文件Uri异常）
 */
class CameraFileProvider : FileProvider() {

    companion object {
        /**
         * 1) getUriForFile 是 父类 FileProvider 里的静态方法 [Java 允许子类调用父类静态方法 Kotlin 不允许]
         * 2) authority 是获取当前应用的 FileProvider 主机名 [包名 + .app.file.provider（与 AndroidManifest.xml 中配置一致）]
         */
        fun getUriForFile(context: Context, file: File): Uri {
            val authority = "${context.packageName}.app.file.provider"
            return getUriForFile(context, authority, file)
        }
    }

}