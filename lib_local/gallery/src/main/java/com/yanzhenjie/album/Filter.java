package com.yanzhenjie.album;

/**
 * <p>Filter.</p>
 * Created by YanZhenjie on 2017/10/15.
 */
public interface Filter<T> {

    /**
     * Filter the file.
     *
     * @param attributes attributes of file.
     * @return filter returns true, otherwise false.
     */
    boolean filter(T attributes);

}