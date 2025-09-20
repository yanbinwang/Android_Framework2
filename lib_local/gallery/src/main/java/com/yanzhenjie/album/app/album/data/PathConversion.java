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
 * Created by YanZhenjie on 2017/10/18.
 */
public class PathConversion {
    private Filter<Long> mSizeFilter;
    private Filter<String> mMimeFilter;
    private Filter<Long> mDurationFilter;

    public PathConversion(Filter<Long> sizeFilter, Filter<String> mimeFilter, Filter<Long> durationFilter) {
        this.mSizeFilter = sizeFilter;
        this.mMimeFilter = mimeFilter;
        this.mDurationFilter = durationFilter;
    }

    @WorkerThread
    @NonNull
    public AlbumFile convert(String filePath) {
        File file = new File(filePath);
        AlbumFile albumFile = new AlbumFile();
        albumFile.setMPath(filePath);
        File parentFile = file.getParentFile();
        albumFile.setMBucketName(parentFile.getName());
        String mimeType = AlbumUtils.getMimeType(filePath);
        albumFile.setMMimeType(mimeType);
        long nowTime = System.currentTimeMillis();
        albumFile.setMAddDate(nowTime);
        albumFile.setMSize(file.length());
        int mediaType = 0;
        if (!TextUtils.isEmpty(mimeType)) {
            if (mimeType.contains("video"))
                mediaType = AlbumFile.TYPE_VIDEO;
            if (mimeType.contains("image"))
                mediaType = AlbumFile.TYPE_IMAGE;
        }
        albumFile.setMMediaType(mediaType);
        if (mSizeFilter != null && mSizeFilter.filter(file.length())) {
            albumFile.setDisable(true);
        }
        if (mMimeFilter != null && mMimeFilter.filter(mimeType)) {
            albumFile.setDisable(true);
        }
        if (mediaType == AlbumFile.TYPE_VIDEO) {
            MediaPlayer player = new MediaPlayer();
            try {
                player.setDataSource(filePath);
                player.prepare();
                albumFile.setMDuration((long) player.getDuration());
            } catch (Exception ignored) {
            } finally {
                player.release();
            }

            if (mDurationFilter != null && mDurationFilter.filter(albumFile.getMDuration())) {
                albumFile.setDisable(true);
            }
        }
        return albumFile;
    }

}