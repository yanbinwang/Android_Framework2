package com.example.mvvm.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.chad.library.BR
import com.example.common.base.BaseActivity
import com.example.common.constant.ARouterPath
import com.example.common.constant.Extras
import com.example.mvvm.R
import com.example.mvvm.bridge.event.UserInfoEvent
import com.example.mvvm.databinding.ActivityUserInfoBinding

/**
 * Created by WangYanBin on 2020/6/3.
 * ViewModel可不写，但是binding必须传
 */
@Route(path = ARouterPath.UserInfoActivity)
open class UserInfoActivity : BaseActivity<ActivityUserInfoBinding>() {

    override fun getLayoutResID(): Int {
        return R.layout.activity_user_info
    }

    override fun initView() {
        super.initView()
        binding?.setVariable(BR.model, intent.getParcelableExtra(Extras.BUNDLE_MODEL))
        binding?.setVariable(BR.event, UserInfoEvent())
    }

}