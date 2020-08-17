package com.example.mvvm.bridge.event

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.example.mvvm.R
import com.example.mvvm.activity.TestListActivity
import com.example.mvvm.bridge.TestListViewModel

/**
 * Created by WangYanBin on 2020/8/17.
 */
class TestListEvent : TestListActivity() {

    var itemChildClickListener = object : OnItemChildClickListener {

        override fun onItemChildClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
            when (view.id) {
                R.id.iv_img -> showToast("图片点击：$position")
                R.id.tv_title -> showToast("标题点击：$position")
            }
        }

    }

    var itemClickListener = object : OnItemClickListener {

        override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
            showToast("整体点击：$position")
        }
    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.btn_test -> viewModel.getListData()
        }
    }

}