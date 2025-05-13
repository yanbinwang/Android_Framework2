package com.example.common.base

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.databinding.ViewDataBinding
import com.example.common.widget.AppToolbar

abstract class BaseTitleFragment<VDB : ViewDataBinding> : BaseFragment<VDB>() {
    private val toolbarTitle by lazy { context?.let { AppToolbar(it) } } //标题栏
    private val rootView by lazy {
        context?.let {
            FrameLayout(it).apply {
                layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            }
        }
    }
    protected val titleBuilder get() = toolbarTitle
    protected val viewGroup get() = rootView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titleBuilder?.bind(mActivity)
//        // 监听返回按钮
//        toolbar.setNavigationOnClickListener {
//            findNavController().popBackStack()
//        }
    }
}