package com.example.album.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.size

/**
 * 事件传递布局（FrameLayout 包装类）
 * 作用：把自身的点击事件 转发 给唯一的子View
 * 专门用于相册里的条目点击、预览点击等场景
 */
class TransferLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    /**
     * 执行点击：如果只有1个子View，就让子View执行点击
     */
    override fun performClick(): Boolean {
        if (size == 1) {
            return getChildAt(0).performClick()
        }
        return super.performClick()
    }

    /**
     * 调用点击：同上，转发给唯一子View
     */
    override fun callOnClick(): Boolean {
        if (size == 1) {
            return getChildAt(0).performClick()
        }
        return super.performClick()
    }

}