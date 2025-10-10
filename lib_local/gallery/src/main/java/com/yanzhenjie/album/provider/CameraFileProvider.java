package com.yanzhenjie.album.provider;

import android.content.Context;

import androidx.core.content.FileProvider;

/**
 * <p>For external access to files.</p>
 * Created by Yan Zhenjie on 2017/3/31.
 */
public class CameraFileProvider extends FileProvider {

    /**
     * Get the provider of the external file path.
     *
     * @param context context.
     * @return provider.
     */
    public static String getProviderName(Context context) {
        return context.getPackageName() + ".app.file.provider";
    }

}