package com.example.common.base

import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.databinding.ViewDataBinding
import com.example.common.widget.AppToolbar
import com.example.framework.utils.function.view.size

/**
 * Created by WangYanBin on 2020/6/10.
 * 带标题的基类，将整一个xml插入容器
 * 由于项目中带固定标题的页面占80%以上，故而实现一个带头的activity，直接可使用titleBuilder操作
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
    protected val titleRoot get() = titleBar // 标题栏
    protected val contentRoot get() = rootView // 标题页面的父容器，用于添加empty，如果不需要标题头的BaseActivity，则在外层绘制一个FrameLayout

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        titleBar.bind(this)
        root.addView(titleBar)
        root.addView(rootView)
    }

    override fun setContentView(view: View?) {
        rootView.addView(mBinding?.root)
        super.setContentView(root)
    }

    protected fun setBackgroundResource(resid: Int) {
        root.setBackgroundResource(resid)
    }

    protected fun setBackgroundColor(color: Int) {
        root.setBackgroundColor(color)
    }
    // </editor-fold>

}