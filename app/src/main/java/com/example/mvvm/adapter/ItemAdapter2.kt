package com.example.mvvm.adapter

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.common.base.binding.adapter.BaseQuickAdapter
import com.example.common.base.binding.adapter.BaseViewDataBindingHolder
import com.example.framework.utils.function.doOnDestroy
import com.example.mvvm.BR
import com.example.mvvm.bean.TestBean
import com.example.mvvm.databinding.ItemTestBinding
import com.tencent.mm.opensdk.openapi.IWXAPI
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

class ItemAdapter2(private val observer: LifecycleOwner) : BaseQuickAdapter<TestBean, ItemTestBinding>() {
    private val apiMap by lazy { ConcurrentHashMap<Int, Job>() }

    init {
        observer.doOnDestroy {
            apiMap.entries.forEach { (key, value) ->
                value.cancel()
                apiMap.remove(key)
            }
        }
    }

    override fun onConvert(holder: BaseViewDataBindingHolder, item: TestBean?, payloads: MutableList<Any>?) {
        super.onConvert(holder, item, payloads)
        val position = holder.absoluteAdapterPosition
        apiMap.remove(position)?.cancel()
        apiMap[position] = observer.lifecycleScope.launch(Main.immediate) {
            // 切子线程跑协程处理
        }
//        setExecutePendingVariable(BR.bean, item)
    }

    override fun onViewRecycled(holder: BaseViewDataBindingHolder) {
        super.onViewRecycled(holder)
        apiMap.remove(holder.absoluteAdapterPosition)?.cancel()
    }

}