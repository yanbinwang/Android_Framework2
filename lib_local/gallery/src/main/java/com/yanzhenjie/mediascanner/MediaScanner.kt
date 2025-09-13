package com.yanzhenjie.mediascanner

import android.content.Context
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.MediaScannerConnectionClient
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.safeGet
import java.util.LinkedList

/**
 * <p>MediaScanner.</p>
 * Created by YanZhenjie on 17-3-27.
 */
class MediaScanner : MediaScannerConnectionClient {
    private var scanCount = 0
    private var mCurrentScanPaths: Array<String>? = null
    private var mMediaScanConn: MediaScannerConnection? = null
    private val mLinkedList = LinkedList<Array<String>>()

    /**
     * Create scanner.
     *
     * @param context context.
     */
    constructor(context: Context) {
        mMediaScanConn = MediaScannerConnection(context.applicationContext, this)
    }

    override fun onMediaScannerConnected() {
        if (mCurrentScanPaths != null && mCurrentScanPaths?.size.orZero > 0) {
            for (filePath in mCurrentScanPaths) {
                val extension = MimeTypeMap.getFileExtensionFromUrl(filePath)
                val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                mMediaScanConn?.scanFile(filePath, mimeType)
            }
        }
    }

    override fun onScanCompleted(path: String?, uri: Uri?) {
        scanCount++
        if (scanCount == mCurrentScanPaths?.size) {
            mMediaScanConn?.disconnect()
            scanCount = 0
            executeOnce()
        }
    }

    /**
     * Scanner is running.
     *
     * @return true, other wise false.
     */
    fun isRunning(): Boolean {
        return mMediaScanConn?.isConnected.orFalse
    }

    /**
     * Scan file.
     *
     * @param filePath file absolute path.
     */
    fun scan(filePath: String) {
        scan(arrayOf(filePath))
    }

    /**
     * Scan file list.
     *
     * @param filePaths file absolute path list.
     */
    fun scan(filePaths: MutableList<String>) {
        scan(filePaths.toTypedArray<String>())
    }

    /**
     * Scan file array.
     *
     * @param filePaths file absolute path array.
     */
    fun scan(filePaths: Array<String>) {
        mLinkedList.add(filePaths)
        if (!isRunning()) executeOnce()
    }

    /**
     * Execute scanner.
     */
    private fun executeOnce() {
        this.mCurrentScanPaths = mLinkedList.safeGet(0)
        if (mCurrentScanPaths != null && mCurrentScanPaths?.size.orZero > 0) mMediaScanConn?.connect()
    }

}