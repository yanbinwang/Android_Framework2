package com.example.common.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.fragment.app.FragmentActivity
import com.example.common.R
import com.example.common.databinding.ViewEmptyBinding
import com.example.common.utils.NetWorkUtil.isNetworkAvailable
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.view.appear
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.color
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.invisible
import com.example.framework.utils.function.view.setResource
import com.example.framework.utils.function.view.size
import com.example.framework.utils.function.view.string
import com.example.framework.utils.function.view.tint
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
 * 2.空数据-无按钮(特殊情况可显示按钮，回调跳转时可做配置)
 * 3.加载错误(无网络，服务器错误)-有按钮
 */
@SuppressLint("InflateParams")
class EmptyLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr) {
    private val mBinding by lazy { ViewEmptyBinding.bind(context.inflate(R.layout.view_empty)) }
    private var state = -1
    private var fullScreen = false
    private var listener: ((result: Boolean) -> Unit)? = null

    init {
//        mBinding.root.layoutParamsMatch()
        //是否是全屏
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.EmptyLayout)
        fullScreen = typedArray.getBoolean(R.styleable.EmptyLayout_elEnableFullScreen, false)
        typedArray.recycle()
        //绘制大小撑到最大/默认背景
        mBinding.root.size(MATCH_PARENT, MATCH_PARENT)
        mBinding.root.setBackgroundColor(color(R.color.bgDefault))
        //点击事件/默认状态
        mBinding.tvRefresh.click {
            if (!isEmpty()) loading()
            listener?.invoke(isEmpty())
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
     * 特殊用法，部分页面需要全屏的empty，带一个返回按钮
     * 1.只需关闭页面直接调setBack（this）
     * 2.返回按钮点击后做别的操作setBack（onClick = {}）->不需要传activity
     */
    fun setBack(mActivity: FragmentActivity? = null, resId: Int = R.mipmap.ic_btn_back, tintColor: Int = 0, width: Int? = null, height: Int? = null, onClick: () -> Unit = { mActivity?.finish() }) {
        mBinding.ivLeft.apply {
            setResource(resId)
            if (0 != tintColor) tint(tintColor)
            if (null != width && null != height) size(width, height)
            click { onClick.invoke() }
        }
    }

    /**
     * 数据加载中
     */
    fun loading() {
        appear(300)
        state = 0
        if (fullScreen) mBinding.ivLeft.visible() else mBinding.ivLeft.invisible()
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
        if (fullScreen) mBinding.ivLeft.visible() else mBinding.ivLeft.invisible()
        if (null != width && null != height) mBinding.ivEmpty.size(width, height)
        mBinding.ivEmpty.setResource(resId ?: R.mipmap.bg_data_empty)
        mBinding.tvEmpty.text = if (text.isNullOrEmpty()) string(R.string.dataEmpty) else text
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
        if (fullScreen) mBinding.ivLeft.visible() else mBinding.ivLeft.invisible()
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
     * 获取当前状态
     */
    fun isLoading(): Boolean {
        return state == 0
    }

    fun isEmpty(): Boolean {
        return state == 1
    }

    fun isError(): Boolean {
        return state == 2
    }

    /**
     * 设置刷新监听
     */
    fun setOnEmptyRefreshListener(listener: ((result: Boolean) -> Unit)) {
        this.listener = listener
    }

}