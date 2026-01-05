package com.example.common.base.binding.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by WangYanBin on 2020/7/17.
 * 基础复用的ViewHolder，传入对应的ViewBinding拿取布局Binding
 */
@Suppress("UNCHECKED_CAST")
open class BaseViewDataBindingHolder(parent: ViewGroup, private val binding: ViewDataBinding?) : RecyclerView.ViewHolder(binding?.root ?: View(parent.context)) {

    companion object {
        /**
         * 构建带具体Binding子类的Holder（兼容XML布局）
         */
        @JvmStatic
        fun <VDB : ViewDataBinding> onCreateViewBindingHolder(parent: ViewGroup, aClass: Class<VDB>?): BaseViewDataBindingHolder {
            var binding: VDB? = null
            try {
                val method = aClass?.getDeclaredMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.javaPrimitiveType)
                binding = method?.invoke(null, LayoutInflater.from(parent.context), parent, false) as? VDB
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return BaseViewDataBindingHolder(parent, binding)
        }

        /**
         * 无XML布局：构建仅持有根视图（LinearLayout）的Holder，用于代码动态添加视图
         * 仅提供根容器，后续手动往根视图中添加EmptyLayout等控件
         * 获取Binding的根视图（LinearLayout，动态添加视图的容器）
         *   val rootContainer = holder.viewBinding<ViewDataBinding>()?.root as LinearLayout
         *   // 清空根容器中的原有视图，避免重复添加
         *   rootContainer.removeAllViews()
         *   // 执行控件添加
         *   rootContainer.addView(xxx)
         *  private class FundsNormalEmptyViewHolder(parent: ViewGroup, binding: ViewDataBinding?) : BaseViewDataBindingHolder(parent, binding)
         *  TYPE_EMPTY -> FundsNormalEmptyViewHolder(parent, onCreateViewBindingHolder(parent).viewBinding()).viewBinding()
         *  holder.viewBinding<ViewDataBinding>()?.apply {}
         */
        @JvmStatic
        fun onCreateViewBindingHolder(parent: ViewGroup): BaseViewDataBindingHolder {
            // 构建根布局（LinearLayout，作为动态添加视图的容器）
            val rootContainer = LinearLayout(parent.context).apply {
                // 设置RecyclerView Item的标准布局参数，避免宽度/高度异常
                layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                // 垂直方向（可根据需求改为HORIZONTAL）
                orientation = LinearLayout.VERTICAL
            }
            // 绑定根视图到ViewDataBinding（仅持有根容器，无控件映射，符合你的需求）
            val binding = DataBindingUtil.bind<ViewDataBinding>(rootContainer)
            // 返回Holder，持有该Binding（仅用于获取根视图rootContainer）
            return BaseViewDataBindingHolder(parent, binding)
        }
    }

    /**
     * 获取binding
     */
    fun <VDB : ViewDataBinding> viewBinding(): VDB? {
        return binding as? VDB
    }

    /**
     * 解绑
     */
    fun unbind() {
        binding?.unbind()
    }

}