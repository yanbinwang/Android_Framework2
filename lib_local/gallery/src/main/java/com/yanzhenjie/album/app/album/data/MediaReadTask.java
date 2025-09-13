package com.yanzhenjie.album.app.album.data;

import android.os.AsyncTask;

import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.AlbumFolder;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Image scan task.</p>
 * Created by Yan Zhenjie on 2017/3/28.
 */
public class MediaReadTask extends AsyncTask<Void, Void, MediaReadTask.ResultWrapper> {

    public interface Callback {
        /**
         * Callback the results.
         *
         * @param albumFolders album folder list.
         */
        void onScanCallback(ArrayList<AlbumFolder> albumFolders, ArrayList<AlbumFile> checkedFiles);
    }

    static class ResultWrapper {
        private ArrayList<AlbumFolder> mAlbumFolders;
        private ArrayList<AlbumFile> mAlbumFiles;
    }

    private int mFunction;
    private List<AlbumFile> mCheckedFiles;
    private MediaReader mMediaReader;
    private Callback mCallback;

    public MediaReadTask(int function, List<AlbumFile> checkedFiles, MediaReader mediaReader, Callback callback) {
        this.mFunction = function;
        this.mCheckedFiles = checkedFiles;
        this.mMediaReader = mediaReader;
        this.mCallback = callback;
    }

    @Override
    protected ResultWrapper doInBackground(Void... params) {
        ArrayList<AlbumFolder> albumFolders;
        switch (mFunction) {
            case Album.FUNCTION_CHOICE_IMAGE: {
                albumFolders = mMediaReader.getAllImage();
                break;
            }
            case Album.FUNCTION_CHOICE_VIDEO: {
                albumFolders = mMediaReader.getAllVideo();
                break;
            }
            case Album.FUNCTION_CHOICE_ALBUM: {
                albumFolders = mMediaReader.getAllMedia();
                break;
            }
            default: {
                throw new AssertionError("This should not be the case.");
            }
        }

        ArrayList<AlbumFile> checkedFiles = new ArrayList<>();

        if (mCheckedFiles != null && !mCheckedFiles.isEmpty()) {
            List<AlbumFile> albumFiles = albumFolders.get(0).getAlbumFiles();
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
        ResultWrapper wrapper = new ResultWrapper();
        wrapper.mAlbumFolders = albumFolders;
        wrapper.mAlbumFiles = checkedFiles;
        return wrapper;
    }

    @Override
    protected void onPostExecute(ResultWrapper wrapper) {
        mCallback.onScanCallback(wrapper.mAlbumFolders, wrapper.mAlbumFiles);
    }

}