package com.example.mvvm.widget.automatic

import androidx.appcompat.app.AppCompatActivity
import com.example.mvvm.widget.automatic.holder.AutomaticInterface
import com.example.mvvm.widget.automatic.holder.EditHolder
import com.example.mvvm.widget.automatic.holder.PicHolder

class AutomaticBuilder private constructor() {
    private var bean: AutomaticBean? = null

    companion object {
        fun builder(bean: AutomaticBean): AutomaticBuilder {
            return AutomaticBuilder().also { it.bean = bean }
        }
    }

    fun build(activity: AppCompatActivity): AutomaticInterface? {
        val bean = bean ?: return null
        return when (bean.type) {
            0 -> EditHolder(activity, bean)
            1 -> PicHolder(activity, bean)
            else -> null
        }
    }
}