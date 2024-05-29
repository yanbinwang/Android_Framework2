package com.example.mvvm.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.config.ARouterPath
import com.example.framework.utils.function.view.click
import com.example.mvvm.databinding.ActivityMainBinding
import com.example.mvvm.widget.dialog.InputDialog

/**
 * 首页
 * https://blog.csdn.net/jaikydota163/article/details/52098869
 */
@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>() {
    private val input by lazy { InputDialog(this) }

    override fun initEvent() {
        super.initEvent()
        mBinding?.tvInput.click { input.showInput() }
    }
}