package com.yanzhenjie.mediascanner

import android.content.Context
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.MediaScannerConnectionClient
import android.net.Uri
import android.webkit.MimeTypeMap
import java.util.LinkedList

/**
 * 将应用下载或生成的媒体文件（如图片、视频、音频）主动通知给系统的媒体库
 * 1) 对于 Android 13 (API 33) 及以上，需要 READ_MEDIA_IMAGES、READ_MEDIA_VIDEO、READ_MEDIA_AUDIO 等权限。
 * 2) 对于旧版本 Android，需要 READ_EXTERNAL_STORAGE 和 WRITE_EXTERNAL_STORAGE 权限
 * Created by yan
 */
class MediaScanner(context: Context) : MediaScannerConnectionClient {
    private var scanCount = 0
    private var mCurrentScanPaths = arrayOf<String>()
    private val mLinkedList = LinkedList<Array<String>>()
    private val mMediaScanConn = MediaScannerConnection(context.applicationContext, this)

    /**
     * 判断媒体扫描器当前是否正在运行（即是否处于连接状态）
     * true 表示扫描器正在运行，false 表示当前没有扫描任务。
     */
    fun isRunning(): Boolean {
        return mMediaScanConn.isConnected
    }

    /**
     * 提交文件路径进行扫描
     */
    fun scan(filePath: String?) {
        filePath ?: return
        scan(arrayOf(filePath))
    }

    fun scan(filePaths: MutableList<String>?) {
        filePaths ?: return
        scan(filePaths.toTypedArray<String>())
    }

    fun scan(filePaths: Array<String>?) {
        filePaths ?: return
        mLinkedList.add(filePaths)
        if (!isRunning()) {
            executeOnce()
        }
    }

    /**
     * 从任务队列中取出下一个任务，并开始执行（即开始扫描）
     */
    private fun executeOnce() {
        mCurrentScanPaths = mLinkedList[0]
        if (mCurrentScanPaths.isNotEmpty()) {
            mMediaScanConn.connect()
        }
    }

    /**
     * 成功与系统扫描服务建立连接后，该方法会被调用
     */
    override fun onMediaScannerConnected() {
        if (mCurrentScanPaths.isNotEmpty()) {
            for (filePath in mCurrentScanPaths) {
                val extension = MimeTypeMap.getFileExtensionFromUrl(filePath)
                val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                mMediaScanConn.scanFile(filePath, mimeType)
            }
        }
    }

    /**
     * 当 scanFile() 请求的单个文件被成功扫描并添加到媒体库后，该方法会被调用
     */
    override fun onScanCompleted(path: String?, uri: Uri?) {
        scanCount++
        if (scanCount == mCurrentScanPaths.size) {
            mMediaScanConn.disconnect()
            scanCount = 0
            mLinkedList.poll()
            executeOnce()
        }
    }

}