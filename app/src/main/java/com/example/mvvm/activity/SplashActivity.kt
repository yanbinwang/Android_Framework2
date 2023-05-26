package com.example.mvvm.activity

import android.content.Intent
import android.os.Bundle
import androidx.databinding.ViewDataBinding
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.config.ARouterPath
import com.example.common.utils.fullScreen
import com.example.framework.utils.WeakHandler
import me.jessyan.autosize.internal.CancelAdapt

/**
 *  Created by wangyanbin
 *  app启动页
 *  1.安卓本身bug会在初次安装应用后点击图标再次拉起启动页，造成界面显示不不正常
 *  2.启动页去除autosize的兼容布局，改用dp绘制，因为启动app使用了自定义的dp背景，启动app时布局兼容并未启动会大小不一
 */
@Route(path = ARouterPath.SplashActivity)
class SplashActivity : BaseActivity<ViewDataBinding>(), CancelAdapt {
    private val weakHandler by lazy {
        WeakHandler {
            navigation(ARouterPath.MainActivity).finish()
            false
        }
    }

    override fun isImmersionBarEnabled() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!isTaskRoot
            && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
            && intent.action != null
            && intent.action == Intent.ACTION_MAIN) {
            finish()
            return
        }
        window.fullScreen()
        super.onCreate(savedInstanceState)
        weakHandler.sendEmptyMessageDelayed(0, 2000)
    }

}