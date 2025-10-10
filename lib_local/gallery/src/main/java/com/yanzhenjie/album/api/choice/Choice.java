package com.yanzhenjie.album.api.choice;

/**
 * Created by YanZhenjie on 2017/8/16.
 */
public interface Choice<Multiple, Single> {

    /**
     * Multiple choice.
     */
    Multiple multipleChoice();

    /**
     * Single choice.
     */
    Single singleChoice();

}