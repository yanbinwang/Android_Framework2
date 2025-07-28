package com.yanzhenjie.album;

import android.content.Context;

import java.util.Locale;

/**
 * <p>Album config.</p>
 * Created by Yan Zhenjie on 2017/3/31.
 */
public class AlbumConfig {
    private AlbumLoader mLoader;
    private Locale mLocale;

    private AlbumConfig(Builder builder) {
        this.mLoader = builder.mLoader == null ? AlbumLoader.DEFAULT : builder.mLoader;
        this.mLocale = builder.mLocale == null ? Locale.getDefault() : builder.mLocale;
    }

    /**
     * Create a new builder.
     */
    public static Builder newBuilder(Context context) {
        return new Builder(context);
    }

    /**
     * Get {@link AlbumLoader}.
     *
     * @return {@link AlbumLoader}.
     */
    public AlbumLoader getAlbumLoader() {
        return mLoader;
    }

    /**
     * Get {@link Locale}.
     *
     * @return {@link Locale}.
     */
    public Locale getLocale() {
        return mLocale;
    }

    public static final class Builder {

        private AlbumLoader mLoader;
        private Locale mLocale;

        private Builder(Context context) {
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
         * Set locale for language.
         *
         * @param locale {@link Locale}.
         * @return {@link Builder}.
         */
        public Builder setLocale(Locale locale) {
            this.mLocale = locale;
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