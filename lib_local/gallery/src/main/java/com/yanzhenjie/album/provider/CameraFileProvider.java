package com.yanzhenjie.album.provider;

import android.content.Context;

import androidx.core.content.FileProvider;

/**
 * 相机文件 FileProvider 扩展类
 * 用于 Android 7.0+ 安全地对外提供文件访问（解决相机拍照文件Uri异常）
 */
public class CameraFileProvider extends FileProvider {

    /**
     * 获取当前应用的 FileProvider 主机名
     * 规则：包名 + .app.file.provider（与 AndroidManifest.xml 中配置一致）
     */
    public static String getProviderName(Context context) {
        return context.getPackageName() + ".app.file.provider";
    }

}