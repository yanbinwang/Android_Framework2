package com.yanzhenjie.album.app.album.data;

import android.os.AsyncTask;

import com.yanzhenjie.album.AlbumFile;

/**
 * Created by YanZhenjie on 2017/10/18.
 */
public class PathConvertTask extends AsyncTask<String, Void, AlbumFile> {

    public interface Callback {
        /**
         * The task begins.
         */
        void onConvertStart();

        /**
         * Callback results.
         *
         * @param albumFile result.
         */
        void onConvertCallback(AlbumFile albumFile);
    }

    private PathConversion mConversion;
    private Callback mCallback;

    public PathConvertTask(PathConversion conversion, Callback callback) {
        this.mConversion = conversion;
        this.mCallback = callback;
    }

    @Override
    protected void onPreExecute() {
        mCallback.onConvertStart();
    }

    @Override
    protected AlbumFile doInBackground(String... params) {
        return mConversion.convert(params[0]);
    }

    @Override
    protected void onPostExecute(AlbumFile file) {
        mCallback.onConvertCallback(file);
    }

}