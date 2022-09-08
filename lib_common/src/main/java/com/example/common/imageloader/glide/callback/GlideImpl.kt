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
    fun displayZoomImage(view: ImageView, string: String?, listener: GlideRequestListener<Bitmap?>? = null)

    fun displayCoverImage(view: ImageView, string: String?)

    fun displayProgressImage(view: ImageView, string: String, onStart: () -> Unit? = {}, onProgress: (progress: Int?) -> Unit = {}, onComplete: () -> Unit? = {})

    fun displayImage(view: ImageView, string: String?, placeholderId: Int = R.drawable.shape_image_loading, errorId: Int = 0, listener: GlideRequestListener<Drawable?>? = null)

    fun displayRoundImage(view: ImageView, string: String?, errorId: Int = 0, roundingRadius: Int = 5, overRide: BooleanArray = booleanArrayOf(false, false, false, false))

    fun displayCircleImage(view: ImageView, string: String?, errorId: Int = R.drawable.shape_image_loading_round)
    //---------------------------------------------圆形图片加载开始---------------------------------------------

    //---------------------------------------------图片库方法开始---------------------------------------------
    fun downloadImage(context: Context, string: String?, listener: GlideRequestListener<File?>?)

    fun clearMemoryCache(context: Context)

    fun clearDiskCache(context: Context)

    val cacheDir: File?
    //---------------------------------------------图片库方法结束---------------------------------------------

}