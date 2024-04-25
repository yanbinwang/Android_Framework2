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
import com.example.framework.utils.function.view.size
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
    private var state = -1
    private var listener: (() -> Unit)? = null

    init {
        mBinding.root.layoutParamsMatch()
        mBinding.root.setBackgroundColor(color(R.color.bgDefault))
        mBinding.tvRefresh.click {
            //进入加载中
            if (state != 1) loading()
            listener?.invoke()
        }
        mBinding.root.click(null)
        loading()
    }

    override fun onInflate() {
        if (isInflate) addView(mBinding.root)
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
        (listView?.parent as? ViewGroup)?.addView(mBinding.root) //添加到当前的View hierarchy
        return mBinding.root
    }

    /**
     * 数据加载中
     */
    fun loading() {
        appear(300)
        state = 0
        mBinding.ivEmpty.setResource(R.mipmap.bg_data_loading)
        mBinding.tvEmpty.text = string(R.string.dataLoading)
        mBinding.tvRefresh.gone()
    }

    /**
     * 数据为空--只会在200并且无数据的时候展示
     */
    fun empty(resId: Int? = null, text: String? = null, refreshText: String? = null, width: Int? = null, height: Int? = null) {
        appear(300)
        state = 1
        if (null != width && null != height) mBinding.ivEmpty.size(width, height)
        mBinding.ivEmpty.setResource(resId ?: R.mipmap.bg_data_empty)
        mBinding.tvEmpty.text = if (text.isNullOrEmpty()) string(R.string.dataEmpty) else text
//        mBinding.tvRefresh.gone()
        if (!refreshText.isNullOrEmpty()) {
            mBinding.tvRefresh.visible()
            mBinding.tvRefresh.text = refreshText
        } else {
            mBinding.tvRefresh.gone()
        }
    }

    /**
     * 数据加载失败-无网络，服务器请求
     * 无网络优先级最高
     */
    fun error(resId: Int? = null, text: String? = null, refreshText: String? = null, width: Int? = null, height: Int? = null) {
        appear(300)
        state = 2
        if (null != width && null != height) mBinding.ivEmpty.size(width, height)
        if (!isNetworkAvailable()) {
            mBinding.ivEmpty.setResource(R.mipmap.bg_data_net_error)
            mBinding.tvEmpty.text = string(R.string.dataNetError)
        } else {
            mBinding.ivEmpty.setResource(resId ?: R.mipmap.bg_data_error)
            mBinding.tvEmpty.text = if (text.isNullOrEmpty()) string(R.string.dataError) else text
        }
        if (!refreshText.isNullOrEmpty()) mBinding.tvRefresh.text = refreshText
        mBinding.tvRefresh.visible()
    }

    /**
     * 设置刷新监听
     */
    fun setOnEmptyRefreshListener(listener: (() -> Unit)) {
        this.listener = listener
    }

    /**
     * 获取状态
     * 0->加载中
     * 1->数据为空
     * 2->数据错误
     */
    fun getState(): Int {
        return state
    }

}