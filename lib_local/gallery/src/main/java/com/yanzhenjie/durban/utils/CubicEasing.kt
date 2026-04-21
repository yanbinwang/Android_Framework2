package com.yanzhenjie.durban.utils;

/**
 * 三次方缓动动画工具类
 * 作用：裁剪界面图片缩放、旋转、回弹的平滑动画计算
 */
public final class CubicEasing {

    /**
     * 私有构造，禁止实例化
     */
    private CubicEasing() {
    }

    /**
     * 缓动动画：先快后慢
     */
    public static float easeOut(float time, float start, float end, float duration) {
        return end * ((time = time / duration - 1.0f) * time * time + 1.0f) + start;
    }

    /**
     * 缓动动画：慢→快→慢
     */
    public static float easeInOut(float time, float start, float end, float duration) {
        return (time /= duration / 2.0f) < 1.0f ? end / 2.0f * time * time * time + start : end / 2.0f * ((time -= 2.0f) * time * time + 2.0f) + start;
    }

}