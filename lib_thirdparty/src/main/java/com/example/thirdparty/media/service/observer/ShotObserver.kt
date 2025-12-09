package com.example.thirdparty.media.service.observer

import android.annotation.SuppressLint
import android.content.ContentUris
import android.database.ContentObserver
import android.database.Cursor
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.common.utils.function.getFileFromUri
import com.example.common.utils.function.isValidImage
import com.example.framework.utils.logWTF
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

/**
 *  Created by wangyanbin
 *  监听产生文件，获取对应路径
 *  1.具备读写权限
 *  2.安卓10开始已淘汰MediaStore.MediaColumns.DATA方法，没法捕获绝对路径，只有通过RELATIVE_PATH捕获相对路径
 */
//class ShotObserver(private val mActivity: FragmentActivity) : ContentObserver(null), LifecycleEventObserver {
//    private var filePath = ""//存储上一次捕获到的文件地址
//    private var listener: (filePath: String?) -> Unit = { _ -> }
//    private val TAG = "ScreenShotObserver"
//
//    init {
//        mActivity.lifecycle.addObserver(this)
//    }
//
//    override fun onChange(selfChange: Boolean) {
//        super.onChange(selfChange)
//        // Query [ 图片媒体集 ] 包括： DCIM/ 和 Pictures/ 目录
////        val columns = arrayOf(MediaStore.MediaColumns.DATE_ADDED, MediaStore.MediaColumns.DATA, MediaStore.Images.Media._ID, MediaStore.Images.Media.TITLE, MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.SIZE)
//        val columns = arrayOf(
//            MediaStore.MediaColumns.DATE_ADDED,
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.MediaColumns.RELATIVE_PATH else MediaStore.MediaColumns.DATA,
//            MediaStore.Images.Media._ID,
//            MediaStore.Images.Media.TITLE,
//            MediaStore.Images.Media.MIME_TYPE,
//            MediaStore.Images.Media.SIZE)
//        var cursor: Cursor? = null
//        try {
//            cursor = mActivity.contentResolver?.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, MediaStore.MediaColumns.DATE_MODIFIED + " desc")
//            if (cursor != null) {
//                if (cursor.moveToFirst()) {
////                    val contentUri = ContentUris.withAppendedId(
////                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
////                        cursor.getLong(cursor.getColumnIndex(BaseColumns._ID))
////                    )
//                    // 获取监听的路径
////                    val queryPath = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))
//                    val queryPath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                        val sdPath = mActivity.getExternalFilesDir(null)?.absolutePath.orEmpty()
//                        "${sdPath.split("Android")[0]}${getQueryResult(cursor, columns[1])}${getQueryResult(cursor, columns[3])}"
////                        "/storage/emulated/0/${getQueryResult(cursor, columns[1])}${getQueryResult(cursor, columns[3])}"
//                    } else {
//                        getQueryResult(cursor, columns[1])
//                    }
//                    if (filePath != queryPath) {
//                        filePath = queryPath
//                        //判断当前路径是否为图片，是的话捕获文件路径
//                        if (queryPath.isValidImage()) {
//                            val file = File(queryPath)
//                            " \n生成图片的路径:$queryPath\n手机截屏的路径：${file.parent}".logE(TAG)
//                            listener.invoke(queryPath)
//                        }
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        } finally {
//            cursor?.close()
//        }
//    }
//
//    /**
//     * 返回查询结果
//     */
//    private fun getQueryResult(cursor: Cursor, columnName: String): String {
//        return cursor.getString(cursor.getColumnIndex(columnName).orZero)
//    }
//
//    /**
//     * 生命周期回调
//     */
//    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
//        when (event) {
//            Lifecycle.Event.ON_RESUME -> register()
////            Lifecycle.Event.ON_PAUSE -> unregister() // 应用退到后台需要抓取,故而不注销
//            Lifecycle.Event.ON_DESTROY -> {
//                unregister()
//                source.lifecycle.removeObserver(this)
//            }
//            else -> {}
//        }
//    }
//
//    /**
//     * 注册监听
//     */
//    private fun register() {
//        unregister()
//        mActivity.contentResolver?.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, this)
//    }
//
//    /**
//     * 注销监听
//     */
//    private fun unregister() {
//        mActivity.contentResolver?.unregisterContentObserver(this)
//    }
//
//    /**
//     * exists->true表示开始录屏，此时可以显示页面倒计时，false表示录屏结束，此时可以做停止的操作
//     */
//    fun setOnShotListener(listener: (filePath: String?) -> Unit) {
//        this.listener = listener
//    }
//
//}
/**
 * 监听设备截屏/图片新增的观察者
 * @param mActivity 关联的Activity（需保证生命周期一致）
 * @param debounceTime 防抖时间（默认500ms，避免重复回调）
 * EAD_EXTERNAL_STORAGE 权限（Android 13 需 READ_MEDIA_IMAGES）
 */
@SuppressLint("Range")
class ShotObserver(private val mActivity: FragmentActivity, private val debounceTime: Long = 500L) : ContentObserver(Handler(Looper.getMainLooper())), LifecycleEventObserver {
    // 存储上一次捕获到的文件地址
    private var lastFilePath = ""
    // 防抖用的延迟任务
    private var debounceJob: Job? = null
    // 回调监听
    private var listener: (filePath: String?) -> Unit = { _ -> }

    companion object {
        private const val TAG = "ShotObserver"
    }

    init {
        mActivity.lifecycle.addObserver(this)
    }

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        // 防抖：取消上次任务，延迟执行查询
        debounceJob?.cancel()
        debounceJob = mActivity.lifecycleScope.launch {
            delay(debounceTime)
            // 校验Activity是否存活，避免销毁后操作
            if (mActivity.isFinishing || mActivity.isDestroyed) return@launch
            queryLatestImage()
        }
    }

    private fun queryLatestImage() {
        val projection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media.RELATIVE_PATH, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.SIZE)
        } else {
            // MediaStore.Images.Media.DATA --> 低版本路径字段
            arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media.DATA, MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.SIZE)
        }
        // 查询最新1条图片记录（按时间倒序）
        val cursor = try {
            mActivity.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, "${MediaStore.Images.Media.DATE_ADDED} DESC LIMIT 1")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        cursor?.use {
            if (it.moveToFirst()) {
                val currentPath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10+：通过 ContentResolver 获取绝对路径（避免手动拼接）
                    val imageId = it.getLong(it.getColumnIndex(projection[0]))
                    val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId)
                    contentUri.getFileFromUri(mActivity)?.absolutePath
                } else {
                    // 低版本仍用DATA字段
                    getQueryResult(it, projection[2])
                }.orEmpty()
                // 过滤非截图文件（通过路径关键词+文件有效性校验）
                if (currentPath != lastFilePath) {
                    // 判断当前路径是否为图片，是的话捕获文件路径
                    if (currentPath.isValidImage()) {
                        val file = File(currentPath)
                        lastFilePath = currentPath
                        " \n生成图片的路径:$currentPath\n手机截屏的路径：${file.parent}".logWTF(TAG)
                        listener.invoke(currentPath)
                    }
                }
            }
        }
    }

    /**
     * 返回查询结果
     */
    private fun getQueryResult(cursor: Cursor, columnName: String): String {
        val columnIndex = cursor.getColumnIndex(columnName)
        return if (columnIndex != -1) cursor.getString(columnIndex) ?: "" else ""
    }

    /**
     * exists->true表示开始录屏，此时可以显示页面倒计时，false表示录屏结束，此时可以做停止的操作
     */
    fun setOnShotListener(listener: (filePath: String?) -> Unit) {
        this.listener = listener
    }

    /**
     * 生命周期回调
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> register()
//            Lifecycle.Event.ON_PAUSE -> unregister() // 应用退到后台需要抓取,故而不注销
            Lifecycle.Event.ON_DESTROY -> {
                debounceJob?.cancel()
                unregister()
                source.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

    /**
     * 注册监听
     */
    private fun register() {
        unregister()
        mActivity.contentResolver?.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, this)
    }

    /**
     * 注销监听
     */
    private fun unregister() {
        mActivity.contentResolver?.unregisterContentObserver(this)
    }

}