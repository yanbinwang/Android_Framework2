package com.example.common.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams.MATCH_PARENT
import com.example.common.R
import com.example.common.databinding.ViewEmptyBinding
import com.example.common.utils.NetWorkUtil.isNetworkAvailable
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.view.*
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
    private val binding by lazy { ViewEmptyBinding.bind(context.inflate(R.layout.view_empty)) }
    private var onRefresh: (() -> Unit)? = null

    init {
//        binding.root.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT) //设置LayoutParams
        binding.root.layoutParamsMatch()
        binding.root.setBackgroundColor(color(R.color.appWindowBackground))
        binding.tvRefresh.click {
            //进入加载中
            loading()
            onRefresh?.invoke()
        }
        binding.root.click(null)
        loading()
    }

    override fun onInflateView() {
        if (isInflate()) addView(binding.root)
    }

    /**
     * 设置列表所需的emptyview
     */
    fun setListView(listView: View?): View {
        removeView(binding.root)
        (listView?.parent as ViewGroup).addView(binding.root) //添加到当前的View hierarchy
        return binding.root
    }

    /**
     * 数据加载中
     */
    fun loading() {
        appear(300)
        binding.ivEmpty.setResource(R.mipmap.bg_data_loading)
        binding.tvEmpty.text = string(R.string.data_loading)
        binding.tvRefresh.gone()
    }

    /**
     * 数据为空--只会在200并且无数据的时候展示
     */
    fun empty(resId: Int = -1, text: String? = null) {
        appear(300)
        binding.ivEmpty.setResource(if (-1 == resId) R.mipmap.bg_data_empty else resId)
        binding.tvEmpty.text = if (text.isNullOrEmpty()) string(R.string.data_empty) else text
        binding.tvRefresh.gone()
    }

    /**
     * 数据加载失败-无网络，服务器请求
     * 无网络优先级最高
     */
    fun error(resId: Int = -1, text: String? = null, refreshText: String? = null) {
        appear(300)
        if (!isNetworkAvailable()) {
            binding.ivEmpty.setResource(R.mipmap.bg_data_net_error)
            binding.tvEmpty.text = string(R.string.data_net_error)
        } else {
            binding.ivEmpty.setResource(if (-1 == resId) R.mipmap.bg_data_error else resId)
            binding.tvEmpty.text = if (text.isNullOrEmpty()) string(R.string.data_error) else text
        }
        if (!refreshText.isNullOrEmpty()) binding.tvRefresh.text = refreshText
        binding.tvRefresh.visible()
    }

    /**
     * 设置刷新监听
     */
    fun setEmptyRefreshListener(onRefresh: (() -> Unit)) {
        this.onRefresh = onRefresh
    }

    /**
     * 设置背景颜色
     */
    override fun setBackgroundColor(color: Int) {
        binding.root.setBackgroundColor(color)
    }

}