package com.example.mvvm.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 滑动视频的数据bean
 */
@Parcelize
data class VideoSnapBean(
    var id: String? = null,
    var videoUrl: String? = null,
) : Parcelable