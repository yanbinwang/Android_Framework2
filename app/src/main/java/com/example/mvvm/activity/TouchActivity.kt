package com.example.mvvm.activity

import android.content.Intent
import android.os.Bundle
import com.example.common.base.BaseTitleActivity
import com.example.common.base.page.Extra
import com.example.common.base.page.ResultCode
import com.example.common.config.RouterPath
import com.example.common.widget.xrecyclerview.gesture.ItemTouchHelper
import com.example.common.widget.xrecyclerview.gesture.ItemTouchCallBack
import com.example.common.widget.xrecyclerview.refresh.finishRefreshing
import com.example.framework.utils.function.value.toArrayList
import com.example.framework.utils.function.view.disable
import com.example.framework.utils.function.view.enable
import com.example.mvvm.BR
import com.example.mvvm.adapter.ItemAdapter
import com.example.mvvm.bean.TestBean
import com.example.mvvm.databinding.ActivityTouchBinding
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import com.therouter.router.Route
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Route(path = RouterPath.TouchActivity)
class TouchActivity : BaseTitleActivity<ActivityTouchBinding>(), OnRefreshLoadMoreListener {
    private val data by lazy { ArrayList<TestBean>() }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        titleRoot.setTitle("手势拖动") { close() }
        mBinding?.setVariable(BR.adapter, ItemAdapter())
    }

    override fun initEvent() {
        super.initEvent()
        setOnBackPressedListener {
            close()
        }
        mBinding?.xrvList?.setOnRefreshListener(this)
        // 拖拽移动和左滑删除
        val callBack = ItemTouchCallBack(mBinding?.adapter)
        // 要实现侧滑删除条目，把 false 改成 true 就可以了
        callBack.setmSwipeEnable(false)
        val helper = ItemTouchHelper(callBack)
        // 设置是否关闭刷新
        helper.setOnMoveListener { move ->
            if (move) {
                mBinding?.xrvList?.refresh.disable()
            } else {
                mBinding?.xrvList?.refresh.enable()
            }
        }
        helper.attachToRecyclerView(mBinding?.xrvList?.recycler)
    }

    override fun initData() {
        super.initData()
        data.clear()
        for (i in 0 until 10) {
            data.add(TestBean("用户id:${i}", "用户名:老王-${i}"))
        }
        mBinding?.adapter?.notify(data)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        launch {
            delay(500)
            mBinding?.xrvList?.refresh?.finishRefreshing(false)
        }
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        launch {
            delay(500)
            mBinding?.xrvList?.refresh?.finishRefreshing(false)
        }
    }

    private fun close() {
        val localData = mBinding?.adapter?.list()?.toArrayList()
        setResult(ResultCode.RESULT_FINISH, Intent().apply { putExtra(Extra.BUNDLE_LIST, localData) })
        finish()
    }

}