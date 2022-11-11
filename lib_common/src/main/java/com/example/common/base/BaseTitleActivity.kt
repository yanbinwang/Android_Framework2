package com.example.common.base

import android.view.View
import androidx.databinding.ViewDataBinding
import com.example.common.databinding.ActivityBaseBinding
import com.example.common.utils.builder.TitleBuilder

/**
 * Created by WangYanBin on 2020/6/10.
 * 带标题的基类，将整一个xml插入容器
 */
abstract class BaseTitleActivity<VDB : ViewDataBinding> : BaseActivity<VDB>() {
    private val baseBinding by lazy { ActivityBaseBinding.inflate(layoutInflater) }
    protected val titleBuilder by lazy { TitleBuilder(this, baseBinding.titleContainer) } //标题栏
    protected val viewGroup get() = baseBinding.flBaseContainer//标题页面的父容器，用于添加empty，如果不需要标题头的baseactivity，则在外层绘制一个FrameLayout

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    override fun setContentView(view: View?) {
        baseBinding.flBaseContainer.addView(binding.root)
        super.setContentView(baseBinding.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        baseBinding.unbind()
    }
    // </editor-fold>

}