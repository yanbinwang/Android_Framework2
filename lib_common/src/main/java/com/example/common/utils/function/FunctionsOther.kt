package com.example.common.utils.function

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.customview.widget.ViewDragHelper
import androidx.drawerlayout.widget.DrawerLayout
import com.example.base.utils.function.*
import com.example.common.BaseApplication
import com.example.common.R
import com.google.android.material.tabs.TabLayout

//------------------------------------按钮，控件行为工具类------------------------------------
///**
// * 获取拖拽范围
// * @param slideWidth  拖拽范围
// */
//fun DrawerLayout?.getDrawerLeftEdgeSize(): Int {
//    val density = (this?.context ?: BaseApplication.instance).resources.displayMetrics.density
//    val default = (20 * density + 0.5f).toInt()
//    this ?: return default
//    return try {
//        val leftDraggerField = this::class.java.getDeclaredField("mLeftDragger")
//        leftDraggerField.isAccessible = true
//        val leftDragger = leftDraggerField.get(this) as? ViewDragHelper ?: return default
//        // find edgesize and set is accessible
//        val edgeSizeField = leftDragger::class.java.getDeclaredField("mEdgeSize")
//        edgeSizeField.isAccessible = true
//        edgeSizeField.getInt(leftDragger).min(default)
//    } catch (e: Exception) {
//        default
//    }
//}
//
///**
// * 获取一个 View 的缓存视图
// *
// * @param view
// * @return
// */
//fun View?.getBitmapFromView(w: Int? = null, h: Int? = null, needBg: Boolean = true): Bitmap? {
//    this ?: return null
//    //请求转换
//    return try {
//        val screenshot = Bitmap.createBitmap(width, height, if (needBg) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_4444)
//        val c = Canvas(screenshot)
//        if (needBg) {
//            c.drawColor(color(R.color.bgDefault))
//        }
//        draw(c)
//        if (w != null && h != null) {
//            screenshot.resizeBitmap(w, h)
//        } else {
//            screenshot
//        }
//    } catch (e: Exception) {
//        null
//    }
//}

///**重设bitmap大小*/
//fun Bitmap?.resizeBitmap(width: Int, height: Int): Bitmap? {
//    this ?: return null
//    val oriWidth = this.width
//    val oriHeight = this.height
//    val matrix = Matrix()
//    matrix.postScale(width / oriWidth.toFloat(), height / oriHeight.toFloat()) //长和宽放大缩小的比例
//    val resultBitmap = Bitmap.createBitmap(this, 0, 0, this.width, this.height, matrix, true)
//    this.recycle()
//    return resultBitmap
//}
///**
// * 设置覆盖色
// * */
//fun ImageView?.tint(@ColorRes res: Int) {
//    this ?: return
//    setColorFilter(color(res))
//}
//
///**
// * 设置覆盖色
// * */
//fun ImageView?.tintColor(@ColorInt res: Int) {
//    this ?: return
//    setColorFilter(res)
//}
//
///**
// * 设置TabLayout的边距
// * */
//fun TabLayout?.paddingEdge(start: Int? = null, top: Int? = null, end: Int? = null, bottom: Int? = null) {
//    this ?: return
//    val view = (getChildAt(0) as? ViewGroup)?.getChildAt(0) as? ViewGroup
//    view?.padding(start, top, end, bottom)
//    view?.clipToPadding = false
//}


/**
 * 获取resources中的color
 */
@ColorInt
fun color(@ColorRes res: Int): Int {
    return BaseApplication.instance?.resources?.getColor(res).toSafeInt()
}

val Number?.dp: Int
    get() {
        return BaseApplication.instance!!.dip2px(this.toSafeFloat())
    }

val Number?.px: Int
    get() {
        return BaseApplication.instance!!.px2dip(this.toSafeFloat())
    }

/**
 * 设置按钮显影图片
 */
fun ImageView.setResource(triple: Triple<Boolean, Int, Int>) = setImageResource(if (!triple.first) triple.third else triple.second)

/**
 * 设置textview内容当中某一段的颜色
 */
@JvmOverloads
fun TextView.setSpan(textStr: String, keyword: String, colorRes: Int = R.color.blue_0d86ff) {
    val spannable = SpannableString(textStr)
    val index = textStr.indexOf(keyword)
    text = if (index != -1) {
        spannable.setSpan(ForegroundColorSpan(context.color(colorRes)), index, index + keyword.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        spannable
    } else textStr
}

/**
 * 设置显示内容和对应文本颜色
 */
@JvmOverloads
fun TextView.setParameter(textStr: String = "", colorRes: Int = R.color.blue_0d86ff, resid: Int = 0) {
    if (!TextUtils.isEmpty(textStr)) text = textStr
    setTextColor(context.color(colorRes))
    setBackgroundResource(resid)
}

/**
 * 状态改变
 */
fun CheckBox.checked() {
    isChecked = !isChecked
}