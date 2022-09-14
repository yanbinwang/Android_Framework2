package com.example.common.widget

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import com.example.base.utils.function.string
import com.example.base.utils.function.view.color
import com.example.base.widget.BaseViewGroup
import com.example.common.R
import com.example.common.databinding.ViewEmptyBinding
import com.example.common.utils.NetWorkUtil.isNetworkAvailable

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
    private var binding: ViewEmptyBinding? = null
    var onRefreshClick: (() -> Unit)? = null

    init {
        binding = DataBindingUtil.bind(LayoutInflater.from(getContext()).inflate(R.layout.view_empty, null))
        binding?.llContainer?.setBackgroundColor(color(R.color.grey_f6f8ff))
        //设置样式
        binding?.root?.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT) //设置LayoutParams
        binding?.root?.setBackgroundColor(color(R.color.grey_f6f8ff))
        //设置监听
        binding?.tvRefresh?.setOnClickListener {
            //进入加载中，并停止刷新动画
            showLoading()
            onRefreshClick?.invoke()
        }
        binding?.root?.setOnClickListener(null)
        showLoading()
    }

    override fun onDrawView() {
        if (onFinishView()) addView(binding?.root)
    }

    /**
     * 设置列表所需的emptyview
     */
    fun setListView(listView: View): View? {
        removeView(binding?.root)
        (listView.parent as ViewGroup).addView(binding?.root) //添加到当前的View hierarchy
        return binding?.root
    }

    /**
     * 数据加载中
     */
    fun showLoading() {
        visibility = VISIBLE
        binding?.ivEmpty?.setImageResource(R.mipmap.img_data_loading)
        binding?.tvEmpty?.text = context.string(R.string.label_data_loading)
        binding?.tvRefresh?.visibility = GONE
//        visibility = VISIBLE
//        binding?.ivEmpty?.visibility = GONE
//        binding?.tvEmpty?.text = context.getString(R.string.label_data_loading)
//        binding?.tvRefresh?.visibility = GONE
    }

    /**
     * 数据为空--只会在200并且无数据的时候展示
     */
    fun showEmpty(resId: Int = -1, text: String? = null) {
        visibility = VISIBLE
        binding?.ivEmpty?.setImageResource(if (-1 == resId) R.mipmap.img_data_empty else resId)
        binding?.tvEmpty?.text = if (TextUtils.isEmpty(text)) context.getString(R.string.label_data_empty) else text
        binding?.tvRefresh?.visibility = GONE
    }

    /**
     * 数据加载失败-无网络，服务器请求
     * 无网络优先级最高
     */
    fun showError(resId: Int = -1, text: String? = null) {
        visibility = VISIBLE
        if (!isNetworkAvailable()) {
            binding?.ivEmpty?.setImageResource(R.mipmap.img_data_net_error)
            binding?.tvEmpty?.text = context.getString(R.string.label_data_net_error)
        } else {
            binding?.ivEmpty?.setImageResource(if (-1 == resId) R.mipmap.img_data_error else resId)
            binding?.tvEmpty?.text =
                if (TextUtils.isEmpty(text)) context.getString(R.string.label_data_error) else text
        }
        binding?.tvRefresh?.visibility = VISIBLE
    }

    /**
     * 设置背景颜色
     */
    override fun setBackgroundColor(color: Int) {
        binding?.llContainer?.setBackgroundColor(color)
    }

}