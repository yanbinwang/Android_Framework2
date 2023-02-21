package com.example.mvvm.widget.automatic.holder

import android.view.View
import com.example.mvvm.widget.automatic.AutomaticBean

interface AutomaticInterface {
    fun getBean(): AutomaticBean
    fun getResult(): Pair<String, String>
    fun getView(): View
}