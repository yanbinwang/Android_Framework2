package com.example.mvvm.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.base.utils.function.SimpleTextWatcher
import com.example.base.utils.function.parameters
import com.example.base.utils.function.textWatcher
import com.example.common.base.BaseTitleActivity
import com.example.common.constant.ARouterPath
import com.example.common.constant.Extras
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

//        viewModel.setEmptyView(baseBinding.flBaseContainer)
    }

    override fun initEvent() {
        super.initEvent()
        //多个写成全局，单个写成匿名
        object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                super.onTextChanged(s, start, before, count)
                log("account:" + binding.etAccount.parameters() + "\npassword:" + binding.etPassword.parameters() + "\n判断：" + !isEmpty(binding.etAccount.parameters(), binding.etPassword.parameters()))
                binding.btnLogin.isEnabled = !isEmpty(binding.etAccount.parameters(), binding.etPassword.parameters())
            }
        }.textWatcher(binding.etAccount, binding.etPassword)

//        PageHandler.getEmptyView(baseBinding.flBaseContainer).setOnEmptyRefreshListener(object : OnEmptyRefreshListener{
//            override fun onRefresh() {
//                showToast("我点")
//            }
//        })

        binding.btnLogin.setOnClickListener {
            viewModel.login(binding.etAccount.parameters(), binding.etPassword.parameters())
//            viewModel.getData()
        }

        //类似mvp的接口回调,通过观察泛型内容随时刷新变化
        viewModel.userInfoData.observe(this) {
//            navigation(
//                ARouterPath.UserInfoActivity,
//                PageParams().append(Extras.BUNDLE_BEAN, it)
//            )
            navigation(
                ARouterPath.UserInfoActivity,
                Extras.BUNDLE_BEAN to it
            )
        }
    }

}