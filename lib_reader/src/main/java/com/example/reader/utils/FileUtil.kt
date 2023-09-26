package com.example.reader.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.FileUtils
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeDouble
import com.example.reader.config.Constant
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import kotlin.math.pow
import kotlin.math.roundToInt

object FileUtil {

    /**
     * 将 Uri 转换为 file path
     */
    @JvmStatic
    fun uri2FilePath(activity: Activity, uri: Uri): String? {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val actualImageCursor = activity.managedQuery(uri, proj, null, null, null)
        return if (actualImageCursor == null) {
            uri.path
        } else {
            actualImageCursor.moveToFirst()
            actualImageCursor.getString(actualImageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
        }
    }

    @JvmStatic
    @RequiresApi(Build.VERSION_CODES.Q)
    fun uri2FileQ(context: Context, uri: Uri): File? {
        var file: File? = null
        try {
            //android10以上转换
            if (uri.scheme == ContentResolver.SCHEME_FILE) {
                file = File(uri.path.orEmpty())
            } else if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
                //把文件复制到沙盒目录
                val contentResolver = context.contentResolver
                val cursor = contentResolver.query(uri, null, null, null, null)
                if (cursor?.moveToFirst().orFalse) {
                    val displayName = cursor?.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME).orZero)
                    try {
                        val inputStream = contentResolver.openInputStream(uri)
                        inputStream ?: return null
                        val cache = File(
                            context.externalCacheDir?.absolutePath,
                            ((Math.random() + 1) * 1000).roundToInt().toString() + displayName
                        )
                        val fos = FileOutputStream(cache)
                        FileUtils.copy(inputStream, fos)
                        file = cache
                        fos.close()
                        inputStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                cursor?.close()
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        return file
    }

    /**
     * 获取文件长度，以 M 为单位
     */
    @JvmStatic
    fun getFileSize(file: File): Double {
        val len = file.length()
        return len.toSafeDouble() / 2.0.pow(20.0)
    }

    /**
     * 获取文件长度，以 M 为单位
     */
    @JvmStatic
    fun getFileSize(len: Long): Double {
        return len.toSafeDouble() / 2.0.pow(20.0)
    }

    /**
     * 通过图片的 filePath 加载本地图片
     */
    @JvmStatic
    fun loadLocalPicture(filePath: String?): Bitmap? {
        var fis: FileInputStream? = null
        return try {
            val file = File(filePath.orEmpty())
            fis = FileInputStream(file)
            BitmapFactory.decodeStream(fis)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        } finally {
            try {
                fis?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 将字符串写入到 /storage/emulated/0/1/data.txt，测试用
     */
    @JvmStatic
    fun writeTxtToLocal(content: String) {
        //生成文件夹之后，再生成文件，不然会出错
        val filePath = "/storage/emulated/0/1/"
        val fileName = "data.txt"
        makeFilePath(filePath, fileName)
        val strFilePath = filePath + fileName
        // 每次写入时，都换行写
        val strContent = content + "\r\n"
        try {
            val file = File(strFilePath)
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:$strFilePath")
                file.parentFile?.mkdirs()
                file.createNewFile()
            }
            val raf = RandomAccessFile(file, "rwd")
            raf.seek(file.length())
            raf.write(strContent.toByteArray())
            raf.close()
        } catch (e: Exception) {
            Log.e("TestFile", "Error on write File:$e")
        }
    }

    //生成文件
    @JvmStatic
    private fun makeFilePath(filePath: String, fileName: String): File? {
        var file: File? = null
        makeRootDirectory(filePath)
        try {
            file = File(filePath + fileName)
            if (!file.exists()) {
                file.createNewFile()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return file
    }

    //生成文件夹
    @JvmStatic
    private fun makeRootDirectory(filePath: String) {
        val file: File?
        try {
            file = File(filePath)
            if (!file.exists()) {
                file.mkdir()
            }
        } catch (e: Exception) {
            Log.i("error:", e.toString() + "")
        }
    }

    /**
     * 获取本地缓存文件的大小
     */
    @JvmStatic
    fun getLocalCacheSize(): String {
        val file = File(Constant.EPUB_SAVE_PATH)
        val len = getFileSize(getTotalSizeOfFiles(file))
        return len.toInt().toString() + "M"
    }

    // 递归方式 计算文件的大小
    @JvmStatic
    private fun getTotalSizeOfFiles(file: File): Long {
        if (file.isFile) return file.length()
        val children = file.listFiles()
        var total = 0L
        if (children != null) for (child in children) total += getTotalSizeOfFiles(child)
        return total
    }

    /**
     * 清除本地缓存
     */
    @JvmStatic
    fun clearLocalCache() {
        val file = File(Constant.EPUB_SAVE_PATH)
        deleteFile(file)
    }

    /**
     * 删除文件夹或文件
     */
    @JvmStatic
    fun deleteFile(file: File?) {
        //判断文件不为null或文件目录存在
        if (file == null || !file.exists()) {
            return
        }
        // 删除文件
        if (file.isFile) {
            file.delete()
            return
        }
        //取得这个目录下的所有子文件对象
        val files = file.listFiles().orEmpty()
        //遍历该目录下的文件对象
        for (f in files) {
            deleteFile(f)
        }
        //删除空文件夹  for循环已经把上一层节点的目录清空。
        file.delete()
    }

}