package com.example.mvvm.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.constant.ARouterPath
import com.example.mvvm.databinding.ActivityTestBinding

/**
 * @description
 * @author
 */
@Route(path = ARouterPath.TestActivity)
class TestActivity : BaseActivity<ActivityTestBinding>() {
}