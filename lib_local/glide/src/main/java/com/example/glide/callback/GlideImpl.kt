package com.example.glide.callback

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import com.example.framework.utils.function.drawable
import com.example.glide.R
import java.io.File

/**
 * Created by WangYanBin on 2020/5/29.
 */
interface GlideImpl {

    //---------------------------------------------图片加载开始---------------------------------------------
    /**
     * 图片会根据设置的宽度等比例拉伸高度
     */
    fun displayZoom(view: ImageView?, string: String?, onStart: () -> Unit = {}, onComplete: (bitmap: Bitmap?) -> Unit = {})

    /**
     * 线上视频加载某一帧
     */
    fun displayFrame(view: ImageView?, string: String?, frameTimeMicros: Long = 1000000000)

    /**
     * 加载gif图片
     */
    fun displayGif(view: ImageView?, string: String?)

    /**
     * 加载本地gif图片/将gif图片放入drawable中
     */
    fun displayGif(view: ImageView?, resource: Int?)

    /**
     * 加载图片，捕获加载进度，路径不能为空
     */
    fun displayProgress(view: ImageView?, string: String, onStart: () -> Unit = {}, onProgress: (progress: Int?) -> Unit = {}, onComplete: (result: Boolean) -> Unit = {})

    /**
     * 加载图片的几种展示实现
     */
    fun display(view: ImageView?, string: String?, error: Int? = R.drawable.shape_glide_bg, onStart: () -> Unit = {}, onComplete: (drawable: Drawable?) -> Unit = {})

    fun display(view: ImageView?, resource: Int?, error: Int? = R.drawable.shape_glide_bg, onStart: () -> Unit = {}, onComplete: (drawable: Drawable?) -> Unit = {})

    fun displayDefType(view: ImageView?, string: String?, errorDrawable: Drawable? = view?.context?.drawable(R.drawable.shape_glide_bg), onStart: () -> Unit = {}, onComplete: (drawable: Drawable?) -> Unit = {})

    fun displayDefType(view: ImageView?, resourceDrawable: Drawable?, errorDrawable: Drawable? = view?.context?.drawable(R.drawable.shape_glide_bg), onStart: () -> Unit = {}, onComplete: (drawable: Drawable?) -> Unit = {})

    fun displayRound(view: ImageView?, string: String?, error: Int? = R.drawable.shape_glide_bg, radius: Int, overRide: BooleanArray = booleanArrayOf(false, false, false, false))

    fun displayRound(view: ImageView?, resource: Int?, error: Int? = R.drawable.shape_glide_bg, radius: Int, overRide: BooleanArray = booleanArrayOf(false, false, false, false))

    fun displayRoundDefType(view: ImageView?, string: String?, errorDrawable: Drawable? = view?.context?.drawable(R.drawable.shape_glide_bg), radius: Int = 5, overRide: BooleanArray = booleanArrayOf(false, false, false, false))

    fun displayRoundDefType(view: ImageView?, resourceDrawable: Drawable?, errorDrawable: Drawable? = view?.context?.drawable(R.drawable.shape_glide_bg), radius: Int = 5, overRide: BooleanArray = booleanArrayOf(false, false, false, false))

    fun displayCircle(view: ImageView?, string: String?, error: Int? = R.drawable.shape_glide_oval_bg)

    fun displayCircle(view: ImageView?, resource: Int?, error: Int? = R.drawable.shape_glide_oval_bg)

    fun displayCircleDefType(view: ImageView?, string: String?, errorDrawable: Drawable? = view?.context?.drawable(R.drawable.shape_glide_oval_bg))

    fun displayCircleDefType(view: ImageView?, resourceDrawable: Drawable?, errorDrawable: Drawable? = view?.context?.drawable(R.drawable.shape_glide_oval_bg))
    //---------------------------------------------图片加载结束---------------------------------------------

    //---------------------------------------------图片库方法开始---------------------------------------------
    fun download(context: Context, string: String? = null, onStart: () -> Unit = {}, onComplete: (file: File?) -> Unit = {})

    fun clearMemoryCache(context: Context, owner: LifecycleOwner)

    fun clearDiskCache(context: Context, owner: LifecycleOwner)

    fun cacheDir(context: Context): File?
    //---------------------------------------------图片库方法结束---------------------------------------------

}