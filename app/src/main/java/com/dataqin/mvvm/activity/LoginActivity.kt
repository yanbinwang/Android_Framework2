package com.dataqin.mvvm.activity

import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.dataqin.common.base.BaseTitleActivity
import com.dataqin.common.base.page.PageParams
import com.dataqin.common.base.proxy.SimpleTextWatcher
import com.dataqin.common.constant.ARouterPath
import com.dataqin.common.constant.Extras
import com.dataqin.mvvm.bridge.LoginViewModel
import com.dataqin.mvvm.databinding.ActivityLoginBinding

/**
 * Created by WangYanBin on 2020/6/3.
 * 模拟用户登录页
 */
@Route(path = ARouterPath.LoginActivity)
class LoginActivity : BaseTitleActivity<ActivityLoginBinding>() {
    private val viewModel by lazy { createViewModel(LoginViewModel::class.java) }

    override fun initView() {
        super.initView()
        titleBuilder.setTitle("登录").getDefault()
    }

    override fun initEvent() {
        super.initEvent()
        //多个写成全局，单个写成匿名
        onTextChanged(textWatcher, binding.etAccount, binding.etPassword)

        binding.btnLogin.setOnClickListener {
            viewModel.login(
                getParameters(binding.etAccount),
                getParameters(binding.etPassword)
            )
//            viewModel.getData()
        }

        //类似mvp的接口回调,通过观察泛型内容随时刷新变化
        viewModel.userInfoData.observe(this, Observer {
            navigation(
                ARouterPath.UserInfoActivity,
                PageParams().append(Extras.BUNDLE_MODEL, it)
            )
        })
    }

    private var textWatcher: SimpleTextWatcher = object : SimpleTextWatcher() {

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            super.onTextChanged(s, start, before, count)
            log(
                "account:" + getParameters(binding.etAccount) + "\npassword:" + getParameters(
                    binding.etPassword
                ) + "\n判断：" + !isEmpty(
                    getParameters(binding.etAccount),
                    getParameters(binding.etPassword)
                )
            )
            binding.btnLogin.isEnabled = !isEmpty(
                getParameters(binding.etAccount),
                getParameters(binding.etPassword)
            )
        }

    }

}