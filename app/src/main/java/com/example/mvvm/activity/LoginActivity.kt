package com.example.mvvm.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseTitleActivity
import com.example.common.base.page.PageHandler
import com.example.common.base.page.PageParams
import com.example.common.base.proxy.SimpleTextWatcher
import com.example.common.constant.ARouterPath
import com.example.common.constant.Extras
import com.example.common.widget.empty.OnEmptyRefreshListener
import com.example.mvvm.bridge.LoginViewModel
import com.example.mvvm.databinding.ActivityLoginBinding

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

//        viewModel.addEmptyView(baseBinding.flBaseContainer)
    }

    override fun initEvent() {
        super.initEvent()
        //多个写成全局，单个写成匿名
        onTextChanged(object : SimpleTextWatcher() {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                super.onTextChanged(s, start, before, count)
                log("account:" + getParameters(binding.etAccount) + "\npassword:" + getParameters(binding.etPassword) + "\n判断：" + !isEmpty(getParameters(binding.etAccount), getParameters(binding.etPassword)))
                binding.btnLogin.isEnabled = !isEmpty(getParameters(binding.etAccount), getParameters(binding.etPassword))
            }

        }, binding.etAccount, binding.etPassword)

        PageHandler.getEmptyView(baseBinding.flBaseContainer).setOnEmptyRefreshListener(object : OnEmptyRefreshListener{
            override fun onRefreshListener() {
                showToast("我点")
            }
        })

        binding.btnLogin.setOnClickListener {
            viewModel.login(getParameters(binding.etAccount), getParameters(binding.etPassword))
//            viewModel.getData()
        }

        //类似mvp的接口回调,通过观察泛型内容随时刷新变化
        viewModel.userInfoData.observe(this, {
            navigation(
                ARouterPath.UserInfoActivity,
                PageParams().append(Extras.BUNDLE_MODEL, it)
            )
        })
    }

}