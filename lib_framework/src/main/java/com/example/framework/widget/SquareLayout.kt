package com.example.framework.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout

/**
 * author: wyb
 * date: 2017/8/29.
 * 嵌套的外层布局，使view的宽高一致
 * 适用于贴吧底部排版image，套个recyclerview，配置manager后再在布局外层套该控件，解决图片大小问题
 */
class SquareLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RelativeLayout(context, attrs, defStyleAttr) {

    /**
     * 重写此方法后默认调用父类的onMeasure方法,分别将宽度测量空间与高度测量空间传入
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

}