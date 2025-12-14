package com.example.mvvm.activity

import com.example.common.base.BaseActivity
import com.example.common.config.RouterPath
import com.example.framework.utils.function.view.click
import com.example.mvvm.databinding.ActivityMainBinding
import com.example.mvvm.widget.dialog.InputDialog
import com.therouter.router.Route

/**
 * 首页
 * https://blog.csdn.net/jaikydota163/article/details/52098869
 */
@Route(path = RouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>() {
    private val input by lazy { InputDialog(this) }

    override fun initEvent() {
        super.initEvent()
        setOnWindowInsetsChanged {
            input.setOnWindowInsetsChanged(it)
        }
        mBinding?.flInput.click {
            input.showInput()
        }
        mBinding?.tvCountry.click {
            navigation(RouterPath.CountryActivity)
        }
    }
}