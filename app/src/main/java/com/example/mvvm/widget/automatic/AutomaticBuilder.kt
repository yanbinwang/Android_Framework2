package com.example.mvvm.widget.automatic

import androidx.appcompat.app.AppCompatActivity
import com.example.mvvm.widget.automatic.holder.AutomaticInterface
import com.example.mvvm.widget.automatic.holder.NormalEditHolder
import com.example.mvvm.widget.automatic.holder.NormalPicHolder

/**
 * viewList = list.toNewList { AutomaticBuilder.builder(it.getAutomaticBean(type)).build(this) }
 */
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
            0 -> NormalEditHolder(activity, bean)
            1 -> NormalPicHolder(activity, bean)
            else -> null
        }
    }
}