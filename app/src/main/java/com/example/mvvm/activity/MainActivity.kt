package com.example.mvvm.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.base.utils.function.view.click
import com.example.base.utils.function.view.focus
import com.example.base.utils.function.view.openDecor
import com.example.common.base.BaseTitleActivity
import com.example.common.constant.ARouterPath
import com.example.mvvm.databinding.ActivityMainBinding
import com.example.mvvm.widget.CommunityEditText

/**
 * object是单例，适合做一些重复性的操作
 */
@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseTitleActivity<ActivityMainBinding>() {

    override fun initView() {
        super.initView()
        titleBuilder.setTitle("10086").getDefault()
        binding.etConnect.openDecor()
        binding.etConnect.focus()
    }

    override fun initEvent() {
        super.initEvent()
        binding.btnCopy.click {
            binding.etConnect.setAt(CommunityEditText.MentionSpan("老王", "", 10086))
        }

        binding.btnCopy2.click {
            binding.etConnect.setAt2(CommunityEditText.MentionSpan("老王", "", 10086))
        }
    }


}