package com.example.mvvm.activity

import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.constant.ARouterPath
import com.example.mvvm.adapter.ColorFragmentAdapter
import com.example.mvvm.databinding.ActivityTestListBinding
import com.example.mvvm.fragment.ColorFragment

/**
 * Created by WangYanBin on 2020/6/4.
 */
@Route(path = ARouterPath.TestListActivity)
class TestListActivity : BaseActivity<ActivityTestListBinding>() {
    private var mPageAdapter: ColorFragmentAdapter? = null
    private val mViewPagerFragments = ArrayList<Fragment>()


    override fun initView() {
        super.initView()
        for (i in 0 until 10) {
            mViewPagerFragments.add(ColorFragment())
        }
        mPageAdapter = ColorFragmentAdapter(supportFragmentManager, mViewPagerFragments)
        binding.stack.initStack(2)
        binding.stack.adapter =
            mPageAdapter //assuming mStackAdapter contains your initialized adapter
    }

    override fun initEvent() {
        super.initEvent()


    }


}