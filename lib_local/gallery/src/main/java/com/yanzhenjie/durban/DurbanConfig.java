package com.yanzhenjie.durban;

import java.util.Locale;

/**
 * <p>Durban config.</p>
 * Created by Yan Zhenjie on 2017/5/30.
 */
public class DurbanConfig {
    private Locale mLocale;

    private DurbanConfig(Builder build) {
        this.mLocale = build.mLocale;
    }

    /**
     * Get {@link Locale}.
     *
     * @return {@link Locale}.
     */
    public Locale getLocale() {
        return mLocale;
    }

    /**
     * Create a new builder.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private Locale mLocale;

        private Builder() {
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
         * @return {@link DurbanConfig}.
         */
        public DurbanConfig build() {
            return new DurbanConfig(this);
        }
    }

}
