package com.yanzhenjie.album.app.album.data;

import android.os.AsyncTask;

import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.model.AlbumFile;
import com.yanzhenjie.album.model.AlbumFolder;

import java.util.ArrayList;
import java.util.List;

/**
 * 媒体扫描异步任务
 * 作用：在子线程扫描手机图片/视频，扫描完毕后回调结果
 * 防止主线程卡顿，是相册加载数据的核心任务
 */
public class MediaReadTask extends AsyncTask<Void, Void, MediaReadTask.ResultWrapper> {
    // 扫描模式：图片 / 视频 / 全部
    private final int mFunction;
    // 已经选中的文件（用于回显勾选状态）
    private final List<AlbumFile> mCheckedFiles;
    // 媒体扫描器
    private final MediaReader mMediaReader;
    // 扫描完成回调
    private final Callback mCallback;

    /**
     * 构造任务
     */
    public MediaReadTask(int function, List<AlbumFile> checkedFiles, MediaReader mediaReader, Callback callback) {
        super();
        this.mFunction = function;
        this.mCheckedFiles = checkedFiles;
        this.mMediaReader = mediaReader;
        this.mCallback = callback;
    }

    /**
     * 子线程：真正执行扫描
     */
    @Override
    protected ResultWrapper doInBackground(Void... params) {
        ArrayList<AlbumFolder> albumFolders;
        // 根据模式调用扫描
        switch (mFunction) {
            // 只扫描图片
            case Album.FUNCTION_CHOICE_IMAGE: {
                albumFolders = mMediaReader.getAllImage();
                break;
            }
            // 只扫描视频
            case Album.FUNCTION_CHOICE_VIDEO: {
                albumFolders = mMediaReader.getAllVideo();
                break;
            }
            // 图片 + 视频都扫描
            case Album.FUNCTION_CHOICE_ALBUM: {
                albumFolders = mMediaReader.getAllMedia();
                break;
            }
            default: {
                throw new AssertionError("This should not be the case.");
            }
        }
        // 处理已选中的文件，恢复勾选状态
        ArrayList<AlbumFile> checkedFiles = new ArrayList<>();
        if (mCheckedFiles != null && !mCheckedFiles.isEmpty()) {
            // 拿到“全部图片/视频”文件夹里的文件
            List<AlbumFile> albumFiles = albumFolders.get(0).getAlbumFiles();
            // 遍历对比，把之前选中的文件重新勾选
            for (AlbumFile checkAlbumFile : mCheckedFiles) {
                for (int i = 0; i < albumFiles.size(); i++) {
                    AlbumFile albumFile = albumFiles.get(i);
                    if (checkAlbumFile.equals(albumFile)) {
                        albumFile.setChecked(true);
                        checkedFiles.add(albumFile);
                    }
                }
            }
        }
        // 包装结果返回
        ResultWrapper wrapper = new ResultWrapper();
        wrapper.mAlbumFolders = albumFolders;
        wrapper.mAlbumFiles = checkedFiles;
        return wrapper;
    }

    /**
     * 主线程：扫描完成，回调结果
     */
    @Override
    protected void onPostExecute(ResultWrapper wrapper) {
        mCallback.onScanCallback(wrapper.mAlbumFolders, wrapper.mAlbumFiles);
    }

    /**
     * 扫描结果回调接口
     */
    public interface Callback {
        void onScanCallback(ArrayList<AlbumFolder> albumFolders, ArrayList<AlbumFile> checkedFiles);
    }

    /**
     * 结果包装类，包装两个返回值
     */
    public static class ResultWrapper {
        private ArrayList<AlbumFolder> mAlbumFolders; // 所有文件夹
        private ArrayList<AlbumFile> mAlbumFiles;     // 已选中的文件
    }

}