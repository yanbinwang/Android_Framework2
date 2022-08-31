package com.example.common.utils.helper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import com.example.common.constant.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 *  Created by wangyanbin
 *  生成图片工具类
 */
object GenerateHelper {
//    private val weakHandler by lazy { WeakHandler(Looper.getMainLooper()) }
//    private val executors by lazy { Executors.newSingleThreadExecutor() }

    //构建图片
    suspend fun create(view: View, onStart: () -> Unit? = {}, onResult: (bitmap: Bitmap?) -> Unit? = {}, onComplete: () -> Unit? = {}) {
        onStart()
        loadLayout(view)
//        executors.execute {
//            try {
//                val bitmap = loadBitmap(view)
//                weakHandler.post { onResult(bitmap) }
//            } catch (e: Exception) {
//            } finally {
//                weakHandler.post { onComplete() }
//            }
//        }
//        executors.isShutdown
        try {
            val bitmap = loadBitmap(view)
            withContext(Dispatchers.Main) { onResult(bitmap) }
        } catch (e: Exception) {
        } finally {
            withContext(Dispatchers.Main) { onComplete() }
        }
    }

    /**
     * 当measure完后，并不会实际改变View的尺寸，需要调用View.layout方法去进行布局
     * 按示例调用layout函数后，View的大小将会变成你想要设置成的大小
     */
    private fun loadLayout(view: View) {
        //整个View的大小 参数是左上角 和右下角的坐标
        view.layout(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT)
        val measuredWidth: Int =
            View.MeasureSpec.makeMeasureSpec(Constants.SCREEN_WIDTH, View.MeasureSpec.EXACTLY)
        val measuredHeight: Int =
            View.MeasureSpec.makeMeasureSpec(Constants.SCREEN_HEIGHT, View.MeasureSpec.EXACTLY)
        view.measure(measuredWidth, measuredHeight)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    }

    //如果不设置canvas画布为白色，则生成透明
    private fun loadBitmap(view: View): Bitmap? {
        val width = view.width
        val height = view.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        view.layout(0, 0, width, height)
        view.draw(canvas)
        return bitmap
    }

}