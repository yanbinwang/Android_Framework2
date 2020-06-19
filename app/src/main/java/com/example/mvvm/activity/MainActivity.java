package com.example.mvvm.activity;


import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.example.common.base.BaseTitleActivity;
import com.example.common.constant.ARouterPath;
import com.example.mvvm.BR;
import com.example.mvvm.R;
import com.example.mvvm.bridge.MainViewModel;
import com.example.mvvm.databinding.ActivityMainBinding;

/**
 * MVVM中，Activity的代码量应该非常少，它并不和数据及控件直接交互，可不写页面的ViewModel，但有xml存在的页面则必须传入Binding文件
 * 1）如果页面涉及到View操作必须在xml文件里套入layout和data标签，让系统生成对应的Binding文件传入基类注入
 * 2）不处理逻辑的操作代码放在Activity，其余的都丢给ViewModel处理，然后通过LiveData回调操作
 * 3）需要处理逻辑的方法，以及从任何入口进去到Activity内的数据，例如上一个类传过来的数据，网络请求获取的数据等等，都放在ViewModel中
 */
@Route(path = ARouterPath.MainActivity)
public class MainActivity extends BaseTitleActivity<MainViewModel, ActivityMainBinding> {

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {
        super.initView();
        titleBuilder.setTitle("10086").getDefault();
    }

    @Override
    public void initEvent() {
        super.initEvent();
        setVariable(BR.click, new ClickProxy());
    }

    public class ClickProxy {

        public View.OnClickListener onClickListener = v -> {
            switch (v.getId()) {
                case R.id.btn_login:
                    navigation(ARouterPath.LoginActivity);
                    break;
                case R.id.btn_list:
                    navigation(ARouterPath.TestListActivity);
                    break;
            }
        };

    }

}