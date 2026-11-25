package com.example.common.base

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.databinding.ViewDataBinding
import com.example.common.widget.AppToolbar
import com.example.common.widget.xrecyclerview.XRecyclerView
import com.example.framework.utils.function.view.size

/**
 * Created by WangYanBin on 2020/6/10.
 * 1) 带标题的基类，将整一个 xml 插入容器
 * 2) 项目中带固定标题的页面占 80% 以上，实现带头的 Activity 直接使用 titleRoot 操作
 */
abstract class BaseTitleActivity<VDB : ViewDataBinding> : BaseActivity<VDB>() {
    private val root by lazy {
        LinearLayout(this).apply {
            size(MATCH_PARENT, MATCH_PARENT)
            orientation = LinearLayout.VERTICAL
        }
    }
    private val titleBar by lazy { AppToolbar(this).apply {
        size(MATCH_PARENT, WRAP_CONTENT)
        onInflate()
    }}
    private val rootView by lazy { FrameLayout(this).apply {
        size(MATCH_PARENT, MATCH_PARENT)
    }}
    protected var contentRoot: FrameLayout? = null // 标题页面的父容器，用于添加 EmptyLayout / 如果不需要标题头，使用 BaseActivity 并在外层绘制一个 FrameLayout
    protected val titleRoot get() = titleBar // 标题栏

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        titleBar.bind(this)
    }

    override fun setContentView(view: View?) {
        // 重置布局 + 固定添加顺序
        root.removeAllViews()
        root.addView(titleBar)
        // mBinding.root 为 null 时直接返回，避免后续操作
        val bindingRoot = mBinding?.root ?: return
        // 如果 xml 内包含一个自定义个 XRecyclerView 是不需要 rootView 的
        if (isOnlyWrapXRecyclerView(bindingRoot)) {
            // 使用局部变量，避免重复 mBinding?.root 调用
            root.addView(bindingRoot)
            contentRoot = (bindingRoot as? XRecyclerView)?.root
        } else {
            root.addView(rootView)
            rootView.addView(bindingRoot)
            contentRoot = rootView
        }
        super.setContentView(root)
    }

    private fun isOnlyWrapXRecyclerView(bindingRoot: View): Boolean {
        val parentGroup = bindingRoot as? ViewGroup ?: return false
        return parentGroup.childCount == 1 && parentGroup.getChildAt(0) is XRecyclerView
    }

    protected fun setBackgroundResource(@DrawableRes resid: Int) {
        root.setBackgroundResource(resid)
    }

    protected fun setBackgroundColor(@ColorInt color: Int) {
        root.setBackgroundColor(color)
    }
    // </editor-fold>

}