package com.example.thirdparty.firebase.bean

import com.example.common.utils.toList

data class LinkInfoBean(
    var language: Int? = null,//English
    var img: String? = null,//www.baidu.com
    var url: String? = null,//www.baidu.com
) {
    companion object {
        @JvmStatic
        fun handle(linkInfo: String?): LinkInfoBean? {
            return handle(linkInfo.toList(LinkInfoBean::class.java))
        }

        @JvmStatic
        fun handle(list: List<LinkInfoBean>?): LinkInfoBean? {
            //默認中文
            var defLanguage = 2
//        when (CountryUtil.getLanguage()) {
//            en_IN -> defLanguage = 3
//            in_ID -> defLanguage = 4
//            zh_TW -> defLanguage = 2
//        }
            return list?.find { it.language == defLanguage }
        }
    }
}