package com.yanzhenjie.album.api.choice;

/**
 * 选择器统一接口
 * 定义：所有选择器必须具备 多选 / 单选 两个能力
 *
 * @param Multiple  多选包装类
 * @param Single    单选包装类
 */
public interface Choice<Multiple, Single> {

    /**
     * 多选模式
     */
    Multiple multipleChoice();

    /**
     * 单选模式
     */
    Single singleChoice();

}