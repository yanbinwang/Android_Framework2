package com.example.mvvm.activity

import android.Manifest
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.base.utils.function.view.clicks
import com.example.common.base.BaseTitleActivity
import com.example.common.constant.ARouterPath
import com.example.common.constant.Extras
import com.example.mvvm.R
import com.example.mvvm.databinding.ActivityMainBinding
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

/**
 * Created by WangYanBin on 2020/8/14.
 * MVVM中，Activity的代码量应该非常少，它并不和数据及控件直接交互，可不写页面的ViewModel，但有xml存在的页面则必须传入Binding文件
 * 1）如果页面涉及到View操作必须在xml文件里套入layout和data标签，让系统生成对应的Binding文件传入基类注入
 * 2）不处理逻辑的代码以及从任何入口进到Activity内的数据放在Activity处理（例如上一个类传过来的数据，事件点击---类似P层回调）其余都丢给ViewModel处理，然后通过LiveData回调给Activity
 * 3）需要处理逻辑的方法，例如网络请求获取的数据，都放在ViewModel中，涉及到UI刷新则通过回调让Activity去修改数据
 */
@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseTitleActivity<ActivityMainBinding>(), View.OnClickListener {

    override fun initView() {
        super.initView()
        titleBuilder.setTitle("10086").getDefault()
    }

    override fun initEvent() {
        super.initEvent()
        clicks(binding.btnLogin, binding.btnList, binding.btnDownload)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_login -> navigation(ARouterPath.LoginActivity)
            R.id.btn_list -> navigation(ARouterPath.TestListActivity, Extras.MOBILE to "test")
            R.id.btn_download -> {

            }
        }
    }

}