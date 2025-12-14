package com.example.mvvm.activity

import android.os.Bundle
import com.example.common.base.BaseActivity
import com.example.common.config.RouterPath
import com.example.framework.utils.WeakHandler
import com.example.mvvm.databinding.ActivityStartBinding
import com.therouter.router.Route

/**
 *  Created by wangyanbin
 *  启动页，通常可在此做一些基础判断
 *  1.进入引导页,权限页（6.0+的手机），引导页存储对应的参数值-ConfigHelper.storageBehavior(Constants.KEY_INITIAL, true)
 *  3.免登陆，进首页
 */
@Route(path = RouterPath.StartActivity)
class StartActivity : BaseActivity<ActivityStartBinding>() {
    private val weakHandler by lazy {
        WeakHandler {
            navigation(RouterPath.MainActivity)?.finish()
            false
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        weakHandler.sendEmptyMessageDelayed(0, 2000)
    }

}