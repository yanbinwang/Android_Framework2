package com.yanzhenjie.durban.error;

/**
 * 存储相关异常
 * 作用：图片保存、读取、文件创建等存储操作失败时抛出的自定义异常
 */
public class StorageError extends Exception {

    /**
     * 构造异常（带错误信息）
     * @param message 错误描述信息
     */
    public StorageError(String message) {
        super(message);
    }

    /**
     * 构造异常（带原始异常原因）
     * @param cause 导致该异常的根异常
     */
    public StorageError(Throwable cause) {
        super(cause);
    }

}