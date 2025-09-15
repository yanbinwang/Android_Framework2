package com.yanzhenjie.album.app.album.data;

import android.content.Context;
import android.os.AsyncTask;

import com.yanzhenjie.album.AlbumFile;

import java.util.ArrayList;

/**
 * Created by YanZhenjie on 2017/10/15.
 */
public class ThumbnailBuildTask extends AsyncTask<Void, Void, ArrayList<AlbumFile>> {
    private ArrayList<AlbumFile> mAlbumFiles;
    private Callback mCallback;
    private ThumbnailBuilder mThumbnailBuilder;

    public interface Callback {
        /**
         * The task begins.
         */
        void onThumbnailStart();

        /**
         * Callback results.
         *
         * @param albumFiles result.
         */
        void onThumbnailCallback(ArrayList<AlbumFile> albumFiles);
    }

    public ThumbnailBuildTask(Context context, ArrayList<AlbumFile> albumFiles, Callback callback) {
        this.mAlbumFiles = albumFiles;
        this.mCallback = callback;
        this.mThumbnailBuilder = new ThumbnailBuilder(context);
    }

    @Override
    protected void onPreExecute() {
        mCallback.onThumbnailStart();
    }

    @Override
    protected ArrayList<AlbumFile> doInBackground(Void... params) {
        for (AlbumFile albumFile : mAlbumFiles) {
            int mediaType = albumFile.getMediaType();
            if (mediaType == AlbumFile.TYPE_IMAGE) {
                albumFile.setThumbPath(mThumbnailBuilder.createThumbnailForImage(albumFile.getPath()));
            } else if (mediaType == AlbumFile.TYPE_VIDEO) {
                albumFile.setThumbPath(mThumbnailBuilder.createThumbnailForVideo(albumFile.getPath()));
            }
        }
        return mAlbumFiles;
    }

    @Override
    protected void onPostExecute(ArrayList<AlbumFile> albumFiles) {
        mCallback.onThumbnailCallback(albumFiles);
    }

}