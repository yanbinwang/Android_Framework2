package com.example.framework.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs

/**
 * https://blog.csdn.net/qq_24580361/article/details/99958688
 *
 * 1、外部存储卡(SD卡)SD卡的根目录 --> 目录为/storage/emulated/0
 * 代码实例：Environment.getExternalStorageDirectory()
 * 1）获取的是外部存储的根目录，自从 Android 10（API 级别 29）起，对外部存储访问的限制更加严格，Google 推荐使用 getExternalFilesDir() 或 getExternalCacheDir() 方法来访问应用专属的目录
 * 2）当前目录下创建的文件不会因程序卸载被清除掉
 *
 * 2、目录为 /storage/emulated/0/Android/data/packagename/cache
 * 代码实例：getExternalCacheDir()
 * 1）packagename为应用包名
 * 2）只有手机系统使用的是虚拟外部存储（虚拟SD卡）的时候才可获取，需要hasSdcard做判断
 * 3）需要申请读写权限（READ_EXTERNAL_STORAGE， WRITE_EXTERNAL_STORAGE）
 * 4）直接在文件系统下Android/data/packagename能看到。如果应用卸载，该目录下的文件会被删除掉
 *
 * 3、目录为 /storage/emulated/0/Android/data/packagename/files
 * 代码实例：getExternalFilesDir(null)
 * 1）packagename为应用包名
 * 2）只有手机系统使用的是虚拟外部存储（虚拟SD卡）的时候才可获取，需要hasSdcard做判断
 * 3）需要申请读写权限（READ_EXTERNAL_STORAGE， WRITE_EXTERNAL_STORAGE）
 * 4）直接在文件系统下Android/data/packagename能看到。如果应用卸载，该目录下的文件会被删除掉
 *
 * 4、内部存储(手机内部存储) --> 目录为 /data
 * 代码实例：getDataDir()
 * 1）通常位于设备的内部存储中，包含了应用的数据和数据库文件
 * 2）File dataDir = Environment.getDataDirectory();
 *   String dirName = dataDir.getAbsolutePath();
 *   // result is: /data/data/包名
 *   指向/data/data/包名目录。这个目录是专为应用设计的，只有该应用可以访问其中的内容。当用户卸载应用时，系统会自动删除这个目录及其中的所有文件
 *
 * 5、应用缓存目录 /data/data/packagename/cache
 * 代码实例：getCacheDir()
 * 1）不需要申请权限
 * 2）必须是root的手机在文件操作系统中才能看到。如果在应用程序中清空数据或者卸载应用，目录下的文件也将会被清空
 *
 * 6、应用文件目录 /data/data/packagename/files
 * 代码实例：getFilesDir()
 * 1）不需要申请权限
 * 2）必须是root的手机在文件操作系统中才能看到。如果在应用程序中清空数据或者卸载应用，目录下的文件也将会被清空
 *
 * 注：2和3直接获取路径不需要权限，但使用其中文件（保存，读写）是需要权限的，并且需要先判断是否具备sd卡
 */
//------------------------------------sd卡工具类(只能拿到外置存储器的大致容量，并不准确)------------------------------------
/**
 * 判断sd卡是否存在
 */
fun hasSdcard() = Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

/**
 * 获取sd卡目录-绝对路径
 */
fun Context.getSdcardAbsolutePath(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        getExternalFilesDir(null)?.absolutePath.orEmpty()
    } else {
        Environment.getExternalStorageDirectory().absolutePath
    }
}

/**
 * 获取sd卡目录-相对路径
 */
fun Context.getSdcardPath(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        getExternalFilesDir(null)?.path.orEmpty()
    } else {
        Environment.getExternalStorageDirectory().path
    }
}

/**
 * 获取sd卡存储空间类
 */
fun Context.getSdcardStatFs() = StatFs(getSdcardPath())

/**
 * 获得内置sd卡总容量，单位M
 */
fun Context.getSdcardTotalCapacity(): Long {
    //获得sdcard上 block的总数
    val blockCount = getSdcardStatFs().blockCountLong
    //获得sdcard上每个block 的大小
    val blockSize = getSdcardStatFs().blockSizeLong
    return (blockCount * blockSize) / 1024 / 1024
}

/**
 * 获得内置sd卡可用容量，即可用大小，单位M
 */
fun Context.getSdcardAvailableCapacity(): Long {
    //获得sdcard上 block的总数
    val blockCount = getSdcardStatFs().availableBlocksLong
    //获得sdcard上每个block 的大小
    val blockSize = getSdcardStatFs().blockSizeLong
    return (blockCount * blockSize) / 1024 / 1024
}

/**
 * 获得内置sd卡不可用容量，即已用大小，单位M
 */
fun Context.getSdcardUnavailableCapacity() = getSdcardTotalCapacity() - getSdcardAvailableCapacity()

/**
 * 传入指定大小的文件长度，扫描sd卡空间是否足够
 * 需有1G的默认大小的空间
 */
fun Context.scanDisk(space: Long = 1024) = getSdcardAvailableCapacity() > space

