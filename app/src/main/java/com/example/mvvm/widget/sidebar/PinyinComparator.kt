package com.example.mvvm.widget.sidebar

import com.example.framework.utils.function.value.orZero
import com.example.mvvm.widget.sidebar.bean.SortBean

class PinyinComparator : Comparator<SortBean> {

    override fun compare(o1: SortBean?, o2: SortBean?): Int {
        return if (o1?.sortLetters.equals("@") || o2?.sortLetters.equals("#")) {
            -1
        } else if (o1?.sortLetters.equals("#") || o2?.sortLetters.equals("@")) {
            1
        } else {
            o1?.sortLetters?.compareTo(o2?.sortLetters.orEmpty()).orZero
        }
    }

}