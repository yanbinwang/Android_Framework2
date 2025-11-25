package com.example.gallery.base

import android.os.Build
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.annotation.ColorRes
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.Toolbar
import androidx.databinding.ViewDataBinding
import com.example.common.R
import com.example.common.base.BaseActivity
import com.example.common.utils.ScreenUtil.shouldUseWhiteSystemBarsForRes
import com.example.common.utils.function.getStatusBarHeight
import com.example.framework.utils.function.view.doOnceAfterLayout
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.size
import com.example.framework.utils.function.view.textColor
import com.example.framework.utils.function.view.textSize
import com.example.gallery.base.bridge.Bye

class BaseGalleryActivity<VDB : ViewDataBinding> : BaseActivity<VDB>(), Bye {

    companion object {

        /**
         * 兼容控件内toolbar
         */
        @JvmStatic
        fun setSupportToolbar(toolbar: Toolbar) {
            toolbar.doOnceAfterLayout {
                val statusBarHeight = getStatusBarHeight()
                it.size(height = it.measuredHeight + statusBarHeight)
                it.padding(top = statusBarHeight)
                // 返回按钮调整
                val navButton = getNavButtonView(it)
                // 去除长按文字
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    navButton?.tooltipText = null
                }
                navButton?.setContentDescription(null)
                navButton?.setOnLongClickListener { v -> true }
                // 去除水波纹
                navButton?.background = null
            }
        }

        /**
         * 反射获取 Toolbar 中的私有字段 mNavButtonView（返回按钮）
         */
        private fun getNavButtonView(toolbar: Toolbar): ImageButton? {
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
                        adjustActionMenuView(it, colorRes)
                    }
                }
            }
        }

        private fun adjustActionMenuView(menuView: ActionMenuView, @ColorRes colorRes: Int) {
            for (i in 0..<menuView.childCount) {
                val itemView = menuView.getChildAt(i)
                // 打破 ActionMenuItemView 的高度限制
                if (itemView is ActionMenuItemView) {
                    // 取消最小高度限制
                    itemView.setMinHeight(0)
                    // 取消最大高度限制
                    itemView.setMaxHeight(Int.MAX_VALUE)
                    // 强制 ActionMenuItemView 高度占满父容器（ActionMenuView）
                    val lp = itemView.layoutParams
                    lp.height = ViewGroup.LayoutParams.MATCH_PARENT
                    itemView.setLayoutParams(lp)
                    // 清除 ActionMenuItemView 自身的 padding
                    itemView.setPadding(itemView.getPaddingLeft(), 0, itemView.getPaddingRight(), 0)
                    // 内容居中
                    itemView.gravity = Gravity.CENTER
                    // 颜色调整
                    if (!shouldUseWhiteSystemBarsForRes(colorRes)) {
                        itemView.textColor(R.color.textBlack)
                    } else {
                        itemView.textColor(R.color.textWhite)
                    }
                    // 字体大小
                    itemView.textSize(R.dimen.textSize14)
                }
            }
        }

    }

    /**
     * 1.bye() 方法中直接调用了 onBackPressed()
     * 2.在未重写 onBackPressed() 的情况下，会执行 Activity 类的默认实现
     * 3.系统默认的 onBackPressed() 最终会调用 finish() 销毁当前 Activity
     */
    override fun bye() {
//        onBackPressed()
        finish()
    }

}