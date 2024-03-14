package com.example.common.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.example.common.R
import com.example.common.databinding.ViewEmptyBinding
import com.example.common.utils.NetWorkUtil.isNetworkAvailable
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.view.appear
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.color
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.layoutParamsMatch
import com.example.framework.utils.function.view.setResource
import com.example.framework.utils.function.view.string
import com.example.framework.utils.function.view.visible
import com.example.framework.widget.BaseViewGroup

/**
 * Created by android on 2017/8/7.
 *
 * @author Wyb
 * <p>
 * 数据为空时候显示的页面（适用于列表，详情等）
 * 情况如下：
 * <p>
 * 1.加载中-无按钮
 * 2.空数据-无按钮
 * 3.加载错误(无网络，服务器错误)-有按钮
 */
@SuppressLint("InflateParams")
class EmptyLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr) {
    private val mBinding by lazy { ViewEmptyBinding.bind(context.inflate(R.layout.view_empty)) }
    private var listener: (() -> Unit)? = null

    init {
//        binding.root.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT) //设置LayoutParams
        mBinding.root.layoutParamsMatch()
        mBinding.root.setBackgroundColor(color(R.color.bgDefault))
        mBinding.tvRefresh.click {
            //进入加载中
            loading()
            listener?.invoke()
        }
        mBinding.root.click(null)
        loading()
    }

    override fun onInflateView() {
        if (isInflate()) addView(mBinding.root)
    }

    /**
     * 设置背景颜色
     */
    override fun setBackgroundColor(color: Int) {
        mBinding.root.setBackgroundColor(color)
    }

    /**
     * 设置列表所需的emptyview
     */
    fun setListView(listView: View?): View {
        removeView(mBinding.root)
        (listView?.parent as ViewGroup).addView(mBinding.root) //添加到当前的View hierarchy
        return mBinding.root
    }

    /**
     * 数据加载中
     */
    fun loading() {
        appear(300)
        mBinding.ivEmpty.setResource(R.mipmap.bg_data_loading)
        mBinding.tvEmpty.setI18nRes(R.string.dataLoading)
        mBinding.tvRefresh.gone()
    }

    /**
     * 数据为空--只会在200并且无数据的时候展示
     */
    fun empty(resId: Int = -1, resText: Int = -1) {
        appear(300)
        mBinding.ivEmpty.setResource(if (-1 == resId) R.mipmap.bg_data_empty else resId)
        mBinding.tvEmpty.setI18nRes(if (-1 == resText) R.string.dataEmpty else resText)
        mBinding.tvRefresh.gone()
    }

    /**
     * 数据加载失败-无网络，服务器请求
     * 无网络优先级最高
     */
    fun error(resId: Int = -1, resText: Int = -1, resRefreshText: Int = R.string.refresh) {
        appear(300)
        if (!isNetworkAvailable()) {
            mBinding.ivEmpty.setResource(R.mipmap.bg_data_net_error)
            mBinding.tvEmpty.setI18nRes(R.string.dataNetError)
        } else {
            mBinding.ivEmpty.setResource(if (-1 == resId) R.mipmap.bg_data_error else resId)
            mBinding.tvEmpty.setI18nRes(if (-1 == resText) R.string.dataError else resText)
        }
        mBinding.tvRefresh.setI18nRes(if (-1 == resRefreshText) R.string.refresh else resRefreshText)
        mBinding.tvRefresh.visible()
    }

    /**
     * 设置刷新监听
     */
    fun setOnEmptyRefreshListener(listener: (() -> Unit)) {
        this.listener = listener
    }

}