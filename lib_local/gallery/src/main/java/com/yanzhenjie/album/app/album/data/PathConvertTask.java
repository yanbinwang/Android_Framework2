package com.yanzhenjie.album.app.album.data;

import android.os.AsyncTask;

import com.yanzhenjie.album.model.AlbumFile;

/**
 * 路径转换异步任务
 * 功能：在子线程将文件路径转换为 AlbumFile
 * 防止主线程卡顿（因为视频时长解析耗时）
 */
public class PathConvertTask extends AsyncTask<String, Void, AlbumFile> {
    // 路径转换器（真正干活的）
    private final PathConversion mConversion;
    // 转换回调
    private final Callback mCallback;

    /**
     * 构造任务
     */
    public PathConvertTask(PathConversion conversion, Callback callback) {
        super();
        this.mConversion = conversion;
        this.mCallback = callback;
    }

    /**
     * 任务开始前：主线程回调
     */
    @Override
    protected void onPreExecute() {
        mCallback.onConvertStart();
    }

    /**
     * 子线程：执行路径转换
     */
    @Override
    protected AlbumFile doInBackground(String... params) {
        return mConversion.convert(params[0]);
    }

    /**
     * 转换完成：主线程回调
     */
    @Override
    protected void onPostExecute(AlbumFile file) {
        mCallback.onConvertCallback(file);
    }

    /**
     * 转换回调接口
     */
    public interface Callback {
        /**
         * 任务开始
         */
        void onConvertStart();

        /**
         * 转换完成，返回结果
         */
        void onConvertCallback(AlbumFile albumFile);
    }

}