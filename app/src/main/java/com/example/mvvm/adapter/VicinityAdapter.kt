package com.example.mvvm.adapter

import android.util.SparseArray
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.common.base.binding.adapter.BaseAdapter
import com.example.common.base.binding.adapter.BaseViewDataBindingHolder
import com.example.common.utils.function.pt
import com.example.common.widget.xrecyclerview.manager.SCommonItemDecoration
import com.example.common.widget.xrecyclerview.manager.SCommonItemDecoration.ItemDecorationProps
import com.example.framework.utils.function.inflate
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.safeSize
import com.example.framework.utils.function.value.safeSubList
import com.example.framework.utils.function.value.toArrayList
import com.example.framework.utils.function.view.cancelItemAnimator
import com.example.framework.utils.function.view.click
import com.example.mvvm.R
import com.example.mvvm.bean.TestBean
import com.example.mvvm.databinding.ItemVicinityBodyBinding
import com.example.mvvm.databinding.ItemVicinityHeaderBinding

class VicinityAdapter : BaseAdapter<TestBean?>() {
    private var listener: ((t: TestBean?) -> Unit)? = null
    private val list by lazy { ArrayList<TestBean>() }

    companion object {
        private const val TYPE_HEADER = 1
        private const val TYPE_BODY = 2
    }

    override fun onConvert(holder: BaseViewDataBindingHolder, item: TestBean?, payloads: MutableList<Any>?) {
        if (holder is CurrencyHeaderViewHolder) {
            holder.viewBinding<ItemVicinityHeaderBinding>()?.apply {
                val first = list.safeGet(0)
                val second = list.safeGet(1)
                tvTips.click {
                    listener?.invoke(first)
                }
                tvTips2.click {
                    listener?.invoke(second)
                }
            }
        } else {
            holder.viewBinding<ItemVicinityBodyBinding>()?.apply {
                val absolutePosition = holder.absoluteAdapterPosition - 1
                val bean = item(absolutePosition)
                tvRoot.click {
                    listener?.invoke(bean)
                }
//                setVariable(BR.bean, bean)
//                flRoot.click { listener?.onBody(bean?.currency.orEmpty()) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewDataBindingHolder {
        var binding: ViewDataBinding? = null
        when (viewType) {
            TYPE_HEADER -> binding = CurrencyHeaderViewHolder(parent, ItemVicinityHeaderBinding.bind(parent.context.inflate(R.layout.item_vicinity_header))).viewBinding()
            TYPE_BODY -> binding = CurrencyBodyViewHolder(parent, ItemVicinityBodyBinding.bind(parent.context.inflate(R.layout.item_vicinity_body))).viewBinding()
        }
        return BaseViewDataBindingHolder(parent, binding)
    }

    /**
     * 头部
     */
    private class CurrencyHeaderViewHolder(parent: ViewGroup, binding: ItemVicinityHeaderBinding?) : BaseViewDataBindingHolder(parent, binding)

    /**
     * 内容
     */
    private class CurrencyBodyViewHolder(parent: ViewGroup, binding: ItemVicinityBodyBinding?) : BaseViewDataBindingHolder(parent, binding)

    /**
     * 1和2选项合并为1
     */
    override fun getItemCount(): Int {
        return super.getItemCount() + 1
    }

    /**
     * 合并后的0代表集合中的1和2
     */
    override fun getItemViewType(position: Int): Int {
        return if (0 == position) {
            TYPE_HEADER
        } else {
            TYPE_BODY
        }
    }

    /**
     * 配置默认参数
     * setRefresh之前调取
     */
    fun init(recycler: RecyclerView) {
        //视图适配
        recycler.setHasFixedSize(true)
        recycler.cancelItemAnimator()
        recycler.adapter = this
        recycler.layoutManager = GridLayoutManager(recycler.context, 3).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (getItemViewType(position)) {
                        TYPE_HEADER -> 3
                        TYPE_BODY -> 3
                        else -> 1
                    }
                }
            }
        }
        //底部item间距适配
        val propMap = SparseArray<ItemDecorationProps>()
        val prop1 = ItemDecorationProps(10.pt, 10.pt, true, true)
        propMap.put(TYPE_BODY, prop1)
        recycler.addItemDecoration(SCommonItemDecoration(propMap))
    }

    /**
     * 数据刷新
     * 将集合的0,1下标单独抠出，作为头部数据，余下的作为底部数据
     */
    fun notify(mList: List<TestBean>?) {
        list.clear()
        list.addAll(mList.safeSubList(0, 2).toArrayList())
        refresh(mList.safeSubList(2, mList.safeSize).toArrayList())
    }

    /**
     * 回调监听
     */
    fun setOnItemClickListener(listener: ((t: TestBean?) -> Unit)) {
        this.listener = listener
    }

}