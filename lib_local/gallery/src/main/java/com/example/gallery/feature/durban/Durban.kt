package com.example.gallery.feature.durban

import android.app.Activity
import android.content.Intent
import androidx.annotation.ColorRes
import androidx.annotation.IntDef
import androidx.annotation.IntRange
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.gallery.feature.durban.app.photobox.PhotoBoxActivity
import com.example.gallery.feature.durban.bean.Controller

/**
 * 图片裁剪入口类（链式调用裁剪配置）
 * 作用：外部调用裁剪功能，统一配置、跳转、接收结果 -> Builder 链式调用
 */
class Durban(private val host: Any) {
    // 跳转裁剪页的 Intent
    private val mCropIntent = Intent(applyActivity(host), PhotoBoxActivity::class.java)

    companion object {
        // 跳转 KEY 常量
        const val KEY_PREFIX = "AlbumCrop"
        const val KEY_INPUT_STATUS_COLOR = "$KEY_PREFIX.KEY_INPUT_STATUS_COLOR"
        const val KEY_INPUT_NAVIGATION_COLOR = "$KEY_PREFIX.KEY_INPUT_NAVIGATION_COLOR"
        const val KEY_INPUT_TITLE = "$KEY_PREFIX.KEY_INPUT_TITLE"
        const val KEY_INPUT_GESTURE = "$KEY_PREFIX.KEY_INPUT_GESTURE"
        const val KEY_INPUT_ASPECT_RATIO = "$KEY_PREFIX.KEY_INPUT_ASPECT_RATIO"
        const val KEY_INPUT_MAX_WIDTH_HEIGHT = "$KEY_PREFIX.KEY_INPUT_MAX_WIDTH_HEIGHT"
        const val KEY_INPUT_COMPRESS_FORMAT = "$KEY_PREFIX.KEY_INPUT_COMPRESS_FORMAT"
        const val KEY_INPUT_COMPRESS_QUALITY = "$KEY_PREFIX.KEY_INPUT_COMPRESS_QUALITY"
        const val KEY_INPUT_DIRECTORY = "$KEY_PREFIX.KEY_INPUT_DIRECTORY"
        const val KEY_INPUT_PATH_ARRAY = "$KEY_PREFIX.KEY_INPUT_PATH_ARRAY"
        const val KEY_INPUT_CONTROLLER = "$KEY_PREFIX.KEY_INPUT_CONTROLLER"
        const val KEY_OUTPUT_IMAGE_LIST = "$KEY_PREFIX.KEY_OUTPUT_IMAGE_LIST"
        const val KEY_ORIGINAL_PATH_LIST = "$KEY_PREFIX.KEY_ORIGINAL_IMAGE_LIST"

        // 不允许任何手势
        const val GESTURE_NONE = 0
        // 允许缩放
        const val GESTURE_SCALE = 1
        // 允许旋转
        const val GESTURE_ROTATE = 2
        // 允许旋转和缩放
        const val GESTURE_ALL = 3
        // 编译时注解：限制手势类型输入
        @IntDef(GESTURE_NONE, GESTURE_SCALE, GESTURE_ROTATE, GESTURE_ALL)
        @Retention(AnnotationRetention.SOURCE)
        annotation class GestureTypes

        // JPEG/PNG 格式
        const val COMPRESS_JPEG = 0
        const val COMPRESS_PNG = 1
        // 编译时注解：限制格式输入
        @IntDef(COMPRESS_JPEG, COMPRESS_PNG)
        @Retention(AnnotationRetention.SOURCE)
        annotation class FormatTypes

        /**
         * 构造方法
         */
        @JvmStatic
        fun with(host: Any): Durban {
            return Durban(host)
        }

        /**
         * 发起裁剪的页面需要得到裁剪后的图片路径集合的时候在onActivityResult中使用
         * override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         * super.onActivityResult(requestCode, resultCode, data)
         * if (requestCode == RESULT_ALBUM) {
         * data ?: return
         * val mImageList = Durban.parseResult(data)
         * mImageList.safeGet(0).shortToast()
         * }
         * }
         */
        @JvmStatic
        fun parseResult(intent: Intent?): ArrayList<String> {
            return intent?.getStringArrayListExtra(KEY_OUTPUT_IMAGE_LIST) ?: arrayListOf()
        }

        /**
         * 拿原始图片集合
         */
        @JvmStatic
        fun parseOriginal(intent: Intent?): ArrayList<String> {
            return intent?.getStringArrayListExtra(KEY_ORIGINAL_PATH_LIST) ?: arrayListOf()
        }
    }

