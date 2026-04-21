package com.example.album.utils

import android.content.Context
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.MediaScannerConnectionClient
import android.net.Uri
import android.webkit.MimeTypeMap
import java.util.LinkedList
import java.util.Queue

/**
 * 媒体文件扫描器
 * 作用：通知Android系统刷新相册/文件管理器
 */
class MediaScanner(context: Context) : MediaScannerConnectionClient {
    // 扫描计数
    private var scanCount = 0
    // 当前正在扫描的文件路径
    private var currentScanPaths: Array<String>? = null
    // 系统扫描连接
    private val mediaScanConnection = MediaScannerConnection(context.applicationContext, this)
    // 扫描任务队列（线程安全）
    private val taskQueue: Queue<Array<String>> = LinkedList()

    /**
     * 系统扫描连接成功
     */
    override fun onMediaScannerConnected() {
        if (currentScanPaths.isNullOrEmpty()) return
        scanCount = 0
        for (filePath in currentScanPaths) {
            val extension = MimeTypeMap.getFileExtensionFromUrl(filePath)
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            mediaScanConnection.scanFile(filePath, mimeType)
        }
    }

    /**
     * 单个文件扫描完成
     */
    override fun onScanCompleted(path: String?, uri: Uri?) {
        scanCount++
        // 当前批次全部扫描完毕
        if (scanCount >= (currentScanPaths?.size ?: 0)) {
            mediaScanConnection.disconnect()
            scanCount = 0
            // 继续执行下一个任务
            executeNextTask()
        }
    }

    /**
     * 扫描单个文件
     */
    fun scan(filePath: String?) {
        if (filePath.isNullOrEmpty()) return
        scan(arrayOf(filePath))
    }

    /**
     * 扫描文件列表
     */
    fun scan(filePaths: List<String>?) {
        if (filePaths.isNullOrEmpty()) return
        scan(filePaths.toTypedArray<String>())
    }

    /**
     * 扫描文件数组
     */
    fun scan(filePaths: Array<String>?) {
        if (filePaths.isNullOrEmpty()) return
        taskQueue.offer(filePaths)
        if (!isRunning()) {
            executeNextTask()
        }
    }

    /**
     * 执行下一个扫描任务
     */
    private fun executeNextTask() {
        // 队列为空，直接结束
        if (taskQueue.isEmpty()) {
            if (isRunning()) {
                mediaScanConnection.disconnect()
            }
            return
        }
        // 取出并移除队首任务
        currentScanPaths = taskQueue.poll()
        if (!currentScanPaths.isNullOrEmpty()) {
            mediaScanConnection.connect()
        }
    }

    /**
     * 是否正在扫描
     */
    fun isRunning(): Boolean {
        return mediaScanConnection.isConnected
    }

}