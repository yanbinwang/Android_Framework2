package com.yanzhenjie.mediascanner;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.util.LinkedList;
import java.util.List;

/**
 * <p>MediaScanner.</p>
 * Created by YanZhenjie on 17-3-27.
 */
public class MediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {
    private int scanCount = 0;
    private String[] mCurrentScanPaths;
    private MediaScannerConnection mMediaScanConn;
    private LinkedList<String[]> mLinkedList = new LinkedList<>();

    /**
     * Create scanner.
     *
     * @param context context.
     */
    public MediaScanner(Context context) {
        this.mMediaScanConn = new MediaScannerConnection(context.getApplicationContext(), this);
    }

    /**
     * Scanner is running.
     *
     * @return true, other wise false.
     */
    public boolean isRunning() {
        return mMediaScanConn.isConnected();
    }

    /**
     * Scan file.
     *
     * @param filePath file absolute path.
     */
    public void scan(String filePath) {
        scan(new String[]{filePath});
    }

    /**
     * Scan file list.
     *
     * @param filePaths file absolute path list.
     */
    public void scan(List<String> filePaths) {
        scan(filePaths.toArray(new String[filePaths.size()]));
    }

    /**
     * Scan file array.
     *
     * @param filePaths file absolute path array.
     */
    public void scan(String[] filePaths) {
        mLinkedList.add(filePaths);
        if (!isRunning())
            executeOnce();
    }

    /**
     * Execute scanner.
     */
    private void executeOnce() {
        this.mCurrentScanPaths = mLinkedList.get(0);
        if (mCurrentScanPaths != null && mCurrentScanPaths.length > 0)
            mMediaScanConn.connect();
    }

    @Override
    public void onMediaScannerConnected() {
        if (mCurrentScanPaths != null && mCurrentScanPaths.length > 0)
            for (String filePath : mCurrentScanPaths) {
                String extension = MimeTypeMap.getFileExtensionFromUrl(filePath);
                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                mMediaScanConn.scanFile(filePath, mimeType);
            }
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        scanCount++;
        if (scanCount == mCurrentScanPaths.length) {
            mMediaScanConn.disconnect();
            scanCount = 0;
            executeOnce();
        }
    }

}