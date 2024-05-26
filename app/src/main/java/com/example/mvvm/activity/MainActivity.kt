package com.example.mvvm.activity

import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.base.page.Extra
import com.example.common.config.ARouterPath
import com.example.common.widget.textview.edittext.EditTextImpl
import com.example.common.widget.xrecyclerview.refresh.finishRefreshing
import com.example.common.widget.xrecyclerview.refresh.init
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.toNewList
import com.example.framework.utils.function.view.adapter
import com.example.mvvm.adapter.VideoSnapPageAdapter
import com.example.mvvm.bean.VideoSnapBean
import com.example.mvvm.databinding.ActivityMainBinding
import com.example.mvvm.fragment.VideoSnapFragment
import com.example.mvvm.utils.VideoSnapImpl
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener


/**
 * 首页
 * https://blog.csdn.net/hongxue8888/article/details/104109232/
 */
@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>(), OnRefreshLoadMoreListener {
    //所有页面数据集合(服务器下发)
    private val dataList = ArrayList<VideoSnapBean>()
    //所有管理方法的集合(一定要写明注入的是接口)
    private var implList = ArrayList<VideoSnapImpl>()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        //所有页面的集合
        val list = ArrayList<VideoSnapFragment>()
        for (i in 0 until 10) {
            val bean = VideoSnapBean(i.toString(), "选中的是：${i}")
            dataList.add(bean)
            val bundle = Bundle()
            bundle.putParcelable(Extra.BUNDLE_BEAN, bean)
            val fragment = VideoSnapFragment().apply { arguments = bundle }
            list.add(fragment)
        }
        //所有管理方法的集合(一定要写明注入的是接口)
        implList = list.toNewList { it }
        //绑定适配器/添加监听
        mBinding?.vpPage.adapter(VideoSnapPageAdapter(this).apply { refresh(list) }, ViewPager2.ORIENTATION_VERTICAL, true)
        mBinding?.vpPage?.registerOnPageChangeCallback(listener)
    }

    override fun initEvent() {
        super.initEvent()
        mBinding?.refresh.init(this)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        mBinding?.refresh.finishRefreshing()
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        mBinding?.refresh.finishRefreshing()
    }

    override fun onDestroy() {
        mBinding?.vpPage?.unregisterOnPageChangeCallback(listener)
        super.onDestroy()
    }

    private val listener = object : ViewPager2.OnPageChangeCallback() {
        private var previousPosition = 0

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            //当页面被选中时，position是当前页面的下标/使用previousPosition记录前一个滑动的下标
            val prePosition = previousPosition
            previousPosition = position
            //关闭前一个
            implList.safeGet(prePosition)?.releaseVideo(dataList.safeGet(prePosition))
            //播放当前的
            implList.safeGet(previousPosition)?.playVideo(dataList.safeGet(previousPosition))
        }
    }

}