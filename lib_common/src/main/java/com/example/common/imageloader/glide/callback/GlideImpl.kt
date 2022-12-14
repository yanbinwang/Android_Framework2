package com.example.common.imageloader.glide.callback

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.example.common.R
import java.io.File

/**
 * Created by WangYanBin on 2020/5/29.
 */
interface GlideImpl {

    //---------------------------------------------图片加载开始---------------------------------------------
    fun displayZoom(view: ImageView, string: String?, onStart: () -> Unit? = {}, onComplete: (bitmap: Bitmap?) -> Unit? = {})

    fun displayCover(view: ImageView, string: String?)

    fun displayProgress(view: ImageView, string: String, onStart: () -> Unit? = {}, onProgress: (progress: Int?) -> Unit = {}, onComplete: () -> Unit? = {})

    fun display(view: ImageView, string: String?, placeholderId: Int = R.drawable.shape_album_loading, errorId: Int = 0, onStart: () -> Unit? = {}, onComplete: (drawable: Drawable?) -> Unit? = {})

    fun displayRound(view: ImageView, string: String?, errorId: Int = 0, roundingRadius: Int = 5, overRide: BooleanArray = booleanArrayOf(false, false, false, false))

    fun displayCircle(view: ImageView, string: String?, errorId: Int = R.drawable.shape_glide_loading_oval)
    //---------------------------------------------圆形图片加载开始---------------------------------------------

    //---------------------------------------------图片库方法开始---------------------------------------------
    fun download(context: Context, string: String?, onStart: () -> Unit? = {}, onComplete: (file: File?) -> Unit? = {})

    fun clearMemoryCache(context: Context)

    fun clearDiskCache(context: Context)

    fun cacheDir(context: Context): File?
    //---------------------------------------------图片库方法结束---------------------------------------------

}