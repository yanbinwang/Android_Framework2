package com.example.thirdparty.media.service

import android.database.ContentObserver
import android.database.Cursor
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.logE
import java.io.File

/**
 *  Created by wangyanbin
 *  监听产生文件，获取对应路径
 *  1.具备读写权限
 *  2.安卓10开始已淘汰MediaStore.MediaColumns.DATA方法，没法捕获绝对路径，只有通过RELATIVE_PATH捕获相对路径
 */
class ShotObserver(private val mActivity: FragmentActivity) : ContentObserver(null), LifecycleEventObserver {
    private var filePath = ""//存储上一次捕获到的文件地址
    private var listener: (filePath: String?) -> Unit = { _ -> }
    private val TAG = "ScreenShotObserver"

    init {
        mActivity.lifecycle.addObserver(this)
    }

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        //Query [ 图片媒体集 ] 包括： DCIM/ 和 Pictures/ 目录
//        val columns = arrayOf(MediaStore.MediaColumns.DATE_ADDED, MediaStore.MediaColumns.DATA, MediaStore.Images.Media._ID, MediaStore.Images.Media.TITLE, MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.SIZE)
        val columns = arrayOf(
            MediaStore.MediaColumns.DATE_ADDED,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.MediaColumns.RELATIVE_PATH else MediaStore.MediaColumns.DATA,
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.TITLE,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.SIZE)
        var cursor: Cursor? = null
        try {
            cursor = mActivity.contentResolver?.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, MediaStore.MediaColumns.DATE_MODIFIED + " desc")
            if (cursor != null) {
                if (cursor.moveToFirst()) {
//                    val contentUri = ContentUris.withAppendedId(
//                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
//                        cursor.getLong(cursor.getColumnIndex(BaseColumns._ID))
//                    )
                    //获取监听的路径
//                    val queryPath = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))
                    val queryPath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val sdPath = mActivity.getExternalFilesDir(null)?.absolutePath.orEmpty()
                        "${sdPath.split("Android")[0]}${getQueryResult(cursor, columns[1])}${getQueryResult(cursor, columns[3])}"
//                        "/storage/emulated/0/${getQueryResult(cursor, columns[1])}${getQueryResult(cursor, columns[3])}"
                    } else {
                        getQueryResult(cursor, columns[1])
                    }
                    if (filePath != queryPath) {
                        filePath = queryPath
                        //inJustDecodeBounds=true不会把图片放入内存，只会获取宽高，判断当前路径是否为图片，是的话捕获文件路径
                        val options = BitmapFactory.Options()
                        options.inJustDecodeBounds = true
                        BitmapFactory.decodeFile(queryPath, options)
                        if (options.outWidth != -1) {
                            val file = File(queryPath)
                            " \n生成图片的路径:$queryPath\n手机截屏的路径：${file.parent}".logE(TAG)
                            listener.invoke(queryPath)
                        }
                    }
                }
            }
        } catch (_: Exception) {
        } finally {
            cursor?.close()
        }
    }

    /**
     * 返回查询结果
     */
    private fun getQueryResult(cursor: Cursor, columnName: String) = cursor.getString(cursor.getColumnIndex(columnName).orZero)

    /**
     * 生命周期回调
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> register()
            Lifecycle.Event.ON_DESTROY -> {
                unregister()
                source.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

    /**
     * 注册监听
     */
    private fun register() = mActivity.contentResolver?.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, this)

    /**
     * 注销监听
     */
    private fun unregister() = mActivity.contentResolver?.unregisterContentObserver(this)

    /**
     * exists->true表示开始录屏，此时可以显示页面倒计时，false表示录屏结束，此时可以做停止的操作
     */
    fun setOnShotListener(listener: (filePath: String?) -> Unit) {
        this.listener = listener
    }

}