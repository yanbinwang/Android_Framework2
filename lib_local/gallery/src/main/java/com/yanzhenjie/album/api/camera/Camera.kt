package com.yanzhenjie.album.api.camera

/**
 * Created by YanZhenjie on 2017/8/18.
 */
interface Camera<Image, Video> {

    /**
     * Take picture.
     */
    fun image(): Image

    /**
     * Take video.
     */
    fun video(): Video

}