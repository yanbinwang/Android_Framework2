package com.yanzhenjie.album.provider

import android.content.Context
import androidx.core.content.FileProvider

/**
 * <p>For external access to files.</p>
 * Created by Yan Zhenjie on 2017/3/31.
 */
class CameraFileProvider : FileProvider() {

    companion object {
        /**
         * Get the provider of the external file path.
         *
         * @param context context.
         * @return provider.
         */
        @JvmStatic
        fun getProviderName(context: Context): String {
            return context.packageName + ".app.file.provider"
        }
    }

}