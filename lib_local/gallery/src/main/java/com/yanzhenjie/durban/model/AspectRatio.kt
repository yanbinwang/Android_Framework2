package com.yanzhenjie.durban.model

import android.os.Parcel
import android.os.Parcelable

class AspectRatio : Parcelable {
    var mAspectRatioTitle: String? = null
        private set
    var mAspectRatioX: Float? = null
        private set
    var mAspectRatioY: Float? = null
        private set

    constructor(aspectRatioTitle: String?, aspectRatioX: Float?, aspectRatioY: Float?) {
        mAspectRatioTitle = aspectRatioTitle
        mAspectRatioX = aspectRatioX
        mAspectRatioY = aspectRatioY
    }

    constructor(parcel: Parcel) {
        mAspectRatioTitle = parcel.readString()
        mAspectRatioX = parcel.readValue(Float::class.java.classLoader) as? Float
        mAspectRatioY = parcel.readValue(Float::class.java.classLoader) as? Float
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(mAspectRatioTitle)
        parcel.writeValue(mAspectRatioX)
        parcel.writeValue(mAspectRatioY)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AspectRatio> {
        override fun createFromParcel(parcel: Parcel): AspectRatio {
            return AspectRatio(parcel)
        }

        override fun newArray(size: Int): Array<AspectRatio?> {
            return arrayOfNulls(size)
        }
    }

}