package com.example.common.base

import android.view.View
import androidx.databinding.ViewDataBinding
import com.example.common.databinding.ActivityBaseBinding
import com.example.common.utils.builder.TitleBuilder

/**
 * Created by WangYanBin on 2020/6/10.
 * 带标题的基类，将整一个xml插入容器
 * 由于项目中带固定标题的页面占80%以上，故而实现一个带头的activity，直接可使用titleBuilder操作
 */
abstract class BaseTitleActivity<VDB : ViewDataBinding> : BaseActivity<VDB>() {
    private val baseBinding by lazy { ActivityBaseBinding.inflate(layoutInflater) }
    protected val titleBuilder by lazy { TitleBuilder(this, baseBinding.titleRoot) } //标题栏
    protected val viewGroup get() = baseBinding.flBaseRoot//标题页面的父容器，用于添加empty，如果不需要标题头的baseactivity，则在外层绘制一个FrameLayout

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    protected fun setBackgroundResource(resid: Int) {
        baseBinding.llRoot.setBackgroundResource(resid)
    }

    protected fun setBackgroundColor(color: Int) {
        baseBinding.llRoot.setBackgroundColor(color)
    }

    override fun setContentView(view: View?) {
        baseBinding.flBaseRoot.addView(mBinding?.root)
        super.setContentView(baseBinding.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        baseBinding.unbind()
    }
    // </editor-fold>

}