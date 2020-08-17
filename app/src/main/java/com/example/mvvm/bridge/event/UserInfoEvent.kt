package com.example.mvvm.bridge.event

import android.view.View
import com.example.common.bus.LiveDataBus
import com.example.common.constant.Constants
import com.example.mvvm.R
import com.example.mvvm.activity.UserInfoActivity

/**
 * Created by WangYanBin on 2020/8/17.
 */
class UserInfoEvent : UserInfoActivity() {

    fun onClick(v: View) {
        when (v.id) {
            R.id.btn_test -> {
//                ActivityCollector.finishAll()
//                navigation(ARouterPath.TestListActivity)
                //发送消息
                LiveDataBus.get().post(Constants.APP_USER_LOGIN_OUT).postValue("50998")
                finish()
            }
        }
    }

}