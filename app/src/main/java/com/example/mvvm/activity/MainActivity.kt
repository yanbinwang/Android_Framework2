package com.example.mvvm.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.config.ARouterPath
import com.example.mvvm.databinding.ActivityMainBinding

/**
 * 首页
 */
@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>() {

}