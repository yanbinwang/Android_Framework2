package com.yanzhenjie.album;

/**
 * 相册全局配置类
 * 用于统一配置相册的图片加载器（AlbumLoader），采用 Builder 构建模式
 */
public class AlbumConfig {
    // 图片加载器（Glide/Picasso等）
    private final AlbumLoader mLoader;

    /**
     * 私有构造，通过 Builder 创建实例
     */
    private AlbumConfig(Builder builder) {
        this.mLoader = builder.mLoader == null ? AlbumLoader.DEFAULT : builder.mLoader;
    }

    /**
     * 获取配置好的图片加载器
     */
    public AlbumLoader getAlbumLoader() {
        return mLoader;
    }

    /**
     * 获取构建器实例
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * 构建器内部类
     */
    public static final class Builder {
        private AlbumLoader mLoader;

        private Builder() {
        }

        /**
         * 设置自定义图片加载器
         */
        public Builder setAlbumLoader(AlbumLoader loader) {
            this.mLoader = loader;
            return this;
        }

        /**
         * 构建最终配置对象
         */
        public AlbumConfig build() {
            return new AlbumConfig(this);
        }
    }

}