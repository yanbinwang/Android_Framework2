package com.example.mvvm.widget.automatic.holder

import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.example.framework.utils.function.inflate
import com.example.mvvm.R
import com.example.mvvm.widget.automatic.AutomaticBean
import kotlin.LazyThreadSafetyMode.NONE

/**
 * @description
 * @author
 */
class EditHolder(context: Context, private val bean: AutomaticBean) : AutomaticInterface {
    private val rootView by lazy(NONE) { context.inflate(R.layout.view_edit) }
    private val editText by lazy(NONE) { rootView.findViewById<EditText>(R.id.et_content) }

    init {
        val textLabel = rootView.findViewById<TextView>(R.id.tv_label)
        textLabel.text = bean.label
    }

    override fun getBean(): AutomaticBean {
        return bean
    }

    override fun getResult(): Pair<String, String> {
        return bean.key to editText.text.toString()
    }

    override fun getView(): View {
        return rootView
    }

}