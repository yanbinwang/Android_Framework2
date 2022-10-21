package com.example.mvvm.activity

import android.annotation.SuppressLint
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.base.utils.function.view.click
import com.example.base.utils.function.view.focus
import com.example.base.utils.function.view.openDecor
import com.example.common.base.BaseTitleActivity
import com.example.common.constant.ARouterPath
import com.example.common.imageloader.ImageLoader
import com.example.common.widget.dialog.AppDialog
import com.example.mvvm.databinding.ActivityMainBinding
import com.example.mvvm.widget.TestPPP
import com.example.mvvm.widget.TestPopup


/**
 * object是单例，适合做一些重复性的操作
 */
@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseTitleActivity<ActivityMainBinding>() {
    private val appDialog by lazy { AppDialog(this) }
    private val testPPP by lazy { TestPopup(window) }

    override fun initView() {
        super.initView()
        titleBuilder.setTitle("10086").getDefault()
        binding.edt.openDecor()
        binding.edt.focus()
    }

    @SuppressLint("SetTextI18n")
    override fun initEvent() {
        super.initEvent()
        ImageLoader.instance.display(binding.ivTest,"https://gimg2.baidu.com/image_search/src=http%3A%2…sec=1667281156&t=e58ae2416a52a53c59d079b19359abd3")
        binding.btnCopy.click {
            appDialog.apply {
                setParams("dsfsdfds","dsfdsfds","dsfdsfdsfsd")
                shown()
            }
//            testPPP.shown()
//            testPPP.show(supportFragmentManager,"testPPP")
//            navigation(ARouterPath.TestActivity)
        }
        showDialog()
    }

}