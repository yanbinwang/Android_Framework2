package com.example.mvvm.widget.automatic

import android.view.View

interface AutomaticInterface {
    fun getBean(): AutomaticBean
    fun getResult(): Pair<String, String>
    fun getView(): View
}