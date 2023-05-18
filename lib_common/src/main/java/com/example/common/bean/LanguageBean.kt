package com.example.common.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @description
 * @author
 */
@Parcelize
class LanguageBean(
    var version: String? = null,
    var data: HashMap<String, String?>? = HashMap()
) : Parcelable