package com.yanzhenjie.album

import android.os.Parcelable
import androidx.annotation.IntDef
import com.example.framework.utils.function.value.orZero
import kotlinx.parcelize.Parcelize

/**
 * Created by YanZhenjie on 2017/8/15.
 */
@Parcelize
data class AlbumFile(
    /** File path */
    var mPath: String? = null,
    /** Folder name */
    var mBucketName: String? = null,
    /** File mime type */
    var mMimeType: String? = null,
    /** Add date */
    var mAddDate: Long? = null,
    /** Latitude */
    var mLatitude: Float? = null,
    /** Longitude */
    var mLongitude: Float? = null,
    /** Size */
    var mSize: Long? = null,
    /** Duration */
    var mDuration: Long? = null,
    /** Thumb path */
    var mThumbPath: String? = null,
    /** Media type */
    var mMediaType: Int? = null,
    /** Checked status */
    var isChecked: Boolean? = null,
    /** Enabled status */
    var isDisable: Boolean? = null
) : Parcelable, Comparable<AlbumFile> {

    companion object {
        const val TYPE_IMAGE: Int = 1
        const val TYPE_VIDEO: Int = 2

        @Retention(AnnotationRetention.SOURCE)
        @IntDef(TYPE_IMAGE, TYPE_VIDEO)
        annotation class MediaType
    }

    override fun compareTo(other: AlbumFile): Int {
        val time = other.mAddDate.orZero - mAddDate.orZero
        if (time > Integer.MAX_VALUE) return Integer.MAX_VALUE
        else if (time < -Integer.MAX_VALUE) return -Integer.MAX_VALUE
        return time.toInt()
    }

    override fun equals(other: Any?): Boolean {
        if (other is AlbumFile) {
            val inPath = other.mPath
            if (mPath != null && inPath != null) {
                return mPath == inPath
            }
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return mPath?.hashCode() ?: super.hashCode()
    }

}