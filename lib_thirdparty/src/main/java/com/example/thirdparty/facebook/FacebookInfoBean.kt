package com.example.thirdparty.facebook

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FacebookInfoBean(
    var id: String? = null,
    var name: String? = null,
    var picture: PictureBean? = null,
    var first_name: String? = null,
    var last_name: String? = null,
    var email: String? = null,
    var gender: String? = null,
    var locale: String? = null,
    var facebookToken: String? = null//用户token
) : Parcelable {
    val avatar = picture?.data?.url
}

@Parcelize
data class PictureBean(
    var data: DataBean? = null,
) : Parcelable

@Parcelize
data class DataBean(
    var height: Int? = null,
    var is_silhouette: Boolean? = null,
    var url: String? = null,
    var width: Int? = null,
) : Parcelable