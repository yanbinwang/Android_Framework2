package com.yanzhenjie.album.api.camera;

/**
 * 相机功能统一接口（契约）
 * 定义：相机必须提供【拍照】和【录像】两个能力
 *
 * @param Image   拍照包装类
 * @param Video   录像包装类
 */
public interface Camera<Image, Video> {

    /**
     * 拍照
     */
    Image image();

    /**
     * 录像
     */
    Video video();

}