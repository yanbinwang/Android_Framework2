package com.example.mvvm.widget.automatic

import androidx.appcompat.app.AppCompatActivity

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
            0 -> AutomaticEdit(activity, bean)
            1 -> AutomaticPic(activity, bean)
            else -> null
        }
    }
}