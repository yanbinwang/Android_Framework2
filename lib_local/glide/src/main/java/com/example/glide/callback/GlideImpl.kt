package com.example.glide.callback

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.example.framework.utils.function.drawable
import com.example.glide.R
import java.io.File

/**
 * Created by WangYanBin on 2020/5/29.
 */
interface GlideImpl {

    //---------------------------------------------图片加载开始---------------------------------------------
    fun displayZoom(view: ImageView?, string: String? = null, onStart: () -> Unit = {}, onComplete: (bitmap: Bitmap?) -> Unit = {})

    fun displayFrame(view: ImageView?, string: String? = null)//线上视频加载某一帧

    fun displayGif(view: ImageView?, string: String? = null)

    fun displayGif(view: ImageView?, resourceId: Int? = null)//gif放入drawable中

    fun displayProgress(view: ImageView?, string: String? = null, onStart: () -> Unit = {}, onProgress: (progress: Int?) -> Unit = {}, onComplete: () -> Unit = {})

    fun display(view: ImageView?, string: String? = null, errorId: Drawable? = view?.context?.drawable(R.drawable.shape_glide_bg), onStart: () -> Unit = {}, onComplete: (drawable: Drawable?) -> Unit = {})

    fun display(view: ImageView?, resourceId: Drawable? = null, errorId: Drawable? = view?.context?.drawable(R.drawable.shape_glide_bg), onStart: () -> Unit = {}, onComplete: (drawable: Drawable?) -> Unit = {})

    fun displayRound(view: ImageView?, string: String? = null, errorId: Drawable? = view?.context?.drawable(R.drawable.shape_glide_bg), radius: Int = 5, overRide: BooleanArray = booleanArrayOf(false, false, false, false))

    fun displayRound(view: ImageView?, resourceId: Drawable? = null, errorId: Drawable? = view?.context?.drawable(R.drawable.shape_glide_bg), radius: Int = 5, overRide: BooleanArray = booleanArrayOf(false, false, false, false))

    fun displayCircle(view: ImageView?, string: String? = null, errorId: Drawable? = view?.context?.drawable(R.drawable.shape_glide_oval_bg))

    fun displayCircle(view: ImageView?, resourceId: Drawable? = null, errorId: Drawable? = view?.context?.drawable(R.drawable.shape_glide_oval_bg))
    //---------------------------------------------图片加载结束---------------------------------------------

    //---------------------------------------------图片库方法开始---------------------------------------------
    fun download(context: Context, string: String? = null, onStart: () -> Unit = {}, onComplete: (file: File?) -> Unit = {})

    fun clearMemoryCache(context: Context)

    fun clearDiskCache(context: Context)

    fun cacheDir(context: Context): File?
    //---------------------------------------------图片库方法结束---------------------------------------------

}