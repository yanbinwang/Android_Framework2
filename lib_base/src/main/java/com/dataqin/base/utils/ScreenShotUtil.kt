package com.dataqin.base.utils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView
import java.lang.ref.WeakReference

/**
 * author: wyb
 * date: 2019/1/28.
 * 截图工具类
 */
object ScreenShotUtil {

    //截取除了导航栏之外的整个屏幕
    @JvmStatic
    fun getWholeViewBitmap(activity: Activity): Bitmap {
        val mActivity = WeakReference(activity)
        val dView = mActivity.get()!!.window.decorView
        dView.isDrawingCacheEnabled = true
        dView.buildDrawingCache()
        return Bitmap.createBitmap(dView.drawingCache)
    }

    //获取View在屏幕可见区域的截图
    @JvmStatic
    fun getViewBitmap(view: View): Bitmap {
        //        //开启缓存功能
        //        view.setDrawingCacheEnabled(true);
        //        //创建缓存
        //        view.buildDrawingCache();
        //        //获取缓存Bitmap
        //        return Bitmap.createBitmap(view.getDrawingCache());
        view.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        view.buildDrawingCache()
        return view.drawingCache
    }

    //截取scrollview的屏幕
    @JvmStatic
    fun getScrollViewBitmap(scrollView: ScrollView): Bitmap {
        var height = 0
        for (i in 0 until scrollView.childCount) {
            height += scrollView.getChildAt(i).height
        }
        return getScrollViewBitmap(scrollView, height)
    }

    //截取scrollview的屏幕(传入高度)
    @JvmStatic
    fun getScrollViewBitmap(scrollView: ScrollView, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(scrollView.width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        scrollView.draw(canvas)
        return bitmap
    }

    //截取nestedScrollView的屏幕
    @JvmStatic
    fun getNestedScrollViewBitmap(nestedScrollView: NestedScrollView): Bitmap {
        var height = 0
        for (i in 0 until nestedScrollView.childCount) {
            height += nestedScrollView.getChildAt(i).height
        }
        return getNestedScrollViewBitmap(nestedScrollView, height)
    }

    //截取nestedScrollView的屏幕(传入高度)
    @JvmStatic
    fun getNestedScrollViewBitmap(nestedScrollView: NestedScrollView, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(nestedScrollView.width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        nestedScrollView.draw(canvas)
        return bitmap
    }

    //横向拼接
    @JvmStatic
    fun addHorizontalBitmap(first: Bitmap, second: Bitmap): Bitmap {
        val width = first.width + second.width
        val height = Math.max(first.height, second.height)
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(result)
        canvas.drawBitmap(first, 0f, 0f, null)
        canvas.drawBitmap(second, first.width.toFloat(), 0f, null)
        return result
    }

    //纵向拼接
    @JvmStatic
    fun addVerticalBitmap(first: Bitmap, second: Bitmap): Bitmap {
        val width = Math.max(first.width, second.width)
        val height = first.height + second.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(result)
        canvas.drawBitmap(first, 0f, 0f, null)
        canvas.drawBitmap(second, 0f, first.height.toFloat(), null)
        return result
    }

    //    //对WebView进行截屏
    //    public static Bitmap captureWebView(WebView webView) {
    //        //5.0之前
    //        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
    //            Picture picture = webView.capturePicture();
    //            int width = picture.getWidth();
    //            int height = picture.getHeight();
    //            if (width > 0 && height > 0) {
    //                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
    //                Canvas canvas = new Canvas(bitmap);
    //                picture.draw(canvas);
    //                return bitmap;
    //            }
    //        }else{
    //            //5.0以上的版本对webview有优化，webveiw只绘制显示部分。如果截长图需要在初始化页面之前先关闭优化
    //            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
    //            //  WebView.enableSlowWholeDocumentDraw();
    //            //}
    //            //setContentView(R.layout.activity_test);
    //            float scale = webView.getScale();
    //            int width = webView.getWidth();
    //            int height = (int) (webView.getContentHeight() * scale + 0.5);
    //            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
    //            Canvas canvas = new Canvas(bitmap);
    //            webView.draw(canvas);
    //            return bitmap;
    //        }
    //        return null;
    //    }

}