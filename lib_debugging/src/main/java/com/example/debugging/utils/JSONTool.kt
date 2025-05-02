package com.example.debugging.utils

/**
 * JSON字符串格式化成JSON结构
 */
object JSONTool {

    @JvmStatic
    fun stringToJSON(strJson: String): String {
        if (strJson.isEmpty() || (strJson[0] != '{' && strJson[0] != '[')) return strJson
        // 计数tab的个数
        var tabNum = 0
        val jsonFormat = StringBuilder()
        val length = strJson.length
        var inValue = false
        var last = 0.toChar()
        for (i in 0..<length) {
            val c = strJson[i]
            if (c == '"' && '\\' != last) inValue = !inValue
            if (inValue) {
                jsonFormat.append(c)
                last = c
                continue
            }
            if (c == '{') {
                tabNum++
                jsonFormat.append(c).append("\n")
                jsonFormat.append(getSpaceOrTab(tabNum))
            } else if (c == '}') {
                tabNum--
                jsonFormat.append("\n")
                jsonFormat.append(getSpaceOrTab(tabNum))
                jsonFormat.append(c)
            } else if (c == ',') {
                jsonFormat.append(c).append("\n")
                jsonFormat.append(getSpaceOrTab(tabNum))
            } else if (c == ':') {
                val next = strJson[i + 1]
                jsonFormat.append(c)
                if (next != ' ') jsonFormat.append(" ")
            } else if (c == '[') {
                tabNum++
                val next = strJson[i + 1]
                if (next == ']') {
                    jsonFormat.append(c)
                } else {
                    jsonFormat.append(c).append("\n")
                    jsonFormat.append(getSpaceOrTab(tabNum))
                }
            } else if (c == ']') {
                tabNum--
                if (last == '[') {
                    jsonFormat.append(c)
                } else {
                    jsonFormat.append("\n").append(getSpaceOrTab(tabNum)).append(c)
                }
            } else {
                jsonFormat.append(c)
            }
            last = c
        }
        return jsonFormat.toString()
    }

    /**
     * 是空格还是tab
     */
    @JvmStatic
    private fun getSpaceOrTab(tabNum: Int): String {
        val sbTab = java.lang.StringBuilder()
        for (i in 0..<tabNum) {
            sbTab.append('\t')
        }
        return sbTab.toString()
    }

}