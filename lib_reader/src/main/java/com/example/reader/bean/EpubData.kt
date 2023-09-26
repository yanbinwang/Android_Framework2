package com.example.reader.bean

data class EpubData(
    var data: String? = null,//对于图片，为图片的绝对地址
    var secondData: String? = null,//图片用，备份地址，data 不行就来找这个
    var type: TYPE? = null//Epub 数据类型
) {
    enum class TYPE {
        TEXT,//文本(<div>, <p>)
        IMG,//图片(<img>)
        TITLE,//标题（<h1> - <h6>）
        LINK//超链接（<a>）
    }
}