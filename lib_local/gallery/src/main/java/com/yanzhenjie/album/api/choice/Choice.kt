package com.yanzhenjie.album.api.choice

/**
 * Created by YanZhenjie on 2017/8/16.
 */
interface Choice<Multiple, Single> {

    /**
     * Multiple choice.
     */
    fun multipleChoice(): Multiple

    /**
     * Single choice.
     */
    fun singleChoice(): Single

}