package com.example.mvvm.activity

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.OrientationHelper
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.base.page.Extra
import com.example.common.config.ARouterPath
import com.example.common.widget.textview.edittext.EditTextImpl
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.toNewList
import com.example.framework.utils.function.view.adapter
import com.example.framework.utils.function.view.getRecyclerView
import com.example.framework.utils.logWTF
import com.example.mvvm.adapter.VideoSnapPageAdapter
import com.example.mvvm.bean.VideoSnap
import com.example.mvvm.databinding.ActivityMainBinding
import com.example.mvvm.fragment.VideoSnapFragment
import com.example.mvvm.utils.VideoSnapImpl
import com.example.mvvm.utils.VideoSnapManager

/**
 * 首页
 * https://blog.csdn.net/hongxue8888/article/details/104109232/
 */
@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>(), EditTextImpl {
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        //所有页面数据集合(服务器下发)
        val dataList = ArrayList<VideoSnap>()
        //所有页面的集合
        val list = ArrayList<VideoSnapFragment>()
        for (i in 0 until 10) {
            val bean = VideoSnap(i.toString(), "选中的是：${i}")
            dataList.add(bean)
            val bundle = Bundle()
            bundle.putParcelable(Extra.BUNDLE_BEAN, bean)
            val fragment = VideoSnapFragment().apply { arguments = bundle }
            list.add(fragment)
        }
        //所有管理方法的集合(一定要写明注入的是接口)
        val managerList: ArrayList<VideoSnapImpl> = list.toNewList { it }
        //绑定适配器/添加管理器
        mBinding?.vpPage.adapter(VideoSnapPageAdapter(this).apply { refresh(list) })
        val videoSnapManager = VideoSnapManager(this, OrientationHelper.VERTICAL, false)
        videoSnapManager.setOnViewPagerListener(object : VideoSnapManager.OnViewPagerListener {
            override fun onPageRelease(isNest: Boolean, itemView: View, position: Int) {
                "onPageRelease：选中的下标：${position}".logWTF
//                val itemBinding = FragmentVideoSnapBinding.bind(itemView)
                managerList.safeGet(position)?.releaseVideo(dataList.safeGet(position))
//                releaseVideo(itemView)
            }

            override fun onPageSelected(isBottom: Boolean, itemView: View, position: Int) {
                "onPageSelected：选中的下标：${position}".logWTF
                managerList.safeGet(position)?.playVideo(dataList.safeGet(position))
//                playVideo(itemView)
            }
        })
        mBinding?.vpPage.getRecyclerView()?.layoutManager = videoSnapManager
//        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                //当前处于停止滑动
//                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
////                    if (list.safeSize()<2) return
//                    if (recyclerView.isTop()) {
//
//                        return
//                    }
//                    if (recyclerView.isBottom()) {
//
//                        return
//                    }
//                }
//                super.onScrollStateChanged(recyclerView, newState)
//            }
//        })
    }

}