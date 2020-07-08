package com.example.mvvm.model

import androidx.databinding.BaseObservable
import java.io.Serializable

/**
 * Created by WangYanBin on 2020/6/3.
 */
class UserInfoModel(
    var name: String? = null,
    var age: Int,
    var avatar: String? = null
) : BaseObservable(), Serializable 