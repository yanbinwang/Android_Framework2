package com.example.mvvm.activity

import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseTitleActivity
import com.example.common.bus.LiveDataBus
import com.example.common.constant.ARouterPath
import com.example.common.constant.Constants
import com.example.mvvm.BR
import com.example.mvvm.R
import com.example.mvvm.bridge.event.MainEvent
import com.example.mvvm.databinding.ActivityMainBinding

/**
 * Created by WangYanBin on 2020/8/14.
 * MVVM中，Activity的代码量应该非常少，它并不和数据及控件直接交互，可不写页面的ViewModel，但有xml存在的页面则必须传入Binding文件
 * 1）如果页面涉及到View操作必须在xml文件里套入layout和data标签，让系统生成对应的Binding文件传入基类注入
 * 2）不处理逻辑的代码以及从任何入口进到Activity内的数据放在Activity处理（例如上一个类传过来的数据，事件点击---类似P层回调）其余都丢给ViewModel处理，然后通过LiveData回调给Activity
 * 3）需要处理逻辑的方法，例如网络请求获取的数据，都放在ViewModel中，涉及到UI刷新则通过回调让Activity去修改数据
 */
@Route(path = ARouterPath.MainActivity)
open class MainActivity : BaseTitleActivity<ActivityMainBinding>() {

    override fun getLayoutResID(): Int {
        return R.layout.activity_main
    }

    override fun initView() {
        super.initView()
        titleBuilder.setTitle("10086").getDefault()
        binding.setVariable(BR.event, MainEvent())
    }

    override fun initEvent() {
        super.initEvent()
        //注册订阅
        LiveDataBus.get().toFlowable(Constants.APP_USER_LOGIN_OUT, String::class.java)
            .observe(this, Observer {
                titleBuilder.setTitle(it).getDefault()
            })
    }

}