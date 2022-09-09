package com.example.mvvm.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by WangYanBin on 2020/6/3.
 */
@Parcelize
class UserInfoModel(
    var name: String? = null,
    var age: Int,
    var avatar: String? = null
) : Parcelable