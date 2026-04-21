package com.example.album.callback

/**
 * 通用过滤器接口
 * 用于筛选文件、图片、文件夹等数据，符合条件返回true，不符合返回false
 */
interface Filter<T> {
    /**
     * 过滤文件/数据项
     *
     * @param attributes 待过滤的数据项（如图片、视频、文件实体）
     * @return true：保留该数据；false：过滤掉该数据
     */
    fun filter(attributes: T): Boolean
}