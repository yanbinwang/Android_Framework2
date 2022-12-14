package com.example.framework.utils

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

/**
 * 清除本应用内部缓存(/data/data/com.xxx.xxx/cache)
 */
fun Context.cleanInternalCache() = deleteFilesByDirectory(cacheDir)

/**
 * 清除本应用所有数据库(/data/data/com.xxx.xxx/databases)
 */
fun Context.cleanDatabases() = deleteFilesByDirectory(File("/data/data/${packageName}/databases"))

/**
 * 清除本应用SharedPreference(/data/data/com.xxx.xxx/shared_prefs)
 */
fun Context.cleanSharedPreference() = deleteFilesByDirectory(File("/data/data/${packageName}/shared_prefs"))

/**
 * 清除/data/data/com.xxx.xxx/files下的内容
 */
fun Context.cleanFiles() = deleteFilesByDirectory(filesDir)

/**
 * 清除外部cache下的内容(/mnt/sdcard/android/data/com.xxx.xxx/cache)
 */
fun Context.cleanExternalCache() = run { if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) deleteFilesByDirectory(externalCacheDir) }

/**
 * 删除方法 这里只会删除某个文件夹下的文件，如果传入的directory是个文件，将不做处理
 */
private fun deleteFilesByDirectory(directory: File?) {
    directory ?: return
    if (directory.exists() && directory.isDirectory) {
        for (item in directory.listFiles().orEmpty()) {
            if (null == item) continue
            if (item.isDirectory) {
                //不删除mmkv文件
                if (item.name == "MMKV" || item.name == "mmkv") continue
                deleteFilesByDirectory(item)
            }
            item.delete()
        }
    }
}

/**
 * 按名字清除本应用数据库
 */
fun Context.cleanDatabaseByName(dbName: String) = deleteDatabase(dbName)

/**
 * 清除本应用所有的数据
 */
fun Context.cleanApplicationData(vararg filepath: String) {
    cleanInternalCache()
    cleanExternalCache()
    cleanDatabases()
    cleanSharedPreference()
    cleanFiles()
    for (filePath in filepath) {
        filePath.cleanCustomCache()
    }
}

/**
 * 清除自定义路径下的文件，使用需小心，请不要误删。而且只支持目录下的文件删除
 */
fun String.cleanCustomCache() = deleteFilesByDirectory(File(this))