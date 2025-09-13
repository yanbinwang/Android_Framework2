package com.yanzhenjie.durban.error;

/**
 * Created by Yan Zhenjie on 2017/5/23.
 */
public class StorageError extends Exception {
    
    public StorageError(String message) {
        super(message);
    }

    public StorageError(Throwable cause) {
        super(cause);
    }

}