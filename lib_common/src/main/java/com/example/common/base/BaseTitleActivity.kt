package com.example.common.base

import android.view.View
import androidx.databinding.ViewDataBinding
import com.example.common.base.page.PageHandler
import com.example.common.databinding.ActivityBaseBinding
import com.example.common.utils.builder.TitleBuilder

/**
 * Created by WangYanBin on 2020/6/10.
 * 带标题的基类，将整一个xml插入容器
 */
abstract class BaseTitleActivity<VDB : ViewDataBinding> : BaseActivity<VDB>() {
    protected val baseBinding by lazy { ActivityBaseBinding.inflate(layoutInflater) }
    protected val titleBuilder by lazy { TitleBuilder(this, baseBinding.titleContainer) } //标题栏
    protected val emptyLayout by lazy { PageHandler.getEmpty(baseBinding.flBaseContainer) }

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    override fun setContentView(view: View?) {
        baseBinding.flBaseContainer.addView(binding.root)
        super.setContentView(baseBinding.root)
    }

//    override fun initView() {
//        super.initView()
//        emptyLayout?.showLoading()
//    }
    // </editor-fold>

}