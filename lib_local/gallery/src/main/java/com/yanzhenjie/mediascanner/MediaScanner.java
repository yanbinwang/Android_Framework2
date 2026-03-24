package com.yanzhenjie.mediascanner;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 媒体文件扫描器
 * 作用：通知Android系统刷新相册/文件管理器
 */
public class MediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {
    // 扫描计数
    private int scanCount = 0;
    // 当前正在扫描的文件路径
    private String[] currentScanPaths;
    // 系统扫描连接
    private final MediaScannerConnection mediaScanConnection;
    // 扫描任务队列（线程安全）
    private final Queue<String[]> taskQueue = new LinkedList<>();

    /**
     * 构造方法
     */
    public MediaScanner(Context context) {
        mediaScanConnection = new MediaScannerConnection(context.getApplicationContext(), this);
    }

    /**
     * 系统扫描连接成功
     */
    @Override
    public void onMediaScannerConnected() {
        if (currentScanPaths == null || currentScanPaths.length == 0) return;
        scanCount = 0;
        for (String filePath : currentScanPaths) {
            String extension = MimeTypeMap.getFileExtensionFromUrl(filePath);
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            mediaScanConnection.scanFile(filePath, mimeType);
        }
    }

    /**
     * 单个文件扫描完成
     */
    @Override
    public void onScanCompleted(String path, Uri uri) {
        scanCount++;
        // 当前批次全部扫描完毕
        if (scanCount >= currentScanPaths.length) {
            mediaScanConnection.disconnect();
            scanCount = 0;
            // 继续执行下一个任务
            executeNextTask();
        }
    }

    /**
     * 是否正在扫描
     */
    public boolean isRunning() {
        return mediaScanConnection.isConnected();
    }

    /**
     * 扫描单个文件
     */
    public void scan(String filePath) {
        if (filePath == null || filePath.isEmpty()) return;
        scan(new String[]{filePath});
    }

    /**
     * 扫描文件列表
     */
    public void scan(List<String> filePaths) {
        if (filePaths == null || filePaths.isEmpty()) return;
        scan(filePaths.toArray(new String[0]));
    }

    /**
     * 扫描文件数组
     */
    public void scan(String[] filePaths) {
        if (filePaths == null || filePaths.length == 0) return;
        taskQueue.offer(filePaths);
        if (!isRunning()) {
            executeNextTask();
        }
    }

    /**
     * 执行下一个扫描任务
     */
    private void executeNextTask() {
        // 队列为空，直接结束
        if (taskQueue.isEmpty()) {
            if (isRunning()) {
                mediaScanConnection.disconnect();
            }
            return;
        }
        // 取出并移除队首任务
        currentScanPaths = taskQueue.poll();
        if (currentScanPaths != null && currentScanPaths.length > 0) {
            mediaScanConnection.connect();
        }
    }

}