package com.yanzhenjie.album.app.album.data;

import android.media.MediaPlayer;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.Filter;
import com.yanzhenjie.album.util.AlbumUtils;

import java.io.File;

/**
 * 路径转换器
 * 功能：将一个【图片/视频路径】转换成相册可识别的 AlbumFile 对象
 * 主要用于：拍照/录像后的路径解析、外部传入路径解析
 */
public class PathConversion {
    // 文件大小过滤器
    private final Filter<Long> mSizeFilter;
    // 文件类型过滤器
    private final Filter<String> mMimeFilter;
    // 视频时长过滤器
    private final Filter<Long> mDurationFilter;

    /**
     * 构造方法：传入过滤规则
     */
    public PathConversion(Filter<Long> sizeFilter, Filter<String> mimeFilter, Filter<Long> durationFilter) {
        this.mSizeFilter = sizeFilter;
        this.mMimeFilter = mimeFilter;
        this.mDurationFilter = durationFilter;
    }

    /**
     * 核心方法：将文件路径 转换为 AlbumFile
     * 必须在子线程执行（因为有视频时长解析）
     */
    @WorkerThread
    @NonNull
    public AlbumFile convert(String filePath) {
        File file = new File(filePath);
        // 创建实体类，设置基础信息
        AlbumFile albumFile = new AlbumFile();
        albumFile.setPath(filePath);
        // 获取文件夹名称
        File parentFile = file.getParentFile();
        albumFile.setBucketName(parentFile.getName());
        // 获取文件类型（image/jpeg、video/mp4...）
        String mimeType = AlbumUtils.getMimeType(filePath);
        albumFile.setMimeType(mimeType);
        // 设置添加时间（当前时间）
        long nowTime = System.currentTimeMillis();
        albumFile.setAddDate(nowTime);
        // 设置文件大小
        albumFile.setSize(file.length());
        // 判断是图片还是视频
        int mediaType = 0;
        if (!TextUtils.isEmpty(mimeType)) {
            // 视频
            if (mimeType.contains("video")) {
                mediaType = AlbumFile.TYPE_VIDEO;
            }
            // 图片
            if (mimeType.contains("image")) {
                mediaType = AlbumFile.TYPE_IMAGE;
            }
        }
        albumFile.setMediaType(mediaType);
        // 应用过滤规则（不符合则置为不可选）
        if (mSizeFilter != null && mSizeFilter.filter(file.length())) {
            albumFile.setDisable(true);
        }
        if (mMimeFilter != null && mMimeFilter.filter(mimeType)) {
            albumFile.setDisable(true);
        }
        // 如果是视频，获取时长
        if (mediaType == AlbumFile.TYPE_VIDEO) {
            MediaPlayer player = new MediaPlayer();
            try {
                player.setDataSource(filePath);
                player.prepare();
                // 获取视频时长
                albumFile.setDuration(player.getDuration());
            } catch (Exception ignored) {
                // 异常忽略
            } finally {
                // 释放播放器，防止内存泄漏
                player.release();
            }
            // 视频时长过滤
            if (mDurationFilter != null && mDurationFilter.filter(albumFile.getDuration())) {
                albumFile.setDisable(true);
            }
        }
        // 返回封装好的实体
        return albumFile;
    }

}