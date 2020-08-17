package com.example.mvvm.activity

import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseTitleActivity
import com.example.common.base.page.PageParams
import com.example.common.constant.ARouterPath
import com.example.common.constant.Extras
import com.example.mvvm.BR
import com.example.mvvm.R
import com.example.mvvm.bridge.LoginViewModel
import com.example.mvvm.bridge.event.LoginEvent
import com.example.mvvm.databinding.ActivityLoginBinding
import com.example.mvvm.model.UserInfoModel

/**
 * Created by WangYanBin on 2020/6/3.
 * 模拟用户登录页
 */
@Route(path = ARouterPath.LoginActivity)
open class LoginActivity : BaseTitleActivity<ActivityLoginBinding>() {
    protected val viewModel: LoginViewModel by lazy {
        createViewModel(LoginViewModel::class.java)
    }

    override fun getLayoutResID(): Int {
        return R.layout.activity_login
    }

    override fun initView() {
        super.initView()
        titleBuilder.setTitle("登录").getDefault()
        binding?.setVariable(BR.event, LoginEvent())
    }

    override fun initEvent() {
        super.initEvent()
        //类似mvp的接口回调,通过观察泛型内容随时刷新变化
        viewModel.userInfoLiveData.observe(this, Observer {
            navigation(
                ARouterPath.UserInfoActivity,
                PageParams().append(Extras.BUNDLE_MODEL, it)
            )
        })
    }
}