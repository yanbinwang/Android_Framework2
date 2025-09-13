package com.yanzhenjie.album.api.camera;

/**
 * Created by YanZhenjie on 2017/8/18.
 */
public interface Camera<Image, Video> {

    /**
     * Take picture.
     */
    Image image();

    /**
     * Take video.
     */
    Video video();

}