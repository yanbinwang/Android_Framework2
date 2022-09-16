package com.example.mvvm.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.bus.Event
import com.example.common.bus.EventBus
import com.example.common.constant.ARouterPath
import com.example.common.constant.Constants
import com.example.common.constant.Extras
import com.example.mvvm.BR
import com.example.mvvm.databinding.ActivityUserInfoBinding

/**
 * Created by WangYanBin on 2020/6/3.
 * ViewModel可不写，但是binding必须传
 */
@Route(path = ARouterPath.UserInfoActivity)
class UserInfoActivity : BaseActivity<ActivityUserInfoBinding>() {

    override fun initView() {
        super.initView()
        binding.setVariable(BR.model, intent.getParcelableArrayExtra(Extras.BUNDLE_BEAN))
    }

    override fun initEvent() {
        super.initEvent()
        binding.btnTest.setOnClickListener {
            //发送消息
            EventBus.instance.post(
                Event(Constants.APP_USER_LOGIN, "50998"),
                Event(Constants.APP_USER_LOGIN_OUT))
            finish()
        }
    }

}