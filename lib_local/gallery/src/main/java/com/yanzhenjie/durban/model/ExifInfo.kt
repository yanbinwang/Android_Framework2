/*
 * Copyright © Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yanzhenjie.durban.model

import com.example.framework.utils.function.value.orZero

/**
 * Update by Yan Zhenjie on 2017/5/23.
 */
data class ExifInfo(
    var mExifOrientation: Int? = null,
    var mExifDegrees: Int? = null,
    var mExifTranslation: Int? = null
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val exifInfo = other as? ExifInfo
        if (mExifOrientation != exifInfo?.mExifOrientation) return false
        if (mExifDegrees != exifInfo?.mExifDegrees) return false
        return mExifTranslation == exifInfo?.mExifTranslation
    }

    override fun hashCode(): Int {
        var result = mExifOrientation.orZero
        result = 31 * result + mExifDegrees.orZero
        result = 31 * result + mExifTranslation.orZero
        return result
    }

}