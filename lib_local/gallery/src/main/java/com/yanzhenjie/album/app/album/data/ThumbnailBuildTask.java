package com.yanzhenjie.album.app.album.data;

import android.content.Context;
import android.os.AsyncTask;

import com.yanzhenjie.album.model.AlbumFile;

import java.util.ArrayList;

/**
 * 缩略图生成 异步任务
 * 功能：批量给图片/视频生成缩略图，在子线程执行
 * 防止列表加载时卡顿、OOM
 */
public class ThumbnailBuildTask extends AsyncTask<Void, Void, ArrayList<AlbumFile>> {
    // 需要生成缩略图的文件列表
    private final ArrayList<AlbumFile> mAlbumFiles;
    // 回调
    private final Callback mCallback;
    // 缩略图构建器
    private final ThumbnailBuilder mThumbnailBuilder;

    /**
     * 构造任务
     */
    public ThumbnailBuildTask(Context context, ArrayList<AlbumFile> albumFiles, Callback callback) {
        super();
        this.mAlbumFiles = albumFiles;
        this.mCallback = callback;
        this.mThumbnailBuilder = new ThumbnailBuilder(context);
    }

    /**
     * 任务开始：主线程
     */
    @Override
    protected void onPreExecute() {
        mCallback.onThumbnailStart();
    }

    /**
     * 子线程：批量生成缩略图
     */
    @Override
    protected ArrayList<AlbumFile> doInBackground(Void... params) {
        // 遍历所有文件，生成缩略图并设置路径
        for (AlbumFile albumFile : mAlbumFiles) {
            int mediaType = albumFile.getMediaType();
            // 图片 → 生成图片缩略图
            if (mediaType == AlbumFile.TYPE_IMAGE) {
                albumFile.setThumbPath(mThumbnailBuilder.createThumbnailForImage(albumFile.getPath()));
                // 视频 → 生成视频缩略图
            } else if (mediaType == AlbumFile.TYPE_VIDEO) {
                albumFile.setThumbPath(mThumbnailBuilder.createThumbnailForVideo(albumFile.getPath()));
            }
        }
        // 返回处理完的数据（带有缩略图）
        return mAlbumFiles;
    }

    /**
     * 任务完成：主线程回调
     */
    @Override
    protected void onPostExecute(ArrayList<AlbumFile> albumFiles) {
        mCallback.onThumbnailCallback(albumFiles);
    }

    /**
     * 回调接口
     */
    public interface Callback {
        /**
         * 开始生成
         */
        void onThumbnailStart();

        /**
         * 生成完成，返回带缩略图路径的数据
         */
        void onThumbnailCallback(ArrayList<AlbumFile> albumFiles);
    }

}