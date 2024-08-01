package com.example.mvvm.activity

import android.content.Intent
import android.os.Bundle
import androidx.databinding.ViewDataBinding
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.config.ARouterPath
import com.example.common.utils.fullScreen
import com.example.framework.utils.builder.TimerBuilder.Companion.schedule
import com.example.framework.utils.function.value.second
import com.example.mvvm.databinding.ActivitySplashBinding
import me.jessyan.autosize.internal.CancelAdapt

/**
 *  Created by wangyanbin
 *  app启动页
 *  进入登录页或者首页的背景如果不是纯白色，可以继承TransitionTheme自己写一个对应颜色的样式
 */
@Route(path = ARouterPath.SplashActivity)
class SplashActivity : BaseActivity<ActivitySplashBinding>(), CancelAdapt {

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
        schedule({ navigation(ARouterPath.MainActivity).finish() }, 2.second)
    }

}