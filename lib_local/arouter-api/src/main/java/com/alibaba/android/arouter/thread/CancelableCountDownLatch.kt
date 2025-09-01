package com.alibaba.android.arouter.thread

import java.util.concurrent.CountDownLatch

/**
 * As its name.
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 16/8/29 15:48
 *
 * Constructs a {@code CountDownLatch} initialized with the given count.
 * @param count the number of times {@link #countDown} must be invoked
 *              before threads can pass through {@link #await}
 * @throws IllegalArgumentException if {@code count} is negative
 */
class CancelableCountDownLatch(count: Int) : CountDownLatch(count) {

    fun cancel() {
        while (count > 0) {
            countDown()
        }
    }

}