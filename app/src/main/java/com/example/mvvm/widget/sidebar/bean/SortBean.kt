package com.example.mvvm.widget.sidebar.bean

data class SortBean(
    var name: String? = null,//显示的数据
    var sortLetters: String? = null,//显示数据拼音的首字母
    var beseId: String? = null //对象的id
)