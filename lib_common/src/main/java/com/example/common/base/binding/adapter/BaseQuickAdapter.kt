package com.example.common.base.binding.adapter

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.example.common.base.binding.adapter.BaseViewDataBindingHolder.Companion.onCreateViewBindingHolder
import java.lang.reflect.ParameterizedType

/**
 * Created by WangYanBin on 2020/7/17.
 * 快捷适配器，传入对应的ViewBinding即可
 *
 * 解绑后的 ViewBinding 仍可被重新绑定。RecyclerView 复用时会通过 onBindViewHolder() 重新设置数据，此时 DataBinding 会自动重新建立绑定关系
 * onCreateViewHolder：通过反射创建 ViewBinding
 * onBindViewHolder：设置点击事件并调用抽象方法 onConvert
 * 子类实现 onConvert，通常会更新 binding 的数据
 */
@Suppress("UNCHECKED_CAST")
abstract class BaseQuickAdapter<T, VDB : ViewDataBinding> : BaseAdapter<T> {
    protected var mBinding: VDB? = null

    constructor() : super()

    constructor(bean: T?) : super(bean)

    constructor(list: ArrayList<T>?) : super(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewDataBindingHolder {
        val vdbClass = try {
            (javaClass.genericSuperclass as? ParameterizedType)?.actualTypeArguments?.get(1) as? Class<*>
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //避免在适配器中持有 Context 引用，通过 holder.itemView.context 获取上下文
        return onCreateViewBindingHolder(parent, vdbClass as? Class<VDB>)
    }

    override fun onConvert(holder: BaseViewDataBindingHolder, item: T?, payloads: MutableList<Any>?) {
        mBinding = holder.viewBinding()
    }

    /**
     * RecyclerView 的缓存机制
     * RecyclerView 为了优化滚动性能，实现了一套复杂的视图缓存机制。当 ItemView 被移出屏幕时，RecyclerView 会根据情况将其放入不同级别的缓存中：
     * 1.Scrap 缓存（一级缓存）
     * 存放刚被移出屏幕、可能马上被复用的 ViewHolder。
     * 特点：ViewHolder 不会被重置，直接复用（不触发 onCreateViewHolder 和 onBindViewHolder）。
     * 触发条件：快速滚动、插入 / 删除操作导致的视图位置变动。
     * 2.RecycledViewPool（二级缓存）
     * 存放长期闲置的 ViewHolder，需要重置后才能复用。
     * 特点：ViewHolder 会被重置（触发 onViewRecycled），复用时需重新绑定数据（触发 onBindViewHolder）。
     * 触发条件：滚动出屏幕的 ViewHolder 超过 Scrap 缓存容量时。
     *
     * onViewRecycled() 仅在 ViewHolder 被放入 RecycledViewPool 时触发，核心目的是清理不再使用的资源, 具体场景包括：
     * 滚动操作：当大量 ItemView 被滚动出屏幕，Scrap 缓存满时，多余的 ViewHolder 会被放入 RecycledViewPool。
     * 手动调用：调用 RecyclerView.clearRecycledViewPool() 强制清空缓存池。
     * Adapter 变更：调用 notifyDataSetChanged() 或 swapAdapter() 时，部分 ViewHolder 可能被回收。
     */
    override fun onViewRecycled(holder: BaseViewDataBindingHolder) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    /**
     * 复用父类的绑定
     */
    protected fun setExecutePendingVariable(variableId: Int, value: Any?) {
        setExecutePendingVariable(mBinding, variableId, value)
    }

}