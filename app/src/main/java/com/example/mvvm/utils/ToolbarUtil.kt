package com.example.mvvm.utils

import android.annotation.SuppressLint
import android.os.Build
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.annotation.ColorRes
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isNotEmpty
import com.example.common.utils.ScreenUtil.shouldUseWhiteSystemBarsForRes
import com.example.common.utils.function.getStatusBarHeight
import com.example.framework.utils.function.view.doOnceAfterLayout
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.size
import com.example.framework.utils.function.view.textColor
import com.example.framework.utils.function.view.textSize
import com.example.gallery.R

object ToolbarUtil {

    /**
     * 兼容控件内toolbar
     */
    @JvmStatic
    fun setSupportToolbar(toolbar: Toolbar?) {
        toolbar.doOnceAfterLayout { tb ->
            // 取当前页面状态栏高度
            val statusBarHeight = getStatusBarHeight()
            // 设置高度
            tb.size(height = tb.measuredHeight + statusBarHeight)
            // 设置左、右内边距全为0
            tb.padding(top = statusBarHeight, start = 0, end = 0)
            // 取出系统按钮
            val systemNavBtn = getNavButtonView(tb)
            // 去除水波纹
            systemNavBtn?.background = null
            // 去除长按文字
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                systemNavBtn?.tooltipText = null
            }
            systemNavBtn?.setContentDescription(null)
            systemNavBtn?.setOnLongClickListener { _ -> true }
        }
    }

    /**
     * 反射获取 Toolbar 中的私有字段 mNavButtonView（返回按钮）
     */
    private fun getNavButtonView(toolbar: Toolbar?): ImageButton? {
        try {
            // 获取 Toolbar 类中的 mNavButtonView 字段
            val field = Toolbar::class.java.getDeclaredField("mNavButtonView")
            // 设置字段可访问（私有字段需要开启）
            field.isAccessible = true
            // 获取字段值（即返回按钮的 ImageButton 实例）
            return field.get(toolbar) as? ImageButton
        } catch (e: Exception) {
            // 转换异常
            e.printStackTrace()
        }
        return null
    }

    /**
     * 处理纯图片的按钮
     * 页面的onCreateOptionsMenu中调取,此时Toolbar已经加载完成
     */
    @JvmStatic
    fun setSupportMenuView(toolbar: Toolbar, @ColorRes colorRes: Int) {
        for (i in 0 until toolbar.childCount) {
            val child = toolbar.getChildAt(i)
            if (child is ActionMenuView) {
                // 设定的按钮被绘制为ActionMenuView,本身高度看似撑满屏幕并且绘制也是,但其内部的view还是带有一定的上下边距
                child.doOnceAfterLayout {
                    adjustActionMenuView(toolbar, it, colorRes)
                }
            }
        }
    }

    /**
     * 1) 32ms = 屏幕一帧的时间（约 30fps） 既不卡 UI，又能最快感知到菜单出现
     * 2) 大多数手机 60fps → 16ms 刷新一次 , 低一点 30fps → 32ms 刷新一次 , 32ms 就是「等下一帧渲染完」
     */
    @JvmStatic
    fun setSupportMenuViewAsync(toolbar: Toolbar?, @ColorRes colorRes: Int) {
        toolbar ?: return
        val interval = 32L   // 每帧检查一次
        val maxRetry = 30    // 最多重试30次 ≈ 1秒超时
        var retry = 0        // 正确计数
        val runnable = object : Runnable {
            override fun run() {
                // View 已销毁 / 超时 → 停止轮询
                if (!toolbar.isAttachedToWindow || retry >= maxRetry) {
                    toolbar.removeCallbacks(this)
                    return
                }
                // 查找菜单
                var found = false
                for (i in 0 until toolbar.childCount) {
                    val child = toolbar.getChildAt(i)
                    if (child is ActionMenuView) {
                        if (child.isNotEmpty()) {
                            adjustActionMenuView(toolbar, child, colorRes)
                            found = true
                        }
                    }
                }
                // 找到/没找到
                if (found) {
                    toolbar.removeCallbacks(this)
                } else {
                    // 正确计数
                    retry++
                    toolbar.postDelayed(this, interval)
                }
            }
        }
        toolbar.post(runnable)
    }

    @SuppressLint("RestrictedApi")
    private fun adjustActionMenuView(toolbar: Toolbar, menuView: ActionMenuView, @ColorRes colorRes: Int) {
        for (i in 0..<menuView.childCount) {
            val itemView = menuView.getChildAt(i)
            // 打破 ActionMenuItemView 的高度限制
            if (itemView is ActionMenuItemView) {
                // 取消最小高度限制
                itemView.minHeight = 0
                // 取消最大高度限制
                itemView.maxHeight = Int.MAX_VALUE
                // 强制 ActionMenuItemView 高度占满父容器（ActionMenuView）
                val lp = itemView.layoutParams
                lp.height = ViewGroup.LayoutParams.MATCH_PARENT
                itemView.layoutParams = lp
                // 清除 ActionMenuItemView 自身的 padding
                itemView.setPadding(itemView.paddingLeft, 0, itemView.paddingRight, 0)
                // 内容居中
                itemView.gravity = Gravity.CENTER
                // 颜色调整
                if (!shouldUseWhiteSystemBarsForRes(colorRes)) {
                    itemView.textColor(R.color.galleryFontDark)
                } else {
                    itemView.textColor(R.color.galleryFontLight)
                }
                // 字体大小
                itemView.textSize(R.dimen.textSize14)
                // 大小修正 -> 判断这个按钮有没有 ICON
                val hasIcon = itemView.itemData?.icon != null
                if (hasIcon) {
                    val adjustHeight = toolbar.measuredHeight - getStatusBarHeight()
                    itemView.size(width = adjustHeight)
                }
                // 干掉菜单按钮水波纹
                itemView.background = null
                itemView.foreground = null
                itemView.setBackgroundResource(0)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    itemView.tooltipText = null
                }
                itemView.setOnLongClickListener { true }
            }
        }
    }

}