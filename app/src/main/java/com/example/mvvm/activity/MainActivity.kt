package com.example.mvvm.activity

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.base.page.Extra
import com.example.common.config.ARouterPath
import com.example.common.widget.textview.edittext.EditTextImpl
import com.example.framework.utils.function.value.safeGet
import com.example.mvvm.bean.VideoSnap
import com.example.mvvm.databinding.ActivityMainBinding
import com.example.mvvm.fragment.VideoSnapFragment
import com.example.mvvm.utils.VideoSnapImpl
import com.example.mvvm.utils.VideoSnapManager

/**
 * 首页
 */
@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>(), EditTextImpl {
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
//        //所有页面数据集合
//        val dataList = ArrayList<VideoSnap>()
//        for (i in 0 until 10) {
//            dataList.add(VideoSnap(i.toString(), "-----"))
//        }
//        //所有页面的集合
//        val list = ArrayList<VideoSnapFragment>()
//        //所有管理方法的集合
//        val managerList = ArrayList<VideoSnapImpl>()
//        for (bean in dataList) {
//            val bundle = Bundle()
//            bundle.putParcelable(Extra.BUNDLE_BEAN, bean)
//            val fragment = VideoSnapFragment().apply { arguments = bundle }
//            list.add(fragment)
//            managerList.add(fragment)
//        }
        //添加管理器
        val recycler = RecyclerView(this)
        val videoSnapManager = VideoSnapManager(this, OrientationHelper.VERTICAL, false)
        recycler.layoutManager = videoSnapManager
//        recycler.adapter
        videoSnapManager.setOnViewPagerListener(object : VideoSnapManager.OnViewPagerListener {
            override fun onPageRelease(isNest: Boolean, itemView: View) {
//                releaseVideo(position)
            }

            override fun onPageSelected(isBottom: Boolean, itemView: View) {
//                playVideo(position)
            }
        })
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