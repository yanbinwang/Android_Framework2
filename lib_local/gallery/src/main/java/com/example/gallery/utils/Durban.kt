package com.example.gallery.utils

import android.content.Context
import android.content.Intent
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import androidx.annotation.IntRange
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.framework.utils.function.value.orFalse
import com.example.gallery.activity.DurbanActivity
import com.yanzhenjie.durban.Controller
import com.yanzhenjie.durban.DurbanConfig
import java.util.Collections
import java.util.Locale

class Durban(private val o: Any) {
    private var mCropIntent: Intent? = null

    init {
        mCropIntent = Intent(getContext(o), DurbanActivity::class.java)
    }

    companion object {
        private var sDurbanConfig: DurbanConfig? = null
        private const val KEY_PREFIX = "AlbumCrop"
        const val KEY_INPUT_STATUS_COLOR = "$KEY_PREFIX.KEY_INPUT_STATUS_COLOR"
        const val KEY_INPUT_TOOLBAR_COLOR = "$KEY_PREFIX.KEY_INPUT_TOOLBAR_COLOR"
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

        /**
         * Do not allow any gestures.
         */
        const val GESTURE_NONE = 0

        /**
         * Allow scaling.
         */
        const val GESTURE_SCALE = 1

        /**
         * Allow rotation.
         */
        const val GESTURE_ROTATE = 2

        /**
         * Allow rotation and scaling.
         */
        const val GESTURE_ALL = 3

        /**
         * JPEG format.
         */
        const val COMPRESS_JPEG = 0

        /**
         * PNG format.
         */
        const val COMPRESS_PNG = 1

        @IntDef(GESTURE_NONE, GESTURE_SCALE, GESTURE_ROTATE, GESTURE_ALL)
        @Retention(AnnotationRetention.SOURCE)
        annotation class GestureTypes

        @IntDef(COMPRESS_JPEG, COMPRESS_PNG)
        @Retention(AnnotationRetention.SOURCE)
        annotation class FormatTypes

        @JvmStatic
        fun with(activity: AppCompatActivity): Durban {
            return Durban(activity)
        }

        @JvmStatic
        fun with(fragment: Fragment): Durban {
            return Durban(fragment)
        }

        @JvmStatic
        fun with(activity: FragmentActivity): Durban {
            return Durban(activity)
        }

        /**
         * Initialize Album.
         *
         * @param durbanConfig [DurbanConfig].
         */
        @JvmStatic
        fun initialize(durbanConfig: DurbanConfig?) {
            sDurbanConfig = durbanConfig
        }

        @JvmStatic
        fun getContext(o: Any): Context {
            return when (o) {
                is AppCompatActivity -> o
                is Fragment -> o.requireContext()
                is FragmentActivity -> o
                else -> throw IllegalArgumentException("${o.javaClass} is not supported.")
            }
        }

        /**
         * Get the durban configuration.
         *
         * @return [DurbanConfig].
         */
        @JvmStatic
        fun getDurbanConfig(context: Context): DurbanConfig? {
            if (sDurbanConfig == null) {
                initialize(DurbanConfig.newBuilder(context)
                        .setLocale(Locale.getDefault())
                        .build())
            }
            return sDurbanConfig
        }

        /**
         * Analyze the crop results.
         */
        @JvmStatic
        fun parseResult(intent: Intent): ArrayList<String>? {
            return intent.getStringArrayListExtra(KEY_OUTPUT_IMAGE_LIST)
        }

    }

    /**
     * The color of the StatusBar.
     */
    fun statusBarColor(@ColorInt color: Int): Durban {
        mCropIntent?.putExtra(KEY_INPUT_STATUS_COLOR, color)
        return this
    }

    /**
     * The color of the Toolbar.
     */
    fun toolBarColor(@ColorInt color: Int): Durban {
        mCropIntent?.putExtra(KEY_INPUT_TOOLBAR_COLOR, color)
        return this
    }

