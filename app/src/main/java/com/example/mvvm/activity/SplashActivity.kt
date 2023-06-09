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
 *  3.可采用TransitionTheme样式（纯白）或者继承该样式，将底色直接改为对应启动页的颜色，或者底色改为xml绘制的图片，可实现点击后瞬间加载页面，不至于黑屏（安卓进栈不重写会黑屏）
 *  4.可在当前页面调用app内全局需要的接口，比如用户信息/版本信息/服务器配置字段，自己实现一个假的2s+的倒计时，保证这些接口请求完成后再进去首页
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