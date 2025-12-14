package com.yanzhenjie.album;

/**
 * <p>Album config.</p>
 * Created by Yan Zhenjie on 2017/3/31.
 */
public class AlbumConfig {
    private AlbumLoader mLoader;

    private AlbumConfig(Builder builder) {
        this.mLoader = builder.mLoader == null ? AlbumLoader.DEFAULT : builder.mLoader;
    }

    /**
     * Create a new builder.
     */
    public static Builder newBuilder() {
        return new Builder();
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