package com.example.mvvm.activity

import android.os.Bundle
import com.example.common.base.BaseTitleActivity
import com.example.common.config.RouterPath
import com.example.common.widget.xrecyclerview.gesture.touch.ItemTouchHelperCallBack
import com.example.common.widget.xrecyclerview.gesture.touch.ItemTouchHelper
import com.example.mvvm.BR
import com.example.mvvm.adapter.ItemAdapter
import com.example.mvvm.bean.TestBean
import com.example.mvvm.databinding.ActivityTouchBinding
import com.therouter.router.Route

@Route(path = RouterPath.TouchActivity)
class TouchActivity : BaseTitleActivity<ActivityTouchBinding>() {
    private val lastData by lazy { ArrayList<TestBean>() }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        titleRoot.setTitle("手势拖动")
        mBinding?.setVariable(BR.adapter, ItemAdapter())
    }

    override fun initEvent() {
        super.initEvent()
        // 拖拽移动和左滑删除
        val simpleItemTouch = ItemTouchHelperCallBack(mBinding?.adapter)
        // 要实现侧滑删除条目，把 false 改成 true 就可以了
        simpleItemTouch.setmSwipeEnable(false)
        val helper = ItemTouchHelper(simpleItemTouch)
        // 设置是否关闭刷新
        helper.setOnMoveListener { move ->
//            mBinding?.xrvList?.recycler.setEnableSwipe(!move)
        }
        helper.attachToRecyclerView(mBinding?.xrvList?.recycler)
    }

    private fun setEnableSwipe(enable: Boolean) {
//        int refreshDirection = swipeRefreshLayout.getDirection();
//        switch (refreshDirection) {
//            //顶部
//            case 0:
//                swipeRefreshLayout.setEnableRefresh(enable);
//                swipeRefreshLayout.setEnableLoadmore(false);
//                swipeRefreshLayout.setEnableOverScroll(false);
//                break;
//            //底部
//            case 1:
//                swipeRefreshLayout.setEnableRefresh(false);
//                swipeRefreshLayout.setEnableLoadmore(enable);
//                swipeRefreshLayout.setEnableOverScroll(false);
//                break;
//            //都有（默认）
//            case 2:
//                swipeRefreshLayout.setEnableLoadmore(enable);
//                swipeRefreshLayout.setEnableRefresh(enable);
//                swipeRefreshLayout.setEnableOverScroll(enable);
//                break;
//        }
    }

    override fun initData() {
        super.initData()
        lastData.clear()
        for (i in 0 until 10) {
            lastData.add(TestBean("用户id:${i}", "用户名:老王-${i}"))
        }
        mBinding?.adapter?.notify(lastData)
    }

}