package com.example.common.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.concurrent.ConcurrentHashMap

/**
 * 本地語言對象
 */
@Parcelize
class LanguageBean(
    var version: String? = null,
    var data: ConcurrentHashMap<String, String?>? = ConcurrentHashMap()
) : Parcelable

/**
 * 服務器列表語言
 */
@Parcelize
data class ServerLanguage(
    var id: Int? = null,
    var language: String? = null,
    var name: String? = null,
    var path: String? = null,
    var version: String? = null,
): Parcelable