    /**
     * 设置状态栏背景颜色/通常随标题栏背景
     */
    fun statusBarColor(@ColorRes color: Int): Durban {
        mCropIntent.putExtra(KEY_INPUT_STATUS_COLOR, color)
        return this
    }

    /**
     * 设置导航栏背景背景
     */
    fun navigationBarColor(@ColorRes color: Int): Durban {
        mCropIntent.putExtra(KEY_INPUT_NAVIGATION_COLOR, color)
        return this
    }

    /**
     * 设置标题
     */
    fun title(title: String): Durban {
        mCropIntent.putExtra(KEY_INPUT_TITLE, title)
        return this
    }

    /**
     * 裁剪时的手势支持
     */
    fun gesture(@GestureTypes gesture: Int): Durban {
        mCropIntent.putExtra(KEY_INPUT_GESTURE, gesture)
        return this
    }

    /**
     * 裁剪时的宽高比
     */
    fun aspectRatio(x: Float, y: Float): Durban {
        mCropIntent.putExtra(KEY_INPUT_ASPECT_RATIO, floatArrayOf(x, y))
        return this
    }

    /**
     * 裁剪时使用原始图像的纵横比列
     */
    fun aspectRatioWithSourceImage(): Durban {
        return aspectRatio(0f, 0f)
    }

    /**
     * 裁剪图片输出的最大宽高
     */
    fun maxWidthHeight(@IntRange(from = 100) width: Int, @IntRange(from = 100) height: Int): Durban {
        mCropIntent.putExtra(KEY_INPUT_MAX_WIDTH_HEIGHT, intArrayOf(width, height))
        return this
    }

    /**
     * 图片压缩格式：JPEG、PNG
     */
    fun compressFormat(@FormatTypes format: Int): Durban {
        mCropIntent.putExtra(KEY_INPUT_COMPRESS_FORMAT, format)
        return this
    }

    /**
     * 图片压缩质量，请参考：Bitmap#compress(Bitmap.CompressFormat, int, OutputStream)
     */
    fun compressQuality(quality: Int): Durban {
        mCropIntent.putExtra(KEY_INPUT_COMPRESS_QUALITY, quality)
        return this
    }

    /**
     * 图片输出文件夹路径
     */
    fun outputDirectory(folder: String): Durban {
        mCropIntent.putExtra(KEY_INPUT_DIRECTORY, folder)
        return this
    }

    /**
     * 图片路径
     */
    fun inputImagePaths(vararg imagePathArray: String): Durban {
        mCropIntent.putStringArrayListExtra(KEY_INPUT_PATH_ARRAY, arrayListOf(*imagePathArray))
        return this
    }

    /**
     * 图片路径list
     */
    fun inputImagePaths(imagePathList: ArrayList<String>): Durban {
        mCropIntent.putStringArrayListExtra(KEY_INPUT_PATH_ARRAY, imagePathList)
        return this
    }

    /**
     * 底部操作盘配置
     */
    fun controller(controller: Controller): Durban {
        mCropIntent.putExtra(KEY_INPUT_CONTROLLER, controller)
        return this
    }

    /**
     * 页面的Request code回调编码, callback to `onActivityResult()`.
     */
    fun requestCode(requestCode: Int): Durban {
        mCropIntent.putExtra("requestCode", requestCode)
        return this
    }

    /**
     * 通过反射跳转执行startActivityForResult,
     */
    fun start() {
//        try {
//            val method = any.javaClass.getMethod("startActivityForResult", Intent::class.java, Int::class.javaPrimitiveType)
//            if (!method.isAccessible) method.isAccessible = true
//            method.invoke(any, mCropIntent, mCropIntent.getIntExtra("requestCode", 1))
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
        val activity = applyActivity(host)
        val requestCode = mCropIntent.getIntExtra("requestCode", 1)
        activity.startActivityForResult(mCropIntent, requestCode)
    }

    /**
     * 内部拿取上下文方法
     */
    private fun applyActivity(host: Any): Activity {
        return when (host) {
            // Activity（兼容所有现代 Activity）
            is FragmentActivity -> host
            // AndroidX Fragment
            is Fragment -> host.requireActivity()
            // 旧系统Fragment
            is android.app.Fragment -> throw RuntimeException("android.app.Fragment is deprecated and not supported!")
            // 不认识的类型
            else -> throw IllegalArgumentException("Unsupported host type: ${host::class.java.name}")
        }
    }

}