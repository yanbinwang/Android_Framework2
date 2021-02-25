package com.dataqin.mvvm.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.dataqin.common.base.BaseActivity
import com.dataqin.common.bus.LiveDataBus
import com.dataqin.common.bus.LiveDataEvent
import com.dataqin.common.constant.ARouterPath
import com.dataqin.common.constant.Constants
import com.dataqin.common.constant.Extras
import com.dataqin.mvvm.BR
import com.dataqin.mvvm.databinding.ActivityUserInfoBinding

/**
 * Created by WangYanBin on 2020/6/3.
 * ViewModel可不写，但是binding必须传
 */
@Route(path = ARouterPath.UserInfoActivity)
class UserInfoActivity : BaseActivity<ActivityUserInfoBinding>() {

    override fun initView() {
        super.initView()
        binding.setVariable(BR.model, intent.getParcelableExtra(Extras.BUNDLE_MODEL))
    }

    override fun initEvent() {
        super.initEvent()
        binding.btnTest.setOnClickListener {
            //发送消息
            LiveDataBus.instance.post(LiveDataEvent(Constants.APP_USER_LOGIN, "50998"))
            LiveDataBus.instance.post(LiveDataEvent(Constants.APP_USER_LOGIN_OUT))
            finish()
        }
    }

}