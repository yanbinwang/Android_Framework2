package com.example.mvvm.activity

import android.text.TextWatcher
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.config.ARouterPath
import com.example.common.utils.builder.shortToast
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.function.pt
import com.example.common.widget.textview.edit.EditTextImpl
import com.example.common.widget.xrecyclerview.refresh.ProjectRefreshHeader
import com.example.common.widget.xrecyclerview.refresh.headerMaxDragRate
import com.example.framework.utils.function.value.toSafeDouble
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.size
import com.example.mvvm.databinding.ActivityMainBinding
import com.example.mvvm.widget.dialog.TestTopDialog

@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>(), EditTextImpl {
    //    private val illustratePopup by lazy { IllustratePopup(this) }
    private val testBottom by lazy { TestTopDialog() }

    override fun initEvent() {
        super.initEvent()
        //通过代码动态重置一下顶部的高度
        val bgHeight = 164.pt + getStatusBarHeight()
        binding.ivFundsBg.size(height = bgHeight)
        binding.llFunds.apply {
            size(height = bgHeight)
            padding(top = getStatusBarHeight())
        }
        //全屏的刷新，顶部需要空出导航栏的距离
        binding.refresh.headerMaxDragRate()
        //设置头部的滑动监听
        (binding.refresh.refreshHeader as? ProjectRefreshHeader)?.apply {
            onDragListener =
                { isDragging: Boolean, percent: Float, offset: Int, height: Int, maxDragHeight: Int ->
                    changeBgHeight(offset)
                }
        }
        binding.viewContent.click {
            "dsfdsfdsfds".shortToast()
//            testBottom.show(supportFragmentManager,"testBottom")
//            illustratePopup.showUp(it, "测试文本测试文本测试文本测试文本测试文本测试文本测文本测试文本测试文本测试本测试文本测试文本测试文本本测试文本测试文本测试文本")
        }
    }

    /**
     * 滑动时改变对应的图片高度
     */
    private fun changeBgHeight(offset: Int) {
        val imgBgHeight = binding.llFunds.measuredHeight
        if (imgBgHeight <= 0) return
        //设置视图围绕其旋转和缩放的点的 y 位置。默认情况下，枢轴点以对象为中心。设置此属性会禁用此行为并导致视图仅使用显式设置的 pivotX 和 pivotY 值。
        binding.ivFundsBg.pivotY = 0f
        //设置视图围绕轴心点在 Y 轴上缩放的量，作为视图未缩放宽度的比例。值为 1 表示不应用缩放。
        binding.ivFundsBg.scaleY = offset.toSafeFloat() / imgBgHeight.toSafeFloat() + 1f
    }

}