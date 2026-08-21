package com.example.topsheet

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.example.topsheet.TopSheetBehavior.Companion.from

/**
 * 核心的对话框容器类（继承自 AppCompatDialog）
 * 负责：
 * 1) 加载包含 CoordinatorLayout 的布局
 * 2) 将用户设置的内容视图（ContentView）包裹进顶部的 FrameLayout 中
 * 3) 绑定 TopSheetBehavior 以实现滑动逻辑
 * 4) 处理点击外部区域关闭、状态变化自动 dismiss 等交互
 */
class TopSheetDialog @JvmOverloads constructor(context: Context, @StyleRes theme: Int = 0) : AppCompatDialog(context, getThemeResId(context, theme)) {
    // 面板状态回调：当面板完全隐藏时自动关闭对话框
    private val mTopSheetCallback by lazy {
        object : TopSheetBehavior.TopSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == TopSheetBehavior.STATE_HIDDEN) {
                    dismiss()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        }
    }

    init {
        // 移除对话框默认标题栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    companion object {
        /**
         * 获取对话框主题资源ID，优先使用当前主题配置，否则使用默认样式
         */
        private fun getThemeResId(context: Context, themeId: Int): Int {
            return if (themeId == 0) {
                val outValue = TypedValue()
                if (context.theme.resolveAttribute(R.attr.bottomSheetDialogTheme, outValue, true)) {
                    outValue.resourceId
                } else {
                    R.style.Theme_Design_TopSheetDialog
                }
            } else {
                themeId
            }
        }
    }

    /**
     * 设置窗口全屏布局以支持顶部滑动行为
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    /**
     * 重写内容视图设置，将视图包裹进顶部面板容器中
     */
    override fun setContentView(layoutResID: Int) {
        super.setContentView(wrapInTopSheet(layoutResID, null, null))
    }

    override fun setContentView(view: View) {
        super.setContentView(wrapInTopSheet(0, view, null))
    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams?) {
        super.setContentView(wrapInTopSheet(0, view, params))
    }

    /**
     * 将内容视图包裹进 CoordinatorLayout 和 TopSheet 结构中，并绑定滑动行为与外部点击事件
     */
    private fun wrapInTopSheet(layoutResId: Int, view: View?, params: ViewGroup.LayoutParams?): View {
        val coordinator = View.inflate(context, R.layout.top_sheet_dialog, null) as CoordinatorLayout
        val topSheet = coordinator.findViewById<FrameLayout>(R.id.design_top_sheet)
        val topSheetBehavior = from(topSheet)
        topSheetBehavior.setTopSheetCallback(mTopSheetCallback)
        val contentView = if (layoutResId != 0 && view == null) {
            layoutInflater.inflate(layoutResId, coordinator, false)
        } else {
            view
        }
        if (params == null) {
            topSheet.addView(contentView)
        } else {
            topSheet.addView(contentView, params)
        }
        coordinator.findViewById<View>(R.id.top_sheet_touch_outside).setOnClickListener {
            if (isShowing) {
                cancel()
            }
        }
        return coordinator
    }

}