    /**
     * Set the color of the NavigationBar.
     */
    fun navigationBarColor(@ColorInt color: Int): Durban {
        mCropIntent?.putExtra(KEY_INPUT_NAVIGATION_COLOR, color)
        return this
    }

    /**
     * The title of the interface.
     */
    fun title(title: String): Durban {
        mCropIntent?.putExtra(KEY_INPUT_TITLE, title)
        return this
    }

    /**
     * The gestures that allow operation.
     *
     * @param gesture gesture sign.
     * @see .GESTURE_NONE
     *
     * @see .GESTURE_ALL
     *
     * @see .GESTURE_ROTATE
     *
     * @see .GESTURE_SCALE
     */
    fun gesture(@GestureTypes gesture: Int): Durban {
        mCropIntent?.putExtra(KEY_INPUT_GESTURE, gesture)
        return this
    }

    /**
     * The aspect ratio column of the crop box.
     *
     * @param x aspect ratio X.
     * @param y aspect ratio Y.
     */
    fun aspectRatio(x: Float, y: Float): Durban {
        mCropIntent?.putExtra(KEY_INPUT_ASPECT_RATIO, floatArrayOf(x, y))
        return this
    }

    /**
     * Use the aspect ratio column of the original image.
     */
    fun aspectRatioWithSourceImage(): Durban {
        return aspectRatio(0f, 0f)
    }

    /**
     * Set maximum size for result cropped image.
     *
     * @param width  max cropped image width.
     * @param height max cropped image height.
     */
    fun maxWidthHeight(@IntRange(from = 100) width: Int, @IntRange(from = 100) height: Int): Durban {
        mCropIntent?.putExtra(KEY_INPUT_MAX_WIDTH_HEIGHT, intArrayOf(width, height))
        return this
    }

    /**
     * The compression format of the cropped image.
     *
     * @param format image format.
     * @see .COMPRESS_JPEG
     *
     * @see .COMPRESS_PNG
     */
    fun compressFormat(@FormatTypes format: Int): Durban {
        mCropIntent?.putExtra(KEY_INPUT_COMPRESS_FORMAT, format)
        return this
    }

    /**
     * The compression quality of the cropped image.
     *
     * @param quality see [Bitmap.compress].
     * @see Bitmap.compress
     */
    fun compressQuality(quality: Int): Durban {
        mCropIntent?.putExtra(KEY_INPUT_COMPRESS_QUALITY, quality)
        return this
    }

    /**
     * Set the output directory of the cropped picture.
     */
    fun outputDirectory(folder: String): Durban {
        mCropIntent?.putExtra(KEY_INPUT_DIRECTORY, folder)
        return this
    }

    /**
     * The pictures to be cropped.
     */
    fun inputImagePaths(vararg imagePathArray: String): Durban {
        val arrayList = ArrayList<String>()
        Collections.addAll(arrayList, *imagePathArray)
        mCropIntent?.putStringArrayListExtra(KEY_INPUT_PATH_ARRAY, arrayList)
        return this
    }

    /**
     * The pictures to be cropped.
     */
    fun inputImagePaths(imagePathList: ArrayList<String>): Durban {
        mCropIntent?.putStringArrayListExtra(KEY_INPUT_PATH_ARRAY, imagePathList)
        return this
    }

    /**
     * Control panel configuration.
     */
    fun controller(controller: Controller): Durban {
        mCropIntent?.putExtra(KEY_INPUT_CONTROLLER, controller)
        return this
    }

    /**
     * Request code, callback to `onActivityResult()`.
     */
    fun requestCode(requestCode: Int): Durban {
        mCropIntent?.putExtra("requestCode", requestCode)
        return this
    }

    /**
     * Start cropping.
     */
    fun start() {
        try {
            val method = o?.javaClass?.getMethod("startActivityForResult", Intent::class.java, Int::class.javaPrimitiveType)
            if (!method?.isAccessible.orFalse) method?.isAccessible = true
            method?.invoke(o, mCropIntent, mCropIntent?.getIntExtra("requestCode", 1))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}