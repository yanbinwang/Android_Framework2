package com.dataqin.base.utils

import android.content.Context
import android.os.Environment

import java.io.File

/**
 * author: wyb
 * date: 2018/7/31.
 * 本应用数据清除管理器
 *
 * //获取缓存数量
 * try {
 * if (FileUtil.getFileSize(getContext().getCacheDir()) > 0) {
 * cachSize = FileUtil.FormetFileSize(FileUtil.getFileSize(getContext().getCacheDir()));
 * }
 * view.setText(cachSize);
 * } catch (Exception e) {
 * e.printStackTrace();
 * }
 * view.setText("V "+CheckAppUtil.getVersionName(getContext()));
 *
 * //清理缓存
 * DataCleanManager.cleanInternalCache(getContext());
 * view.setText("0.0M");
 */
object DataCleanManager {

    //清除本应用内部缓存(/data/data/com.xxx.xxx/cache)
    @JvmStatic
    fun cleanInternalCache(context: Context) {
        deleteFilesByDirectory(context.cacheDir)
    }

    //清除本应用所有数据库(/data/data/com.xxx.xxx/databases)
    @JvmStatic
    fun cleanDatabases(context: Context) {
        deleteFilesByDirectory(File("/data/data/" + context.packageName + "/databases"))
    }

    //清除本应用SharedPreference(/data/data/com.xxx.xxx/shared_prefs)
    @JvmStatic
    fun cleanSharedPreference(context: Context) {
        deleteFilesByDirectory(File("/data/data/" + context.packageName + "/shared_prefs"))
    }

    //按名字清除本应用数据库
    @JvmStatic
    fun cleanDatabaseByName(context: Context, dbName: String) {
        context.deleteDatabase(dbName)
    }

    //清除/data/data/com.xxx.xxx/files下的内容
    @JvmStatic
    fun cleanFiles(context: Context) {
        deleteFilesByDirectory(context.filesDir)
    }

    //清除外部cache下的内容(/mnt/sdcard/android/data/com.xxx.xxx/cache)
    @JvmStatic
    fun cleanExternalCache(context: Context) {
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            deleteFilesByDirectory(
                context.externalCacheDir
            )
        }
    }

    //清除自定义路径下的文件，使用需小心，请不要误删。而且只支持目录下的文件删除
    @JvmStatic
    fun cleanCustomCache(filePath: String) {
        deleteFilesByDirectory(
            File(
                filePath
            )
        )
    }

    //清除本应用所有的数据
    @JvmStatic
    fun cleanApplicationData(context: Context, vararg filepath: String) {
        cleanInternalCache(context)
        cleanExternalCache(context)
        cleanDatabases(context)
        cleanSharedPreference(context)
        cleanFiles(context)
        for (filePath in filepath) {
            cleanCustomCache(filePath)
        }
    }

    //删除方法 这里只会删除某个文件夹下的文件，如果传入的directory是个文件，将不做处理
    @JvmStatic
    private fun deleteFilesByDirectory(directory: File) {
        if (directory.exists() && directory.isDirectory) {
            for (item in directory.listFiles()) {
                if (null == item) continue
                //不删除mmkv
                if (item.isDirectory) {
                    if (item.name == "MMKV" || item.name == "mmkv") continue
                    deleteFilesByDirectory(
                        item
                    )
                }
                item.delete()
            }
        }
    }

}
