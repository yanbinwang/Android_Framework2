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
package com.yanzhenjie.durban

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator

/**
 * <p>Control panel configuration.</p>
 * Created by Yan Zhenjie on 2017/5/30.
 */
class Controller : Parcelable {
    var enable: Boolean = false
        private set
    var rotation: Boolean = false
        private set
    var rotationTitle: Boolean = false
        private set
    var scale: Boolean = false
        private set
    var scaleTitle: Boolean = false
        private set

    companion object CREATOR : Creator<Controller> {

        override fun createFromParcel(parcel: Parcel): Controller {
            return Controller(parcel)
        }

        override fun newArray(size: Int): Array<Controller?> {
            return arrayOfNulls(size)
        }

        /**
         * Create a Builder.
         */
        @JvmStatic
        fun newBuilder(): Builder {
            return Builder()
        }
    }

    private constructor(builder: Builder) {
        this.enable = builder.enable
        this.rotation = builder.rotation
        this.rotationTitle = builder.rotationTitle
        this.scale = builder.scale
        this.scaleTitle = builder.scaleTitle
    }

    constructor(parcel: Parcel) {
        this.enable = parcel.readByte().toInt() != 0
        this.rotation = parcel.readByte().toInt() != 0
        this.rotationTitle = parcel.readByte().toInt() != 0
        this.scale = parcel.readByte().toInt() != 0
        this.scaleTitle = parcel.readByte().toInt() != 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeByte((if (enable) 1 else 0).toByte())
        dest.writeByte((if (rotation) 1 else 0).toByte())
        dest.writeByte((if (rotationTitle) 1 else 0).toByte())
        dest.writeByte((if (scale) 1 else 0).toByte())
        dest.writeByte((if (scaleTitle) 1 else 0).toByte())
    }

    override fun describeContents(): Int {
        return 0
    }

    class Builder internal constructor() {
        var enable = true
            private set
        var rotation = true
            private set
        var rotationTitle = true
            private set
        var scale = true
            private set
        var scaleTitle = true
            private set

        fun enable(enable: Boolean): Builder {
            this.enable = enable
            return this
        }

        fun rotation(rotation: Boolean): Builder {
            this.rotation = rotation
            return this
        }

        fun rotationTitle(rotationTitle: Boolean): Builder {
            this.rotationTitle = rotationTitle
            return this
        }

        fun scale(scale: Boolean): Builder {
            this.scale = scale
            return this
        }

        fun scaleTitle(scaleTitle: Boolean): Builder {
            this.scaleTitle = scaleTitle
            return this
        }

        fun build(): Controller {
            return Controller(this)
        }
    }

}