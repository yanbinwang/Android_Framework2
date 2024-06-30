/*
 * Copyright 2017 Yan Zhenjie.
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
package com.yanzhenjie.album;

/**
 * <p>Album config.</p>
 * Created by Yan Zhenjie on 2017/3/31.
 */
public class AlbumConfig {
    private AlbumLoader mLoader;

    /**
     * Create a new builder.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    private AlbumConfig(Builder builder) {
        this.mLoader = builder.mLoader == null ? AlbumLoader.DEFAULT : builder.mLoader;
    }

    /**
     * Get {@link AlbumLoader}.
     *
     * @return {@link AlbumLoader}.
     */
    public AlbumLoader getAlbumLoader() {
        return mLoader;
    }

    public static final class Builder {
        private AlbumLoader mLoader;

        private Builder() {
        }

        /**
         * Set album loader.
         *
         * @param loader {@link AlbumLoader}.
         * @return {@link Builder}.
         */
        public Builder setAlbumLoader(AlbumLoader loader) {
            this.mLoader = loader;
            return this;
        }

        /**
         * Create AlbumConfig.
         *
         * @return {@link AlbumConfig}.
         */
        public AlbumConfig build() {
            return new AlbumConfig(this);
        }

    }

}