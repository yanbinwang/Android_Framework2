package com.example.mvvm.bean

/**
 * 国家类
 */
data class CountryBean(
    var code: String? = null,
    var name: String? = null,
    var childs: List<Province>? = null
)

data class Province(
    var code: String? = null,
    var name: String? = null,
    var childs: List<Proper>? = null
)

data class Proper(
    var code: String? = null,
    var name: String? = null
)
//data class CountryBean(
//    var countryName: String? = null,
//    var countryId: String? = null
//)