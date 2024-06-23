/*
 * Copyright © Yan Zhenjie. All Rights Reserved
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

import java.util.Locale

/**
 * <p>Durban config.</p>
 * Created by Yan Zhenjie on 2017/5/30.
 */
class DurbanConfig(build: Builder) {
    var mLocale: Locale? = null
        private set

    init {
        this.mLocale = build.mLocale
    }

    companion object {
        /**
         * Create a new builder.
         */
        @JvmStatic
        fun newBuilder(): Builder {
            return Builder()
        }
    }

    class Builder internal constructor() {
        var mLocale: Locale? = null
            private set

        /**
         * Set locale for language.
         *
         * @param locale [Locale].
         * @return [Builder].
         */
        fun setLocale(locale: Locale): Builder {
            this.mLocale = locale
            return this
        }

        /**
         * Create AlbumConfig.
         *
         * @return [DurbanConfig].
         */
        fun build(): DurbanConfig {
            return DurbanConfig(this)
        }
    }

}